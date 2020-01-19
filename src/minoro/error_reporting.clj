(ns minoro.error-reporting
  (:require [taoensso.timbre :as timbre]
            [mount.core :refer [defstate]]
            [clojure.core.async :as async :refer [go]]
            [while-let.core :refer [while-let]]))

(defn start-reporting-proc
  [c]
  (go
    (while-let [[msg data] (async/<! c)]
      (try
        ;; write errors to slack / logstash etc
        ;; logging for now
        (timbre/error msg)
        (timbre/error data)
        ;; catch any errors otherwise the queue will get seized
        (catch Throwable e
          (timbre/error msg)
          (timbre/error data)
          (timbre/error e))))))

(def reports-chan (async/chan 3))

(defstate reporting-proc
  :start (start-reporting-proc reports-chan)
  :stop (async/close! reports-chan))

(defn report-errors
  [msg data]
  (async/put! reports-chan [msg data]))

(defn filter-errors
  [resps]
  (let [grouped-resp (group-by :status resps)]
    (when-let [non-200-resp (seq (dissoc grouped-resp 200))]
      ;; will not park or block
      (report-errors "Non 200 Responses while retrieving data" non-200-resp))
    (get-in grouped-resp [200])))
