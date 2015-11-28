package com.banno

import com.typesafe.config.ConfigFactory

trait Config {
  lazy val config = ConfigFactory.load()
}

trait ApiConfig extends Config {
  lazy val httpServerHost = config.getString("api.http-server.host")
  lazy val httpServerPort = config.getInt("api.http-server.port")
}

trait DatabaseConfig extends Config {
  lazy val databaseUrl = config.getString("api.database.url")
  lazy val databaseUsername = config.getString("api.database.username")
  lazy val databasePassword = config.getString("api.database.password")
}

trait KafkaConfig extends Config {
  lazy val zookeeperConnect = config.getString("api.zookeeper.connect")
  lazy val schemaRegistryHost = config.getString("api.schema-registry.host")
  lazy val schemaRegistryPort = config.getInt("api.schema-registry.port")
  lazy val schemaRegistryUrl = s"http://$schemaRegistryHost:$schemaRegistryPort"
  lazy val usersTopic = config.getString("api.kafka.users-topic")
  lazy val tweetsTopic = config.getString("api.kafka.tweets-topic")
}
