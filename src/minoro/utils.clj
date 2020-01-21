(ns minoro.utils
  (:require [java-time :as jt]
            [clojure.java.io :as io]
            [clojure.data.csv :as csv]
            [clojure.string :as cs]
            [taoensso.timbre :as timbre])
  (:import [java.time LocalDateTime ZoneOffset Instant]))

(defn unix-time->local-date-time
  [unix-time-s utc-offset-s]
  (LocalDateTime/ofInstant (jt/instant (* unix-time-s 1000)) (ZoneOffset/ofTotalSeconds utc-offset-s)))

(defn unix-time->sql-date
  [unix-time-s utc-offset-s]
  (jt/format
   (jt/sql-timestamp
    (jt/truncate-to (unix-time->local-date-time unix-time-s utc-offset-s) :days))))

(defn unix-time->sql-timestamp
  [unix-time-s utc-offset-s]
  (jt/format
   (jt/sql-timestamp (unix-time->local-date-time unix-time-s utc-offset-s))))

(comment
  (unix-time->local-date-time 1578830944 7200)
  (unix-time->sql-date 1578830944 7200)
  (unix-time->sql-timestamp 1578830944 7200))

(defn get-file-name
  [prefix]
  (str prefix "_" (jt/local-date-time) ".csv"))

(comment (get-file-name "a"))

(defn write-file
  [file-name v]
  (with-open [writer (io/writer file-name)]
    (csv/write-csv writer v)))

(defn read-file
  [file-name]
  (io/input-stream file-name))

(defn openweather-iso-time->iso-formatted
  [v]
  (let [[f s] (cs/split v #" ")]
    (str f "T" s "Z")))

(comment (openweather-iso-time->iso-formatted "2020-01-13 18:00:00"))

(defn iso-time->local-date-time
  [iso-time utc-offset-s]
  (LocalDateTime/ofInstant (Instant/parse (openweather-iso-time->iso-formatted iso-time))
                           (ZoneOffset/ofTotalSeconds utc-offset-s)))

(defn iso-time->sql-timestamp
  [iso-time utc-offset-s]
  (jt/format
   (jt/sql-timestamp (iso-time->local-date-time iso-time utc-offset-s))))

(defn iso-time->sql-date
  [iso-time utc-offset-s]
  (jt/format
   (jt/sql-timestamp
    (jt/truncate-to (iso-time->local-date-time iso-time utc-offset-s) :days))))

(comment
  (iso-time->sql-timestamp "2020-01-13 18:00:00" 7200)
  (iso-time->sql-date "2020-01-13 18:00:00" 7200))

(defn log-info
  "Logger that can be used in threading macros"
  ([msg] (timbre/info msg) msg)
  ([msg v] (timbre/info msg) v))
