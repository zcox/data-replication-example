package com.banno

import org.apache.avro.generic.GenericRecord

trait KafkaAvroSerde[K, V] {
  def keyFromRecord(record: GenericRecord): K
  def valueFromRecord(record: GenericRecord): V
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
