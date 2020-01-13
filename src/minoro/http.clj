(ns minoro.http
  (:require [minoro.config :as config]
            [com.climate.claypoole :as cp]
            [mount.core :refer [defstate]]
            [clj-http.client :as client]
            [minoro.error-reporting :refer [filter-errors]]))

(defstate cp-threadpool
  :start (cp/threadpool (config/cp-threadpool))
  :stop (cp/shutdown cp-threadpool))

(defn http-get
  "An http get call using claypoole future."
  [url]
  (cp/future cp-threadpool (client/get url)))

(defn get-data
  [urls]
  (->> urls
       ;; do not run the http call + deref as a transducer, otherwise the IO will not be concurrent
       (map http-get)
       (map deref)
       filter-errors))

(comment
  @(http-get "http://api.openweathermap.org/data/2.5/group?id=264371,6455259&units=metric&APPID=4eb295eac46b0bec3ee898ed86e21b9e")

  (get-data ["http://api.openweathermap.org/data/2.5/group?id=524901,3169070&units=metric&APPID=4eb295eac46b0bec3ee898ed86e21b9e"
            "http://api.openweathermap.org/data/2.5/group?id=264371,6455259&units=metric&APPID=4eb295eac46b0bec3ee898ed86e21b9e"]))

