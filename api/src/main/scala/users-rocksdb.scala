package com.banno

import scala.pickling.Defaults._
import scala.pickling.binary._
import scala.pickling.shareNothing._
// import scala.pickling.static._ with this, scala-pickling is unable to generate a pickler for User because it contains a DateTime, which is really lame :(
import org.apache.avro.generic.GenericRecord
import org.joda.time.DateTime

object UsersRocksDb extends Config {
  lazy val rocksDbPath = config.getString("api.rocksdb.users-db-path")
  lazy val rocksDb = RocksDbFactory.rocksDbFor(rocksDbPath)(UserRocksDbSerde)
}

object UserKafkaAvroSerde extends KafkaAvroSerde[Long, User] with AvroUtils {
  override def keyFromRecord(record: GenericRecord): Long = record.get("id").asInstanceOf[Long]
  override def valueFromRecord(record: GenericRecord): User = User(
    record.get("id").asInstanceOf[Long],
    record.get("username").toString,
    Option(record.get("name")).map(_.toString).getOrElse(""),
    Option(record.get("description")).map(_.toString).getOrElse(""),
    Option(record.get("image_url")).map(_.toString).getOrElse(""),
    new DateTime(record.get("created_at").asInstanceOf[Long]),
    new DateTime(record.get("updated_at").asInstanceOf[Long]))
  override def keyFromValue(user: User): Long = user.id
}

object UserRocksDbSerde extends RocksDbSerde[Long, User] {

  override def keyToBytes(key: Long): Array[Byte] = key.pickle.value
  override def valueToBytes(user: User): Array[Byte] = user.pickle.value

  override def keyFromBytes(bytes: Array[Byte]): Long = bytes.unpickle[Long]
  override def valueFromBytes(bytes: Array[Byte]): User = bytes.unpickle[User].withUtc //scala-pickling apparently can't deserialize the DateTimeZone... :/
}
