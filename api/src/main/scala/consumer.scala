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

    val consumerConnector = Consumer.create(new ConsumerConfig(props))
    val streams = consumerConnector.createMessageStreams(Map(usersTopic -> 1, tweetsTopic -> 1))
    new Thread(new RocksDbUpdater(usersTopic, streams(usersTopic)(0), RocksDbFactory.usersRocksDb)).start()

    sys.addShutdownHook {
      consumerConnector.shutdown()
    }
  }
}

class RocksDbUpdater(topic: String, stream: KafkaStream[Array[Byte], Array[Byte]], db: RocksDB) extends Runnable with Logging {
  override def run(): Unit = {
    log.debug(s"Consuming from $topic...")
    stream foreach { messageAndMetadata => 
      //TODO it's probably better to convert the key record into something that UserRepository can easily do Long => Array[Byte] to do the lookup
      //UserRepository still needs to convert Array[Byte] => GenericRecord though...
      val key = messageAndMetadata.key
      val message = messageAndMetadata.message
      if (message != null) db.put(key, message)
      else db.remove(key)
      //TODO timer
    }
    log.debug(s"Done consuming from $topic")
  }
}
