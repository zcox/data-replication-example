package com.banno

import twitter4j.{StatusListener, Status, StatusDeletionNotice, StallWarning, TwitterStreamFactory}
import twitter4j.conf.ConfigurationBuilder
import org.joda.time.DateTime

object TwitterStreamingMain
    extends App 
    with StatusListener 
    with UserRepository
    with TweetRepository
    with Config 
    with Logging {

  println(SlickDatabase.db) //warm up Slick

  Metrics.reportToGraphite()

  lazy val twitter4jConfiguration = new ConfigurationBuilder()
    .setOAuthConsumerKey(config.getString("twitter.oauth.consumer-key"))
    .setOAuthConsumerSecret(config.getString("twitter.oauth.consumer-secret"))
    .setOAuthAccessToken(config.getString("twitter.oauth.access-token"))
    .setOAuthAccessTokenSecret(config.getString("twitter.oauth.access-token-secret"))
    .build()

  val stream = new TwitterStreamFactory(twitter4jConfiguration).getInstance
  stream.addListener(this)
  stream.sample()

  def toUser(status: Status): User = User(
    status.getUser.getId,
    status.getUser.getScreenName,
    status.getUser.getName,
    status.getUser.getDescription,
    status.getUser.getOriginalProfileImageURL)

  def toTweet(status: Status): Tweet = Tweet(
    status.getId,
    status.getText,
    status.getUser.getId,
    new DateTime(status.getCreatedAt),
    Option(status.getGeoLocation).map(_.getLatitude),
    Option(status.getGeoLocation).map(_.getLongitude))

  def valid(user: User, tweet: Tweet): Boolean = 
    user.id > 0 && user.username != null && user.username.trim.length > 0 && tweet.id > 0 && tweet.userId > 0

  override def onStatus(status: Status): Unit = {
    import scala.concurrent.ExecutionContext.Implicits.global
    val user = toUser(status)
    val tweet = toTweet(status)
    if (valid(user, tweet)) { //filter out any bad data from Twitter
      createOrUpdateUser(user) onFailure { case throwable => log.error(s"Error writing $user", throwable) }
      createTweet(tweet) onFailure { case throwable => log.error(s"Error writing $tweet", throwable) }
    }
  }

  override def onDeletionNotice(statusDeletionNotice: StatusDeletionNotice): Unit = {} //TODO impl this too!

  override def onTrackLimitationNotice(numberOfLimitedStatuses: Int): Unit = {}
  override def onException(ex: Exception): Unit = { ex.printStackTrace }
  override def onScrubGeo(arg0: Long, arg1: Long): Unit = {}
  override def onStallWarning(p1: StallWarning): Unit = {}
}
