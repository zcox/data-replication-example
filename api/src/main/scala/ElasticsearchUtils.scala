package com.banno

import org.elasticsearch.common.settings.Settings
import org.elasticsearch.common.transport.TransportAddress
import org.elasticsearch.client.transport.TransportClient
import org.elasticsearch.common.transport.InetSocketTransportAddress
import java.net.InetAddress
import scala.sys.addShutdownHook

trait ElasticsearchUtils extends Config {
  lazy val elasticsearchHost = config.getString("api.elasticsearch.host")
  lazy val elasticsearchTransportPort = config.getInt("api.elasticsearch.transport-port")
  lazy val elasticsearchClusterName = config.getString("api.elasticsearch.cluster-name")

  lazy val elasticsearchClient = {
    val settings = Settings.settingsBuilder().put("cluster.name", elasticsearchClusterName).build()
    val client = TransportClient.builder().settings(settings).build()
    client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(elasticsearchHost), elasticsearchTransportPort))

    addShutdownHook {
      client.close()
    }

    client
  }
}
