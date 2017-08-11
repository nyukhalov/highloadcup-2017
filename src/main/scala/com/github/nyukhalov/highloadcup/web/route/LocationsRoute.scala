package com.github.nyukhalov.highloadcup.web.route

import akka.actor.Props
import akka.http.scaladsl.server.Directives.{complete, get, path}
import akka.http.scaladsl.server.{Route, RouteResult}
import akka.http.scaladsl.server.Directives._
import com.github.nyukhalov.highloadcup.web.actor.GetLocationWithIdActor
import com.github.nyukhalov.highloadcup.web.domain.GetLocationWithId

import scala.concurrent.Promise

trait LocationsRoute extends BaseRoute {
  private val getLocationRoute =
    path("locations" / IntNumber) {
      id => get {
        getLocationWithId {
          id
        }
      }
    }

  private val getLocationAvgMark =
    path("locations" / IntNumber / "avg") {
      id => get {
        complete("5")
      }
    }

  def getLocationWithId(id: Int): Route = ctx => {
    val p = Promise[RouteResult]
    implicit val actorSystem = as
    perRequest(ctx, Props(classOf[GetLocationWithIdActor]), GetLocationWithId(id), p)
    p.future
  }

  val locationsRoute: Route = getLocationRoute ~ getLocationAvgMark
}
