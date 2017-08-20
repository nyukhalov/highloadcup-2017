package com.github.nyukhalov.highloadcup.web

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer
import com.github.nyukhalov.highloadcup.core.{AppLogger, HLService}
import com.github.nyukhalov.highloadcup.web.route.{LocationsRoute, UsersRoute, VisitsRoute}

import scala.concurrent.ExecutionContext

class WebServer(serverPort: Int,
                val hlService: HLService)
               (implicit actorSystem: ActorSystem, mat: Materializer, ec: ExecutionContext)
  extends UsersRoute with VisitsRoute with LocationsRoute with AppLogger with HttpServer {

  val route: Route = usersRoute ~ visitsRoute ~ locationsRoute

  def start(): Unit = {
    Http().bind("0.0.0.0", serverPort).runForeach(_.handleWith(Route.handlerFlow(route)))
    logger.info(s"Server started on port $serverPort")
  }
}
