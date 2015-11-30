package com.banno

object Main extends App with HttpServer with ReplicateIntoRocksDb with ReplicateIntoElasticsearch {
  startHttpServer()
  startRocksDbReplication()
  startElasticsearchReplication()
  Metrics.reportToGraphite()
}
