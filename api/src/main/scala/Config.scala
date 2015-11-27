package com.banno

import com.typesafe.config.ConfigFactory

trait ApiConfig {
  lazy val config = ConfigFactory.load()

  lazy val httpServerHost = config.getString("api.http-server.host")
  lazy val httpServerPort = config.getInt("api.http-server.port")
}
