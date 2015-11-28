package com.banno

import java.util.Properties
import io.confluent.kafka.serializers.KafkaAvroDecoder
import kafka.consumer.{Consumer, ConsumerConfig, ConsumerConnector, KafkaStream}
import kafka.utils.VerifiableProperties
import org.apache.avro.generic.GenericRecord
import org.rocksdb.{RocksDB, Options, BlockBasedTableConfig, CompressionType, CompactionStyle}

trait ReplicateIntoRocksDb extends KafkaConfig {

  def startReplication(): Unit = {
    val props = new Properties()
    props.put("zookeeper.connect", zookeeperConnect)
    props.put("group.id", "api") //TODO include hostname if there are multiple instances of this application
    props.put("auto.offset.reset", "smallest")
    props.put("dual.commit.enabled", "false")
    props.put("offsets.storage", "kafka")
    props.put("schema.registry.url", schemaRegistryUrl)

    val vProps = new VerifiableProperties(props)
    val keyDecoder = new KafkaAvroDecoder(vProps)
    val valueDecoder = new KafkaAvroDecoder(vProps)

    val consumerConnector = Consumer.create(new ConsumerConfig(props))
    val streams = consumerConnector.createMessageStreams(Map(usersTopic -> 1, tweetsTopic -> 1), keyDecoder, valueDecoder)
    new Thread(new RocksDbUpdater(usersTopic, streams(usersTopic)(0), RocksDbFactory.usersRocksDb)(UserKafkaAvroSerde, UserRocksDbSerde)).start()

    sys.addShutdownHook {
      consumerConnector.shutdown()
    }
  }
}

class RocksDbUpdater[K, V]
  (topic: String, stream: KafkaStream[Object, Object], db: RocksDB)
  (implicit kafkaAvroSerde: KafkaAvroSerde[K, V], rocksDbSerde: RocksDbSerde[K, V]) extends Runnable with Logging {

  override def run(): Unit = {
    log.debug(s"Consuming from $topic...")
    stream foreach { messageAndMetadata => 
      val key = kafkaAvroSerde.keyFromRecord(messageAndMetadata.key.asInstanceOf[GenericRecord])
      val keyBytes = rocksDbSerde.keyToBytes(key)
      val message = messageAndMetadata.message
      if (message == null) {
        db.remove(keyBytes)
      } else {
        val value = kafkaAvroSerde.valueFromRecord(message.asInstanceOf[GenericRecord])
        db.put(keyBytes, rocksDbSerde.valueToBytes(value))
      }
    }
    log.debug(s"Done consuming from $topic")
  }
}
