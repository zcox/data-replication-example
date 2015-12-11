package com.banno

import org.apache.avro.generic.GenericRecord

trait KafkaAvroSerde[K, V] {
  def keyFromRecord(record: GenericRecord): K
  def valueFromRecord(record: GenericRecord): V

  def keyFromValue(value: V): K //have to have this because of https://github.com/confluentinc/kafka-connect-jdbc/issues/28
}
