(ns minoro.weather
  (:require [taoensso.timbre :as timbre]
            [mount.core :as mount]
            ;; for mount initialization
            [minoro.http]
            [minoro.error-reporting]
            [minoro.s3]))

;; log uncaught exceptions in threads
(Thread/setDefaultUncaughtExceptionHandler
 (reify Thread$UncaughtExceptionHandler
   (uncaughtException [_ thread ex]
     (timbre/error {:what :uncaught-exception
                    :exception ex
                    :where (str "Uncaught exception on" (.getName thread))}))))

(defn stop-app
  []
  (doseq [component (:stopped (mount/stop))]
    (timbre/info component "stopped")))

(defn start-app
  [logging-level]
  (timbre/set-level! logging-level)
  (doseq [component (:started (mount/start))]
    (timbre/info component "started"))
  (.addShutdownHook (Runtime/getRuntime) (Thread. stop-app)))

(defn -main
  [& args]
  (start-app :info))

(comment
  (start-app :info)
  (stop-app))
