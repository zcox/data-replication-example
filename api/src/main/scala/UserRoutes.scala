package com.banno

import akka.http.scaladsl.server.Directives._

trait UserRoutes extends UserRepository with JsonSupport {

  val userRoute = path(LongNumber) { userId => 
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
      } ~ 
      entity(as[NewUser]) { user => 
        complete {
          createUser(userId, user)
        }
      }
    }
  }

  val usersRoute = pathPrefix("users") {
    userRoute ~ 
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
