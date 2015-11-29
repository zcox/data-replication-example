package com.banno

import nl.grons.metrics.scala.InstrumentedBuilder

import com.codahale.metrics.{MetricRegistry, MetricFilter}
import com.codahale.metrics.graphite.{Graphite, GraphiteReporter}
import java.net.InetSocketAddress
import java.util.concurrent.TimeUnit

object Metrics extends Config {
  lazy val metricRegistry = new MetricRegistry()

  lazy val graphiteHost = config.getString("api.graphite.host")
  lazy val graphitePort = config.getInt("api.graphite.port")
  def reportToGraphite(): Unit = {
    val graphite = new Graphite(new InetSocketAddress(graphiteHost, graphitePort))
    val reporter = GraphiteReporter.forRegistry(metricRegistry)
      .prefixedWith("api")
      .convertRatesTo(TimeUnit.SECONDS)
      .convertDurationsTo(TimeUnit.MILLISECONDS)
      .filter(MetricFilter.ALL)
      .build(graphite)
    reporter.start(10, TimeUnit.SECONDS)
  }
}

trait Instrumented extends InstrumentedBuilder {
  val metricRegistry = Metrics.metricRegistry
}
