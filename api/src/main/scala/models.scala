package com.banno

import spray.json._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import org.joda.time.DateTime
import org.joda.time.format.ISODateTimeFormat

final case class User(
  id: Long,
  username: String,
  name: String,
  description: String,
  imageUrl: String) {
  def toNewUser = NewUser(username, name, description, imageUrl)
}

final case class NewUser(
  username: String,
  name: String,
  description: String,
  imageUrl: String)

final case class Tweet(
  id: Long,
  text: String,
  userId: Long,
  createdAt: DateTime,
  latitude: Option[Double],
  longitude: Option[Double])

trait JsonSupport extends SprayJsonSupport with DefaultJsonProtocol {

  implicit object DateTimeFormat extends JsonFormat[DateTime] {
    val formatter = ISODateTimeFormat.basicDateTimeNoMillis
    def write(time: DateTime): JsValue = JsString(formatter.print(time))
    def read(json: JsValue): DateTime = json match {
      case JsString(s) => formatter.parseDateTime(s)
      case _ => throw new DeserializationException("DateTime expected")
    }
  }

  implicit val userFormat = jsonFormat5(User)
  implicit val newUserFormat = jsonFormat4(NewUser)
  implicit val tweetFormat = jsonFormat6(Tweet)
}
