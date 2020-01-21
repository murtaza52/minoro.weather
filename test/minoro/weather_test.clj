(ns minoro.weather-test
  (:require [clojure.test :refer :all]
            [minoro.extracts.current-weather :as cw]
            [minoro.extracts.forecast :as fct]
            [minoro.data-test :as td]
            [minoro.helpers-test :as helpers]
            [clojure.spec.alpha :as s]
            [minoro.extracts.specs.current-weather :as scw]
            [minoro.extracts.specs.forecast :as sfct]
            [minoro.cities :as cities]))

;; (use-fixtures :once helpers/setup)

(deftest data-transforms
  (testing "Current Weather"
    (is (= td/processed-weather-data
           (cw/process-resp td/current-weather-data))))
  (testing "Forecast Weather"
    (is (= td/processed-forecast-data
           (fct/process-resp td/forecast-data)))))

(deftest openweather-contract
  (testing "Current Weather"
    (is (= true (s/valid? ::scw/current-weather-m (first (helpers/retrieve-current-weather-data [:paris-fr]))))))
  (testing "Forecast Weather"
    (is (= true (s/valid? ::sfct/forecast-m (first (helpers/retrieve-forecast-weather-data [:paris-fr])))))))

(deftest smoke-test
  (testing "Current Weather"
    (is (string? (:ETag (cw/retrieve-current-weather-data cities/cities)))))
  (testing "Forecast Weather"
    (is (string? (:ETag (fct/retrieve-forecast-data cities/cities))))))

(comment
  (clojure.test/run-tests))
