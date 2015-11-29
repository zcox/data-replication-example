package com.banno

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global //TODO DB operations should be performed on their own ExecutionContext
import org.joda.time.DateTime

trait TweetRepository extends Database {

  def getTweet(tweetId: Long): Future[Tweet] = getTweetFromDatabase(tweetId)

  def getTweetFromDatabase(tweetId: Long): Future[Tweet] = Future {
    queryOne(s"SELECT * FROM tweets WHERE id=$tweetId") { results => 
      Tweet(
        results.getLong("id"), 
        results.getString("content"), 
        results.getLong("user_id"), 
        new DateTime(results.getTimestamp("created_at")), 
        Option(results.getDouble("latitude")), 
        Option(results.getDouble("longitude")))
    }
  }

  def createTweet(tweet: Tweet): Future[Tweet] = Future {
    import tweet._
    update(s"INSERT INTO tweets (id, text, user_id, created_at, latitude, longitude) VALUES ($id, '$text', $userId, '$createdAt', ${latitude.getOrElse(null)}, ${longitude.getOrElse(null)})")
    tweet
  }

  def createTweet(tweet: NewTweet): Future[Tweet] = Future {
    import tweet._
    insertAndGetGeneratedId(s"INSERT INTO tweets (content, user_id, created_at, latitude, longitude) VALUES ('$text', $userId, '${new DateTime}', ${latitude.getOrElse(null)}, ${longitude.getOrElse(null)})")
  } flatMap getTweetFromDatabase
}
