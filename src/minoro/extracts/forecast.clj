(ns minoro.extracts.forecast
  (:require [minoro.config :as config]
            [minoro.cities :as cities]
            [clojure.core.reducers :as r]
            [cheshire.core :as cs]
            [minoro.http :refer [get-data]]
            [minoro.utils :as utils]
            [clojure.spec.alpha :as s]
            [minoro.error-reporting :as mer]
            [minoro.extracts.specs.forecast :as sft]
            [minoro.s3 :as s3]))

(def forecast-url (str (config/forecast-endpoint)
                              "?id=%s&units=metric&APPID=%s"))

(def file-prefix "reports/forecast")

(defn make-forecast-url
  [id]
  (format forecast-url
          id
          (config/openweather-api-key)))

(make-forecast-url 1)

(def xget-forecast-urls
  (comp (map cities/city->ids)
     (map make-forecast-url)))

(defn get-forecast-urls
  [cities]
  (sequence xget-forecast-urls cities))

(comment
  (get-forecast-urls cities/cities))

(defn generate-city-id
  [city-name country-code city-id]
  (str city-name "_" country-code "_" city-id) )

(def columns
  '[gen-city-id
    dt
    forecast-date
    forecast-date-time
    city-name
    city-id
    country-code
    timezone
    wind-speed
    wind-deg
    cloudiness
    temp
    feels-like
    temp-min
    temp-max
    pressure
    humidity
    pressure-sea-level
    pressure-grnd-level])

(defn process-resp
  [{city-name                         :name
    dt                                :dt
    forecast-dt                       :dt_txt
    {wind-speed :speed wind-deg :deg} :wind
    city-id                           :id
    {cloudiness :all}                 :clouds
    country-code                      :country
    timezone                          :timezone
    {pressure-sea-level  :sea_level
     temp                :temp
     feels-like          :feels_like
     temp-min            :temp_min
     temp-max            :temp_max
     pressure            :pressure
     humidity            :humidity
     pressure-grnd-level :grnd_level} :main
    :as                               m}]
  (try
    (if (s/valid? ::sft/forecast-m m)
      [(generate-city-id city-name country-code city-id)
       (utils/unix-time->sql-timestamp dt timezone)
       (utils/iso-time->sql-date forecast-dt timezone)
       (utils/iso-time->sql-timestamp forecast-dt timezone)
       city-name
       city-id
       country-code
       timezone
       wind-speed
       wind-deg
       cloudiness
       temp
       feels-like
       temp-min
       temp-max
       pressure
       humidity
       pressure-sea-level
       pressure-grnd-level]
      (do
        (mer/report-errors "Validation Error in Forecast Weather Data" (s/explain-str ::sft/forecast-m m))
        []))
    (catch Throwable e
      (mer/report-errors e m)
      [])))

(defn add-city-meta
  "Add the city meta to every forecast record"
  [{:keys [list city]}]
  (map #(merge % city) list))

(defn process-responses
  [resps]
  (into [] (r/fold 20 r/cat r/append! (->> resps
                                           (r/map :body)
                                           (r/map #(cs/parse-string % true))
                                           (r/mapcat add-city-meta)
                                           (r/map process-resp)
                                           (r/filter seq)))))

(defn add-column-headers
  [coll]
  (cons (map name columns) coll))

(defn retrieve-forecast-data
  [cities]
  (let [file-name (utils/get-file-name file-prefix)]
    (->> cities
         get-forecast-urls
         get-data
         process-responses
         add-column-headers
         (utils/write-file file-name))
    (s3/upload-file (:forecast (config/s3-buckets))
                    file-name)))

(comment
  (retrieve-forecast-data [:paris-fr :london-gb]))

(comment
  (def m {:sunset 1578929169,
          :coord {:lat 37.9795, :lon 23.7162},
          :dt_txt "2020-01-13 18:00:00",
          :timezone 7200,
          :name "Athens",
          :dt 1578938400,
          :sunrise 1578894032,
          :wind {:speed 2.68, :deg 11},
          :id 264371,
          :weather [{:id 800, :main "Clear", :description "clear sky", :icon "01n"}],
          :clouds {:all 7},
          :sys {:pod "n"},
          :main
          {:sea_level 1026,
           :pressure 1026,
           :temp 9.59,
           :temp_max 9.71,
           :temp_min 9.59,
           :temp_kf -0.12,
           :humidity 67,
           :feels_like 6.35,
           :grnd_level 1013},
          :country "GR"})
  (process-resp m)
  (s/explain ::sft/forecast-m m)
  (retrieve-forecast-data [:paris-fr :london-gb])
  (retrieve-forecast-data cities/cities))
