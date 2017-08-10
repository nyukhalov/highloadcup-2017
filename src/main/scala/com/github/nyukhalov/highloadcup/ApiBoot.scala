package com.github.nyukhalov.highloadcup

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.github.nyukhalov.highloadcup.web.WebServer
import com.typesafe.scalalogging.Logger

import scala.concurrent.ExecutionContext

object ApiBoot {
  def run(implicit actorSystem: ActorSystem, mat: Materializer, ec: ExecutionContext, logger: Logger): Unit = {
    new WebServer().start()
  }
}
