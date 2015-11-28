package com.banno

import java.util.Properties
import io.confluent.kafka.serializers.KafkaAvroDecoder
import kafka.consumer.{Consumer, ConsumerConfig, ConsumerConnector, KafkaStream}
import kafka.utils.VerifiableProperties
import org.apache.avro.generic.GenericRecord
import org.rocksdb.{RocksDB, Options, BlockBasedTableConfig, CompressionType, CompactionStyle}

trait ReplicateIntoRocksDb extends Config {
  lazy val zookeeperConnect = config.getString("api.zookeeper.connect")
  lazy val schemaRegistryHost = config.getString("api.schema-registry.host")
  lazy val schemaRegistryPort = config.getInt("api.schema-registry.port")
  lazy val usersTopic = config.getString("api.kafka.users-topic")
  lazy val tweetsTopic = config.getString("api.kafka.tweets-topic")
  lazy val rocksdbBlockSize = config.getLong("api.rocksdb.block-size")
  lazy val rocksdbBlockCacheSize = config.getLong("api.rocksdb.block-cache-size")
  lazy val usersRocksDbPath = config.getString("api.rocksdb.users-db-path")
  lazy val tweetsRocksDbPath = config.getString("api.rocksdb.tweets-db-path")

  def rocksDbFor(dbPath: String): RocksDB = {
    //TODO mkdir if needed
    val opts = new Options()
    opts.setCompressionType(CompressionType.SNAPPY_COMPRESSION)
    opts.setCompactionStyle(CompactionStyle.UNIVERSAL)
    opts.setMaxWriteBufferNumber(3)
    opts.setCreateIfMissing(true)

    val tableOpts = new BlockBasedTableConfig()
    tableOpts.setBlockSize(rocksdbBlockSize)
    tableOpts.setBlockCacheSize(rocksdbBlockCacheSize)

    opts.setTableFormatConfig(tableOpts)

    val db = RocksDB.open(opts, dbPath)

    sys.addShutdownHook {
      db.close()
      opts.dispose()
    }

    db
  }

  def startReplication(): Unit = {
    val props = new Properties()
    props.put("zookeeper.connect", zookeeperConnect)
    props.put("group.id", "api") //TODO include hostname if there are multiple instances of this application
    props.put("auto.offset.reset", "smallest")
    props.put("dual.commit.enabled", "false")
    props.put("offsets.storage", "kafka")
    props.put("schema.registry.url", s"http://$schemaRegistryHost:$schemaRegistryPort")

    val consumerConnector = Consumer.create(new ConsumerConfig(props))
    val streams = consumerConnector.createMessageStreams(Map(usersTopic -> 1, tweetsTopic -> 1))
    new Thread(new RocksDbUpdater(usersTopic, streams(usersTopic)(0), rocksDbFor(usersRocksDbPath))).start()

    sys.addShutdownHook {
      consumerConnector.shutdown()
    }
  }
}

class RocksDbUpdater(topic: String, stream: KafkaStream[Array[Byte], Array[Byte]], db: RocksDB) extends Runnable with Logging {
  override def run(): Unit = {
    log.debug(s"Consuming from $topic...")
    stream foreach { messageAndMetadata => 
      val key = messageAndMetadata.key
      val message = messageAndMetadata.message
      if (message != null) db.put(key, message)
      else db.remove(key)
      //TODO timer
    }
    log.debug(s"Done consuming from $topic")
  }
}
