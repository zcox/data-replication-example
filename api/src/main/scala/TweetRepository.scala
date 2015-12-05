package com.banno

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global //TODO DB operations should be performed on their own ExecutionContext
import org.joda.time.DateTime
import slick.driver.PostgresDriver.api._

trait TweetRepository {

  import SlickDatabase._
  import Tables._

  def getTweet(tweetId: Long): Future[Option[Tweet]] = 
    db.run(Tweets.filter(_.id === tweetId).result.headOption).map(_.map(rowToTweet))

  def createTweet(tweet: NewTweet): Future[Tweet] = 
    db.run((Tweets returning Tweets.map(_.id) into ((tweet,id) => tweet.copy(id=id))) += tweetToRow(tweet)) map rowToTweet

  def createTweet(tweet: Tweet): Future[Tweet] = 
    db.run(Tweets.forceInsert(tweetToRow(tweet))) map { _ => tweet }
}
