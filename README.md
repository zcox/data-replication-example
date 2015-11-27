Simple example that stores users and tweets in Postgres, uses Bottled Water to stream data changes to Kafka topics, and then replicates data into RocksDB and Elasticsearch.

[TODO links in above paragraph]
[TODO insert architecture diagram]

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
