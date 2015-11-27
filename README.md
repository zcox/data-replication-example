== Overview

Simple example that stores users and tweets in [Postgres](http://www.postgresql.org/), uses [Bottled Water](https://github.com/confluentinc/bottledwater-pg) to stream data changes to [Kafka](http://kafka.apache.org/) topics, and then replicates data into [RocksDB](http://rocksdb.org/) and [Elasticsearch](https://www.elastic.co/products/elasticsearch).

[TODO insert architecture diagram]

== Run the Things

Run all data stores & services (e.g. Postgres, Bottled Water, Zookeeper, Kafka, Elasticsearch):

```
docker-compose up -d
```

Run REST API service:

```
cd api
sbt run
```

[![Stories in Ready](https://badge.waffle.io/zcox/data-replication-example.png?label=ready&title=Ready)](https://waffle.io/zcox/data-replication-example)
