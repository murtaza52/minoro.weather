# minoro.weather

FIXME: my new application.

## Installation

Download from https://github.com/minoro/minoro.weather.

## Usage

FIXME: explanation

Run the project directly:

    $ clojure -m minoro.weather

Run the project's tests (they'll fail until you edit them):

    $ clojure -A:test:runner

Build an uberjar:

    $ clojure -A:uberjar

Run that uberjar:

    $ java -jar minoro.weather.jar

## Options

FIXME: listing of options this app accepts.

## Examples

...

### Bugs

...
f fs
### Any Other Sections
### That You Think
### Might be Useful

## License

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
