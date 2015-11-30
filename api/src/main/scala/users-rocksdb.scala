package com.banno

import scala.pickling.Defaults._
import scala.pickling.binary._
import scala.pickling.shareNothing._
import scala.pickling.static._
import org.apache.avro.generic.GenericRecord

object UsersRocksDb extends Config {
  lazy val rocksDbPath = config.getString("api.rocksdb.users-db-path")
  lazy val rocksDb = RocksDbFactory.rocksDbFor(rocksDbPath)(UserRocksDbSerde)
}

object UserKafkaAvroSerde extends KafkaAvroSerde[Long, User] {
  override def keyFromRecord(record: GenericRecord): Long = record.get("id").asInstanceOf[Long]
  override def valueFromRecord(record: GenericRecord): User = User(
    record.get("id").asInstanceOf[Long],
    record.get("username").toString,
    Option(record.get("name")).map(_.toString).getOrElse(""),
    Option(record.get("description")).map(_.toString).getOrElse(""),
    Option(record.get("imageUrl")).map(_.toString).getOrElse(""))
}

object UserRocksDbSerde extends RocksDbSerde[Long, User] {

  override def keyToBytes(key: Long): Array[Byte] = key.pickle.value
  override def valueToBytes(value: User): Array[Byte] = value.pickle.value

  override def keyFromBytes(bytes: Array[Byte]): Long = bytes.unpickle[Long]
  override def valueFromBytes(bytes: Array[Byte]): User = bytes.unpickle[User]
}
