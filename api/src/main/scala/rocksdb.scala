package com.banno

import org.rocksdb.{RocksDB, Options, BlockBasedTableConfig, CompressionType, CompactionStyle}

object RocksDbFactory extends Config {
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

  lazy val usersRocksDb = rocksDbFor(usersRocksDbPath)
  lazy val tweetsRocksDb = rocksDbFor(tweetsRocksDbPath)
}
