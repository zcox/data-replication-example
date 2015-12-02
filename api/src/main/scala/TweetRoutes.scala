package com.banno

import akka.http.scaladsl.server.Directives._

trait TweetRoutes extends TweetRepository with JsonSupport {

  val tweetsRoute = pathPrefix("tweets") {
    path(LongNumber) { tweetId => 
      get { 
        complete { 
          getTweet(tweetId) 
        } 
      }
    } ~ 
    get { 
      complete { 
        "TODO return some information about all tweets"
      }
    } ~ 
    post { 
      entity(as[NewTweet]) { tweet => 
        complete { 
          createTweet(tweet)
        }
      }
    }
  }
}
