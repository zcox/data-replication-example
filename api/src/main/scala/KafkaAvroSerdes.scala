package com.banno

import io.confluent.kafka.serializers.{KafkaAvroSerializer, KafkaAvroDecoder}
import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient
import scala.collection.JavaConversions._
import org.apache.avro.Schema
import org.apache.avro.generic.{GenericRecord, GenericData}

/** If the messages from Bottled Water topics are written directly to RocksDB, these are the hoops you must jump through to:
  *   1) serialize the ID into key bytes to do the RocksDB lookup
  *   2) deserialize the value obtained from RocksDB
  * It's probably simpler to deserialize the message from the BW topic in the Kafka consumer to a GenericRecord, and then 
  * convert that to your own key & value bytes to put into RocksDB. Then use that same serde when you do the RocksDB lookup.
  * That way you don't need to involve the Schema Registry or use the KafkaAvroSerializer/KafkaAvroDecoder.
  */
trait KafkaAvroSerdes extends KafkaConfig {
  //these details were obtained from KafkaAvroSerializer
  val maxSchemasPerSubject = 1000
  lazy val schemaRegistry = new CachedSchemaRegistryClient(schemaRegistryUrl, maxSchemasPerSubject)
  def schema(subject: String): Schema = schemaRegistry.getByID(schemaRegistry.getLatestSchemaMetadata(subject).getId)
  def keySubject(topic: String): String = s"$topic-key"
  def keySchema(topic: String): Schema = schema(keySubject(topic))
  lazy val keySerializer: KafkaAvroSerializer = {
    val s = new KafkaAvroSerializer()
    val props = Map("schema.registry.url" -> schemaRegistryUrl)
    s.configure(props, true)
    s
  }

  lazy val usersKeySchema: Schema = keySchema(usersTopic)
  def usersKeyRecord(userId: Long): GenericRecord = {
    val record = new GenericData.Record(usersKeySchema)
    record.put("id", userId)
    record
  }
  def usersKeyBytes(userId: Long): Array[Byte] = keySerializer.serialize(usersTopic, usersKeyRecord(userId))

  lazy val decoder = new KafkaAvroDecoder(schemaRegistry)
  def recordFrom(bytes: Array[Byte]): GenericRecord = decoder.fromBytes(bytes).asInstanceOf[GenericRecord]
  def userFrom(record: GenericRecord): User = User(
    record.get("id").asInstanceOf[Long],
    record.get("username").toString,
    Option(record.get("name")).map(_.toString).getOrElse(""),
    Option(record.get("description")).map(_.toString).getOrElse(""),
    Option(record.get("imageUrl")).map(_.toString).getOrElse(""))
  def userFrom(bytes: Array[Byte]): User = userFrom(recordFrom(bytes))
}
