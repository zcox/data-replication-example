package com.banno

import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._

trait HttpServer
    extends UserRoutes 
    with TweetRoutes
    with SearchRoutes
    with ActorModule
    with ApiConfig 
    with Logging {

  val apiRoute = pathPrefix("api") { usersRoute ~ tweetsRoute ~ searchRoute }

  val pingRoute = path("ping") { get { complete { "pong" } } }

  val route = apiRoute ~ pingRoute

  def startHttpServer() = {
    import actorSystem.dispatcher
    log.debug(s"Starting HTTP server on $httpServerHost:$httpServerPort...")
    val bindFuture = Http().bindAndHandle(route, httpServerHost, httpServerPort)
    bindFuture onSuccess { case _ => log.debug(s"Started HTTP server on $httpServerHost:$httpServerPort") }
    bindFuture
  }
}
