(ns minoro.extracts.current-weather
  (:require [minoro.config :as config]
            [minoro.cities :as cities]
            [clojure.core.reducers :as r]
            [cheshire.core :as cs]
            [minoro.http :refer [get-data]]
            [minoro.utils :as utils]
            [clojure.spec.alpha :as s]
            [minoro.error-reporting :as mer]
            [minoro.extracts.specs.current-weather :as scw]
            [minoro.s3 :as s3]
            [taoensso.timbre :as timbre]))

(def current-weather-url (str (config/current-weather-endpoint)
                              "?id=%s&units=metric&APPID=%s"))

(def file-prefix "reports/current_weather")

(defn make-current-weather-url
  [ids]
  (format current-weather-url
          (apply str ids)
          (config/openweather-api-key)))

(make-current-weather-url [1 2])

(def xget-current-weather-urls
  (comp (map cities/city->ids)
     (partition-all (config/current-weather-city-limit))
     (map #(interpose "," %))
     (map make-current-weather-url)))

(comment
  (sequence xget-current-weather-urls cities/cities))

(defn get-current-weather-urls
  [cities]
  (sequence xget-current-weather-urls cities))

(defn generate-city-id
  [city-name country-code city-id]
  (str city-name "_" country-code "_" city-id))

(def columns
  '[gen-city-id
    date
    date-time
    city-name
    city-id
    country-code
    timezone
    sunrise
    sunset
    wind-speed
    wind-deg
    cloudiness
    temp
    feels-like
    temp-min
    temp-max
    pressure
    humidity
    visibility])

(defn process-resp
  [{city-name                         :name
    dt                                :dt
    {wind-speed :speed wind-deg :deg} :wind
    city-id                           :id
    {cloudiness :all}                 :clouds
    {country-code :country
     timezone     :timezone
     sunrise      :sunrise
     sunset       :sunset}            :sys
    {temp       :temp
     feels-like :feels_like
     temp-min   :temp_min
     temp-max   :temp_max
     pressure   :pressure
     humidity   :humidity}            :main
    visibility                        :visibility
    :as m}]
  (try
    (if (s/valid? ::scw/current-weather-m m)
         [(generate-city-id city-name country-code city-id)
          (utils/unix-time->sql-date dt timezone)
          (utils/unix-time->sql-timestamp dt timezone)
          city-name
          city-id
          country-code
          timezone
          (utils/unix-time->sql-timestamp sunrise timezone)
          (utils/unix-time->sql-timestamp sunset timezone)
          wind-speed
          wind-deg
          cloudiness
          temp
          feels-like
          temp-min
          temp-max
          pressure
          humidity
          visibility]
         (do
           (mer/report-errors "Validation Error in Current Weather Data" (s/explain-str ::scw/current-weather-m m))
           []))
    (catch Throwable e
      (mer/report-errors e m)
      [])))


(defn process-responses
  [resps]
  (into [] (r/fold 20 r/cat r/append! (->> resps
                                           (r/map :body)
                                           (r/map #(cs/parse-string % true))
                                           (r/mapcat :list)
                                           (r/map process-resp)
                                           (r/filter seq)))))

(defn add-column-headers
  [coll]
  (cons (map name columns) coll))

(defn retrieve-current-weather-data
  [cities]
  (timbre/info "Starting Extract: Current Weather")
  (let [file-name (utils/get-file-name file-prefix)]
    (->> cities
         get-current-weather-urls
         get-data
         process-responses
         add-column-headers
         (utils/write-file file-name)
         (utils/log-info (str "Writing file: " file-name)))
    (utils/log-info "Uploading file to s3")
    (utils/log-info (s3/upload-file (:current-weather (config/s3-buckets))
                                    file-name))))


(comment
  (def m {:coord     {:lon 23.72, :lat 37.98},
          :name       "Athens",
          :dt         1578830944,
          :wind       {:speed 3.6, :deg 330},
          :id         264371,
          :weather
          [{:id 802, :main "Clouds", :description "scattered clouds", :icon "03d"}],
          :clouds     {:all 40},
          :sys
          {:country "GR", :timezone 7200, :sunrise 1578807644, :sunset 1578842708},
          :main
          {:temp       13.01,
           :feels_like 9.16,
           :temp_min   11.11,
           :temp_max   14.44,
           :pressure   1023,
           :humidity   "1"},
           :visibility 10000})
  (process-resp m)
  (s/explain-str ::scw/current-weather-m m)
  (retrieve-current-weather-data [:paris-fr :london-gb])
  (retrieve-current-weather-data cities/cities))
