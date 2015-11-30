package com.banno

import java.util.Properties
import io.confluent.kafka.serializers.KafkaAvroDecoder
import kafka.consumer.{Consumer, ConsumerConfig, ConsumerConnector, KafkaStream}
import kafka.utils.VerifiableProperties
import org.apache.avro.generic.GenericRecord

trait KafkaConsumer extends KafkaConfig {
  
  def getStreams(groupId: String): Map[String, List[KafkaStream[Object, Object]]] = {
    val props = new Properties()
    props.put("zookeeper.connect", zookeeperConnect)
    props.put("group.id", groupId) //TODO include hostname if there are multiple instances of this application
    props.put("auto.offset.reset", "smallest")
    props.put("dual.commit.enabled", "false")
    props.put("offsets.storage", "kafka")
    props.put("schema.registry.url", schemaRegistryUrl)

    val vProps = new VerifiableProperties(props)
    val keyDecoder = new KafkaAvroDecoder(vProps)
    val valueDecoder = new KafkaAvroDecoder(vProps)

    val consumerConnector = Consumer.create(new ConsumerConfig(props))
    sys.addShutdownHook {
      consumerConnector.shutdown()
    }
    consumerConnector.createMessageStreams(Map(usersTopic -> 1, tweetsTopic -> 1), keyDecoder, valueDecoder).toMap
  }
}

trait DatabaseChangeHandler[K, V] {
  def rowChanged(key: K, value: V): Unit
  def rowDeleted(key: K): Unit
}

class DatabaseChangeConsumer[K, V]
    (topic: String, stream: KafkaStream[Object, Object], handler: DatabaseChangeHandler[K, V])
    (implicit serde: KafkaAvroSerde[K, V])
  extends Runnable with Logging {

  override def run(): Unit = {
    log.debug(s"Consuming from $topic...")
    stream foreach { messageAndMetadata => 
      val key = serde.keyFromRecord(messageAndMetadata.key.asInstanceOf[GenericRecord])
      val message = messageAndMetadata.message
      if (message == null) handler.rowDeleted(key)
      else handler.rowChanged(key, serde.valueFromRecord(message.asInstanceOf[GenericRecord]))
    }
    log.debug(s"Done consuming from $topic")
  }
}
