package com.banno

import akka.http.scaladsl.server.Directives._

trait UserRoutes extends UserRepository with JsonSupport {

  val usersRoute = pathPrefix("users") {
    path(LongNumber) { userId => 
      get {
        complete { 
          getUser(userId) 
        }
      } ~ 
      put { 
        entity(as[User]) { user => 
          complete { 
            updateUser(user) 
          } 
        }
      }
    } ~ 
    get { 
      complete { 
        "TODO return some information about all users"
      }
    } ~ 
    post { 
      entity(as[NewUser]) { user => 
        complete { 
          createUser(user)
        }
      }
    }
  }
}
