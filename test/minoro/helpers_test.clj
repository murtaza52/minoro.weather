(ns minoro.helpers-test
  "Helper functions for testing"
  (:require [minoro.extracts.current-weather :as cw]
            [minoro.extracts.forecast :as fct]
            [minoro.http :as http]
            [cheshire.core :as cs]
            [minoro.weather :as mw]))

;;; fixtures ;;;

(defn setup
  [tests]
  (mw/start-app :info)
  (tests)
  #_(mw/stop-app :info))


;;; helpers for retrieving data ;;;

(defn retrieve-current-weather-data
  [cities]
  (->> cities
       cw/get-current-weather-urls
       http/get-data
       (map :body)
       (map #(cs/parse-string % true))
       (mapcat :list)))

(defn retrieve-forecast-weather-data
  [cities]
  (->> cities
       fct/get-forecast-urls
       http/get-data
       (map :body)
       (map #(cs/parse-string % true))
       (mapcat fct/add-city-meta)))

(comment
  (retrieve-weather-data [:paris-fr])
  (retrieve-forecast-weather-data [:paris-fr]))
