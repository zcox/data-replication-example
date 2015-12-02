package com.banno

import slick.driver.PostgresDriver.api._
import org.joda.time.DateTime
import java.sql.Timestamp

object SlickDatabase {
  val db = Database.forURL("jdbc:postgresql://192.168.59.103:5432/postgres?user=example&password=example", driver = "org.postgresql.Driver")

  import Tables._

  def userToRow(user: User): UsersRow = UsersRow(
    id = user.id,
    username = user.username,
    name = Option(user.name),
    description = Option(user.description),
    imageUrl = Option(user.imageUrl))

  def userToRow(user: NewUser): UsersRow = UsersRow(
    id = 0, //Slick will use auto-generated primary key on insert
    username = user.username,
    name = Option(user.name),
    description = Option(user.description),
    imageUrl = Option(user.imageUrl))

  def rowToUser(row: UsersRow): User = User(
    id = row.id,
    username = row.username,
    name = row.name getOrElse "",
    description = row.description getOrElse "",
    imageUrl = row.imageUrl getOrElse "")

  def rowToTweet(row: TweetsRow): Tweet = Tweet(
    id = row.id,
    text = row.content getOrElse "",
    userId = row.userId,
    createdAt = row.createdAt.map(t => new DateTime(t)) getOrElse null,
    latitude = row.latitude,
    longitude = row.longitude)

  def tweetToRow(tweet: NewTweet): TweetsRow = TweetsRow(
    id = 0, //Slick will use auto-generated primary key on insert
    content = Option(tweet.text),
    createdAt = Some(new Timestamp(System.currentTimeMillis)),
    latitude = tweet.latitude,
    longitude = tweet.longitude,
    userId = tweet.userId)

  def tweetToRow(tweet: Tweet): TweetsRow = TweetsRow(
    id = tweet.id,
    content = Option(tweet.text),
    createdAt = Option(tweet.createdAt).map(dt => new Timestamp(dt.getMillis)),
    latitude = tweet.latitude,
    longitude = tweet.longitude,
    userId = tweet.userId)
}
