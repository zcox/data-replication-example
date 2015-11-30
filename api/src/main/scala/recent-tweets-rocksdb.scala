package com.banno

import scala.pickling._
import scala.pickling.Defaults._
import scala.pickling.binary._
import scala.pickling.shareNothing._
// import scala.pickling.static._ with this, scala-pickling seems unable to serialize any collection type like Seq, List, Array, Vector
import org.apache.avro.generic.GenericRecord
import org.joda.time.{DateTime, DateTimeZone}

object RecentTweets extends Config {
  lazy val rocksDbPath = config.getString("api.rocksdb.recent-tweets-db-path")
  lazy val rocksDb = RocksDbFactory.rocksDbFor(rocksDbPath)(RecentTweetsRocksDbSerde)
}

object TweetKafkaAvroSerde extends KafkaAvroSerde[Long, Tweet] {
  override def keyFromRecord(record: GenericRecord): Long = record.get("id").asInstanceOf[Long]
  override def valueFromRecord(record: GenericRecord): Tweet = 
    Tweet(
      record.get("id").asInstanceOf[Long],
      record.get("content").toString,
      record.get("user_id").asInstanceOf[Long],
      toDateTime(record.get("created_at").asInstanceOf[GenericRecord]),
      Option(record.get("latitude")).map(_.asInstanceOf[Double]),
      Option(record.get("longitude")).map(_.asInstanceOf[Double]))

  //class = org.apache.avro.generic.GenericData$Record, toString = {"year": 2015, "month": 11, "day": 29, "hour": 20, "minute": 5, "second": 4, "micro": 812000}
  def toDateTime(record: GenericRecord): DateTime = new DateTime(
    record.get("year").asInstanceOf[Int],
    record.get("month").asInstanceOf[Int],
    record.get("day").asInstanceOf[Int],
    record.get("hour").asInstanceOf[Int],
    record.get("minute").asInstanceOf[Int],
    record.get("second").asInstanceOf[Int],
    record.get("micro").asInstanceOf[Int] / 1000).withZone(DateTimeZone.UTC)
}

object RecentTweetsRocksDbSerde extends RocksDbSerde[Long, List[Tweet]] {

  override def keyToBytes(userId: Long): Array[Byte] = userId.pickle.value
  override def valueToBytes(tweets: List[Tweet]): Array[Byte] = tweets.pickle.value

  override def keyFromBytes(bytes: Array[Byte]): Long = bytes.unpickle[Long]
  override def valueFromBytes(bytes: Array[Byte]): List[Tweet] = bytes.unpickle[List[Tweet]].map(_.withUtc) //scala-pickling apparently can't deserialize the DateTimeZone... :/
}

object RecentTweetsHandler extends DatabaseChangeHandler[Long, Tweet] {
  val db = RecentTweets.rocksDb
  val maxSize = 10

  override def rowChanged(tweetId: Long, tweet: Tweet): Unit = {
    val tweets = (tweet +: db.get(tweet.userId).getOrElse(List.empty)) take maxSize
    db.put(tweet.userId, tweets)
  }

  override def rowDeleted(tweetId: Long): Unit = ???
}
