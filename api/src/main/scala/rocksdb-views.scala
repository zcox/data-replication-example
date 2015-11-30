package com.banno

trait ReplicateIntoRocksDb extends KafkaConsumer {

  def startRocksDbReplication(): Unit = {
    val streams = getStreams("api-rocksdb")

    val usersStream = streams(usersTopic)(0)
    val usersHandler = new ReplicateDatabaseRowsIntoRocksDb(UsersRocksDb.rocksDb)
    val usersConsumer = new DatabaseChangeConsumer(usersTopic, usersStream, usersHandler)(UserKafkaAvroSerde)
    new Thread(usersConsumer).start()

    val tweetsStream = streams(tweetsTopic)(0)
    val tweetsConsumer = new DatabaseChangeConsumer(tweetsTopic, tweetsStream, RecentTweetsHandler)(TweetKafkaAvroSerde)
    new Thread(tweetsConsumer).start()
  }
}

class ReplicateDatabaseRowsIntoRocksDb[K, V](db: TypedRocksDB[K, V]) extends DatabaseChangeHandler[K, V] {
  override def rowChanged(key: K, value: V): Unit = db.put(key, value)
  override def rowDeleted(key: K): Unit = db.remove(key)
}
