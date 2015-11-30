package com.banno

import org.apache.avro.generic.GenericRecord

trait KafkaAvroSerde[K, V] {
  def keyFromRecord(record: GenericRecord): K
  def valueFromRecord(record: GenericRecord): V
}
