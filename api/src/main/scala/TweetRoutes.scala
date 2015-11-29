package com.banno

import akka.http.scaladsl.server.Directives._

trait TweetRoutes extends TweetRepository with JsonSupport {

  val tweetRoute = path(LongNumber) { tweetId => 
    get { 
      complete { 
        getTweet(tweetId) 
      } 
    } ~ 
    put { 
      entity(as[Tweet]) { tweet => 
        complete { 
          createTweet(tweet) 
        } 
      }
    }
  }

  val tweetsRoute = pathPrefix("tweets") {
    tweetRoute ~ 
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
