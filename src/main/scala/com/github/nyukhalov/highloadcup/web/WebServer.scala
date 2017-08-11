package com.github.nyukhalov.highloadcup.web

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer
import com.typesafe.scalalogging.Logger
import com.github.nyukhalov.highloadcup.web.route.{LocationsRoute, UsersRoute, VisitsRoute}

import scala.concurrent.ExecutionContext

class WebServer(implicit actorSystem: ActorSystem, mat: Materializer, ec: ExecutionContext, logger: Logger)
  extends UsersRoute with VisitsRoute with LocationsRoute {

  override def as: ActorSystem = implicitly

  val route: Route = usersRoute ~ visitsRoute ~ locationsRoute

  def start(): Unit = {
    val port = 80
    Http().bind("0.0.0.0", port).runForeach(_.handleWith(Route.handlerFlow(route)))
    logger.info(s"Server started on port $port")
  }
}
