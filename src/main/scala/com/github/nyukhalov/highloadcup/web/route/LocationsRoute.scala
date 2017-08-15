package com.github.nyukhalov.highloadcup.web.route

import akka.actor.Props
import akka.http.scaladsl.server.Directives.{complete, get, path}
import akka.http.scaladsl.server.{Route, RouteResult}
import akka.http.scaladsl.server.Directives._
import com.github.nyukhalov.highloadcup.core.domain.Location
import com.github.nyukhalov.highloadcup.web.actor.{CreateLocationActor, GetLocationAvgRatingActor, GetLocationWithIdActor, UpdateLocationActor}
import com.github.nyukhalov.highloadcup.web.domain._

import scala.concurrent.Promise

trait LocationsRoute extends BaseRoute {
  private val createLocationRoute =
    path("locations" / "new") {
      post {
        decodeRequest {
          entity(as[Location]) {
            location =>
              createLocation {
                location
              }
          }
        }
      }
    }

  private val getLocationRoute =
    path("locations" / IntNumber) { id =>
      get {
        getLocationWithId {
          id
        }
      } ~
        post {
          decodeRequest {
            entity(as[LocationUpdate]) {
              locationUpdate => updateLocation(id, locationUpdate)
            }
          }
        }
    }

  private val getLocationAvgMark =
    path("locations" / IntNumber / "avg") {
      id =>
        get {
          parameters('fromDate.as[Long] ?, 'toDate.as[Long] ?, 'fromAge.as[Int] ?, 'toAge.as[Int] ?, 'gender.as[String] ?) {
            (fromDate, toDate, fromAge, toAge, gender) =>
              getAvarageRating(id, fromDate, toDate, fromAge, toAge, gender)
          }
        }
    }

  val locationsRoute: Route = getLocationRoute ~ getLocationAvgMark ~ createLocationRoute

  def getLocationWithId(id: Int): Route = {
    handleRequest(Props[GetLocationWithIdActor], GetLocationWithId(id))
  }

  def createLocation(location: Location): Route = {
    handleRequest(Props[CreateLocationActor], CreateLocation(location))
  }

  def updateLocation(id: Int, locationUpdate: LocationUpdate): Route = {
    handleRequest(Props[UpdateLocationActor], UpdateLocation(id, locationUpdate))
  }

  def getAvarageRating(id: Int, fromDate: Option[Long], toDate: Option[Long], fromAge: Option[Int], toAge: Option[Int], gender: Option[String]): Route = {
    handleRequest(Props[GetLocationAvgRatingActor], GetLocAvgRating(id, fromDate, toDate, fromAge, toAge, gender))
  }
}
