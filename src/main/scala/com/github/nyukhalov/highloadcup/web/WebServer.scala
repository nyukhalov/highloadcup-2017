package com.github.nyukhalov.highloadcup.web

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.{Route, RouteResult}
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer
import com.typesafe.scalalogging.Logger
import com.github.nyukhalov.highloadcup.web.actor.{GetLocationWithIdActor, GetUserWithIdActor, GetVisitWithIdActor, PerRequestCreator}
import com.github.nyukhalov.highloadcup.web.domain.{GetLocationWithId, GetUserWithId, GetVisitWithId}
import com.github.nyukhalov.highloadcup.web.json.JsonSupport

import scala.concurrent.{ExecutionContext, Promise}

class WebServer(implicit actorSystem: ActorSystem, mat: Materializer, ec: ExecutionContext, logger: Logger)
  extends PerRequestCreator with JsonSupport {

  val getUserRoute =
    path("users" / IntNumber) {
      id => get {
        getUserWithId {
          id
        }
      }
    }

  val getVisitRoute =
    path("visits" / IntNumber) {
      id => get {
        getVisitWithId {
          id
        }
      }
    }

  val getLocationRoute =
    path("locations" / IntNumber) {
      id => get {
        getLocationWithId {
          id
        }
      }
    }

  val route: Route = getUserRoute ~ getVisitRoute ~ getLocationRoute

  def getUserWithId(id: Int): Route = ctx => {
    val p = Promise[RouteResult]
    perRequest(ctx, Props(classOf[GetUserWithIdActor]), GetUserWithId(id), p)
    p.future
  }

  def getVisitWithId(id: Int): Route = ctx => {
    val p = Promise[RouteResult]
    perRequest(ctx, Props(classOf[GetVisitWithIdActor]), GetVisitWithId(id), p)
    p.future
  }

  def getLocationWithId(id: Int): Route = ctx => {
    val p = Promise[RouteResult]
    perRequest(ctx, Props(classOf[GetLocationWithIdActor]), GetLocationWithId(id), p)
    p.future
  }

  def start(): Unit = {
    Http().bind("localhost", 8080).runForeach(_.handleWith(Route.handlerFlow(route)))
    logger.info(s"Server started")
  }
}
