package com.banno

import akka.http.scaladsl.server.Directives._

trait SearchRoutes extends SearchService with JsonSupport {

  val searchRoute = path("search") {
    get {
      parameter('q) { query => 
        complete {
          search(query)
        }
      }
    }
  }
}
