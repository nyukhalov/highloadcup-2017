package com.github.nyukhalov.highloadcup

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import com.github.nyukhalov.highloadcup.core.AppLogger

import scala.concurrent.duration._
import scala.concurrent.Await

object Boot extends App with AppLogger {

  implicit val system = ActorSystem("highloadcup-actor-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  implicit val l = logger

  ApiBoot.run

  sys.addShutdownHook(() => {
    val future = system.terminate()
    Await.result(future, 120.seconds)
  })
}
