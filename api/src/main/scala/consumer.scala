package com.banno

import java.util.Properties
import io.confluent.kafka.serializers.KafkaAvroDecoder
import kafka.consumer.{Consumer, ConsumerConfig, ConsumerConnector, KafkaStream}
import kafka.utils.VerifiableProperties
import org.apache.avro.generic.IndexedRecord

trait ReplicateIntoRocksDb extends Config {
  lazy val zookeeperConnect = config.getString("api.zookeeper.connect")
  lazy val schemaRegistryHost = config.getString("api.schema-registry.host")
  lazy val schemaRegistryPort = config.getInt("api.schema-registry.port")
  lazy val usersTopic = config.getString("api.kafka.users-topic")
  lazy val tweetsTopic = config.getString("api.kafka.tweets-topic")

  def startReplication(): Unit = {
    val props = new Properties()
    props.put("zookeeper.connect", zookeeperConnect)
    props.put("group.id", "api") //TODO include hostname if there are multiple instances of this application
    props.put("auto.offset.reset", "smallest")
    props.put("dual.commit.enabled", "false")
    props.put("offsets.storage", "kafka")
    props.put("schema.registry.url", s"http://$schemaRegistryHost:$schemaRegistryPort")

    val vProps = new VerifiableProperties(props)
    val keyDecoder = new KafkaAvroDecoder(vProps)
    val valueDecoder = new KafkaAvroDecoder(vProps)

    val consumerConnector = Consumer.create(new ConsumerConfig(vProps.props)) //TODO use props here?
    val streams = consumerConnector.createMessageStreams(Map(usersTopic -> 1, tweetsTopic -> 1), keyDecoder, valueDecoder)
    new Thread(new RocksDbUpdater(usersTopic, streams(usersTopic)(0))).start()

    sys.addShutdownHook {
      consumerConnector.shutdown()
    }
  }
}

class RocksDbUpdater(topic: String, stream: KafkaStream[Object, Object]) extends Runnable with Logging {
  override def run(): Unit = {
    log.debug(s"Consuming from $topic...")
    stream foreach { messageAndMetadata => 
      val key = messageAndMetadata.key
      val message = messageAndMetadata.message
      log.debug(s"Received from topic $topic: key = $key, message = $message")
    }
    log.debug(s"Done consuming from $topic")
  }
}
