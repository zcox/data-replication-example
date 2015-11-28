package com.banno

trait RocksDbSerde[K, V] {

  def keyToBytes(key: K): Array[Byte]
  def valueToBytes(value: V): Array[Byte]

  def keyFromBytes(bytes: Array[Byte]): K //is this even needed? when would it ever be used?
  def valueFromBytes(bytes: Array[Byte]): V
}

import scala.pickling.Defaults._
import scala.pickling.binary._
import scala.pickling.shareNothing._
import scala.pickling.static._

object UserRocksDbSerde extends RocksDbSerde[Long, User] {

  override def keyToBytes(key: Long): Array[Byte] = key.pickle.value
  override def valueToBytes(value: User): Array[Byte] = value.pickle.value

  override def keyFromBytes(bytes: Array[Byte]): Long = bytes.unpickle[Long]
  override def valueFromBytes(bytes: Array[Byte]): User = bytes.unpickle[User]
}
