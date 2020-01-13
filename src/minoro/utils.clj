(ns minoro.utils
  (:require [java-time :as jt]
            [clojure.java.io :as io]
            [clojure.data.csv :as csv]))

(defn unix-time->sql-timestamp
  [v]
  (.toString (jt/instant->sql-timestamp (* v 1000))))

(unix-time->sql-timestamp 1578834545)

(defn get-file-name
  [prefix]
  (str prefix "_" (jt/local-date-time) ".csv"))

(get-file-name "current_weather")

(defn export-data
  [file-name coll]
  (with-open [writer (io/writer file-name)]
    (csv/write-csv writer coll)))

(defn iso-time->sql-timestamp
  [iso-time]
  (.toString (jt/sql-timestamp (jt/local-date-time "yyyy-MM-dd HH:mm:ss" iso-time))))

(iso-time->sql-timestamp "2020-01-13 18:00:00")
