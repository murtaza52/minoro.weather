(ns minoro.extracts.current-weather
  (:require [minoro.config :as config]
            [minoro.cities :as cities]
            [clojure.core.reducers :as r]
            [cheshire.core :as cs]
            [minoro.http :refer [get-data]]
            [minoro.utils :as utils]
            [clojure.spec.alpha :as s]
            [minoro.error-reporting :as mer]
            [minoro.extracts.specs.current-weather :as scw]))

(def current-weather-url (str (config/current-weather-endpoint)
                              "?id=%s&units=metric&APPID=%s"))

(def file-prefix "current_weather")

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

(def columns
  '[dt
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
      [(utils/unix-time->sql-timestamp dt)
       city-name
       city-id
       country-code
       timezone
       (utils/unix-time->sql-timestamp sunrise)
       (utils/unix-time->sql-timestamp sunset)
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
  (cons
   (map name columns)
   (into [] (r/fold 20 r/cat r/append! (->> resps
                                            (r/map :body)
                                            (r/map #(cs/parse-string % true))
                                            (r/mapcat :list)
                                            (r/map process-resp)
                                            (r/filter seq))))))

(defn retrieve-current-weather-data*
  [cities]
  (->> cities
       (sequence xget-current-weather-urls)
       get-data
       process-responses
       (utils/export-data (utils/get-file-name file-prefix))))

(comment
  (retrieve-current-weather-data* [:paris-fr :london-gb]))

(defn retrieve-current-weather-data
  []
  (retrieve-current-weather-data* cities/cities))

(comment (retrieve-current-weather-data))

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
  (s/explain-str ::scw/current-weather-m m))
