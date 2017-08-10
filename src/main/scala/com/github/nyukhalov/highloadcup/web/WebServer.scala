package com.github.nyukhalov.highloadcup.web

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.{Route, RouteResult}
import akka.stream.Materializer
import com.typesafe.scalalogging.Logger
import akka.http.scaladsl.server.Directives._
import com.github.nyukhalov.highloadcup.web.actor.{GetUserWithIdActor, PerRequestCreator}
import com.github.nyukhalov.highloadcup.web.domain.GetUserWithId
import com.github.nyukhalov.highloadcup.web.json.JsonSupport

import scala.concurrent.{ExecutionContext, Promise}

class WebServer(implicit actorSystem: ActorSystem, mat: Materializer, ec: ExecutionContext, logger: Logger)
  extends PerRequestCreator with JsonSupport {

  val route: Route =
    path("users") {
      get {
        getUserWithId {
          1
        }
      }
    }

  def getUserWithId(id: Int): Route = ctx => {
    val p = Promise[RouteResult]
    perRequest(ctx, Props(classOf[GetUserWithIdActor]), GetUserWithId(id), p)
    p.future
  }

  def start(): Unit = {
    Http().bind("localhost", 8080).runForeach(_.handleWith(Route.handlerFlow(route)))
    logger.info(s"Server started")
  }
}
