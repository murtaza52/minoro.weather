(ns minoro.config
  (:require [aero.core :as aero]
            [mount.core :refer [defstate]]))

(defstate config :start (aero/read-config "resources/config.edn"))

(def openweather-api-key #(get-in config [:openweather :api-key]))

(def current-weather-endpoint #(get-in config [:openweather :current-weather-endpoint]))

(def current-weather-city-limit #(get-in config [:openweather :current-weather-city-limit]))

(def forecast-endpoint #(get-in config [:openweather :forecast-endpoint]))

(def cp-threadpool #(get-in config [:claypoole :threadpool-size]))
