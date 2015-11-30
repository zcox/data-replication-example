package com.banno

trait RocksDbSerde[K, V] {

  def keyToBytes(key: K): Array[Byte]
  def valueToBytes(value: V): Array[Byte]

  def keyFromBytes(bytes: Array[Byte]): K //is this even needed? when would it ever be used?
  def valueFromBytes(bytes: Array[Byte]): V
}
