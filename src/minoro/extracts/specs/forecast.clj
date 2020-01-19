(ns minoro.extracts.specs.forecast
  (:require [minoro.utils :as utils]
            [clojure.spec.alpha :as s]))

(s/def ::name string?)

(s/def ::unix-time #(utils/unix-time->sql-timestamp % 0))
(s/def ::iso-time #(utils/iso-time->sql-timestamp % 0))

(s/def ::speed number?)
(s/def ::deg number?)
(s/def ::wind (s/keys :opt-un [::speed ::deg]))

(s/def ::dt ::unix-time)

(s/def ::dt_txt ::iso-time)

(s/def ::all number?)
(s/def ::clouds (s/keys :req-un [::all]))

(s/def ::country string?)
(s/def ::timezone number?)

(s/def ::temp number?)
(s/def ::feels_like number?)
(s/def ::temp_min number?)
(s/def ::temp_max number?)
(s/def ::pressure number?)
(s/def ::humidity number?)
(s/def ::grnd_level number?)
(s/def ::sea_level number?)

(s/def ::main (s/keys :req-un [::temp ::feels_like ::temp_min ::temp_max ::pressure ::humidity ::grnd_level ::sea_level]))

(s/def ::id integer?)

(s/def ::forecast-m (s/keys :req-un [::name ::dt ::dt_txt ::country ::timezone ::wind ::id ::clouds ::main]))
