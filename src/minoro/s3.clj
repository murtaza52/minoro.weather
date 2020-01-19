(ns minoro.s3
  (:require [minoro.config :as config]
            [minoro.utils :as mu]
            [cognitect.aws.client.api :as aws]
            [cognitect.aws.credentials :as creds]
            [mount.core :refer [defstate]]
            [taoensso.timbre :as timbre]))

(defn get-s3-client
  []
  (aws/client {:api                  :s3
               :credentials-provider (creds/basic-credentials-provider (config/aws-creds))
               :endpoint-override    (config/aws-endpoint-override)}))

(defstate s3-client
  :start (get-s3-client))

(defn upload-file
  [bucket-name file-name]
  (let [r (aws/invoke s3-client {:op      :PutObject
                                 :request {:Bucket               bucket-name
                                           :ServerSideEncryption "AES256"
                                           :Key                  file-name
                                           :Body                 (mu/read-file file-name)}})]
    (timbre/info (str "Uploaded file to bucket - " bucket-name))
    (timbre/info r)
    r))

(comment
  (upload-file "forecast" "reports/forecast_2020-01-16T13:10:03.020.csv"))
