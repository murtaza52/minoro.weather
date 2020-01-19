(ns minoro.extracts.specs.current-weather
  (:require [minoro.utils :as utils]
            [clojure.spec.alpha :as s]))

(s/def ::name string?)

(s/def ::unix-time #(utils/unix-time->sql-timestamp % 0))

(s/def ::speed number?)
(s/def ::deg number?)
(s/def ::wind (s/keys :opt-un [::speed ::deg]))

(s/def ::lon number?)
(s/def ::lat number?)
(s/def ::coord (s/keys :req-un [::lon ::lat]))

(s/def ::dt ::unix-time)

(s/def ::all number?)
(s/def ::clouds (s/keys :req-un [::all]))

(s/def ::country string?)
(s/def ::timezone number?)
(s/def ::sunrise number?)
(s/def ::sunset number?)

(s/def ::sys (s/keys :req-un [::country ::timezone ::sunrise ::sunset]))

(s/def ::temp number?)
(s/def ::feels_like number?)
(s/def ::temp_min number?)
(s/def ::temp_max number?)
(s/def ::pressure number?)
(s/def ::humidity number?)

(s/def ::main (s/keys :req-un [::temp ::feels_like ::temp_min ::temp_max ::pressure ::humidity]))

(s/def ::visibility number?)

(s/def ::id integer?)

(s/def ::current-weather-m (s/keys :req-un [::coord ::name ::dt ::wind ::id ::clouds ::sys ::main ::visibility]))
