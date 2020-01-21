# minoro.weather

An application which shows data retrieval / massaging using clojure, retrieves and massages data from Open Weather API. Concurrent I/O using claypool, error reporting using core.async, data massaging using core.reducers and request preparation using transducers.

## Localstack

Localstack can be setup locally to emulate s3 services. 

Install aws cli:

    $ pip install awscli
    
Configure aws cli with dummy creds:

    $ aws configure

Start the container:

    $ docker-compose up -d

Create the buckets:

    $ aws --endpoint-url=http://localhost:4572 s3 mb s3://current-weather

    $ aws --endpoint-url=http://localhost:4572 s3 mb s3://forecast

Attach ACL to the buckets:

    $ aws --endpoint-url=http://localhost:4572 s3api put-bucket-acl --bucket current-weather --acl public-read 

    $ aws --endpoint-url=http://localhost:4572 s3api put-bucket-acl --bucket forecast --acl public-read 

Navigate to `http://localhost:8055/` to check for the buckets.
