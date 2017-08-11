package com.github.nyukhalov.highloadcup.web.route

import akka.actor.Props
import akka.http.scaladsl.server.Directives.{complete, get, path}
import akka.http.scaladsl.server.{Route, RouteResult}
import akka.http.scaladsl.server.Directives._
import com.github.nyukhalov.highloadcup.core.domain.Location
import com.github.nyukhalov.highloadcup.web.actor.{CreateLocationActor, GetLocationWithIdActor}
import com.github.nyukhalov.highloadcup.web.domain.{CreateLocation, GetLocationWithId}

import scala.concurrent.Promise

trait LocationsRoute extends BaseRoute {
  private val createLocationRoute =
    path("locations" / "new") {
      post {
        decodeRequest {
          entity(as[Location]) {
            location => createLocation { location }
          }
        }
      }
    }

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

  val locationsRoute: Route = getLocationRoute ~ getLocationAvgMark ~ createLocationRoute

  def getLocationWithId(id: Int): Route = ctx => {
    val p = Promise[RouteResult]
    implicit val actorSystem = actorSys
    perRequest(ctx, Props(classOf[GetLocationWithIdActor], entityRepository), GetLocationWithId(id), p)
    p.future
  }

  def createLocation(location: Location): Route = ctx => {
    val p = Promise[RouteResult]
    implicit val actorSystem = actorSys
    perRequest(ctx, Props(classOf[CreateLocationActor], entityRepository), CreateLocation(location), p)
    p.future
  }
}
