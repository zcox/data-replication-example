package com.banno

import org.rocksdb.{RocksDB, Options, BlockBasedTableConfig, CompressionType, CompactionStyle}

class TypedRocksDB[K, V](db: RocksDB, timerName: String)(implicit serde: RocksDbSerde[K, V]) extends Instrumented {

  lazy val getTimer = metrics.timer(s"$timerName.get")
  def get(key: K): Option[V] = getTimer.time { 
    Option(db.get(serde.keyToBytes(key))).map(serde.valueFromBytes)
  }

  lazy val putTimer = metrics.timer(s"$timerName.put")
  def put(key: K, value: V): Unit = putTimer.time { 
    db.put(serde.keyToBytes(key), serde.valueToBytes(value))
  }

  lazy val removeTimer = metrics.timer(s"$timerName.remove")
  def remove(key: K): Unit = removeTimer.time { 
    db.remove(serde.keyToBytes(key))
  }
}

object RocksDbFactory extends Config {
  lazy val rocksdbBlockSize = config.getLong("api.rocksdb.block-size")
  lazy val rocksdbBlockCacheSize = config.getLong("api.rocksdb.block-cache-size")
  lazy val usersRocksDbPath = config.getString("api.rocksdb.users-db-path")

  def rocksDbFor[K, V](dbPath: String)(implicit serde: RocksDbSerde[K, V]): TypedRocksDB[K, V] = {
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

    new TypedRocksDB(db, timerName(dbPath))
  }

  def timerName(dbPath: String): String = {
    val i = dbPath.lastIndexOf('/')
    if (i >= 0) dbPath.substring(i) else dbPath
  }

  lazy val usersRocksDb = rocksDbFor(usersRocksDbPath)(UserRocksDbSerde)
}
