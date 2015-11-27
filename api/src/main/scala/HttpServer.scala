package com.banno

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._

trait HttpServer
    extends UserRoutes 
    with ApiConfig 
    with Logging {

  //could refactor this out into an ActorModule trait if this stuff is also needed elsewhere
  implicit val system = ActorSystem("my-system")
  import system.dispatcher
  implicit val materializer = ActorMaterializer()

  val apiRoute = pathPrefix("api") { usersRoute }

  val pingRoute = path("ping") { get { complete { "pong" } } }

  val route = apiRoute ~ pingRoute

  def startHttpServer() = {
    log.debug(s"Starting HTTP server on $httpServerHost:$httpServerPort...")
    val bindFuture = Http().bindAndHandle(route, httpServerHost, httpServerPort)
    bindFuture onSuccess { case _ => log.debug(s"Started HTTP server on $httpServerHost:$httpServerPort") }
    bindFuture
  }
}
