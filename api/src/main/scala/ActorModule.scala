package com.banno

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer

object ActorModule {
  implicit val actorSystem = ActorSystem("api")
  val actorMaterializer = ActorMaterializer()
}

trait ActorModule {
  implicit val actorSystem = ActorModule.actorSystem
  implicit val actorMaterializer = ActorModule.actorMaterializer
}
