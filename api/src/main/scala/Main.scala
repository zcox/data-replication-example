package com.banno

object Main extends App with HttpServer with ReplicateIntoRocksDb {
  startHttpServer()
  startReplication()
}
