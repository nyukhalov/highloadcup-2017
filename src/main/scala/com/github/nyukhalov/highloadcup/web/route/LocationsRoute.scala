package com.github.nyukhalov.highloadcup.web.route

import akka.http.scaladsl.server.Directives.{complete, get, path}
import akka.http.scaladsl.server.{Route, RouteResult}
import akka.http.scaladsl.server.Directives._
import com.github.nyukhalov.highloadcup.core.domain.{Location, LocationV, UserV}
import com.github.nyukhalov.highloadcup.web.domain._
import org.joda.time.{DateTime, DateTimeZone}

import scala.concurrent.{Future, Promise}

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

  def getLocationWithId(id: Int): Route = ctx => {
    val r = hlService.getLocation(id)
    ctx.complete(t(r))
  }

  def createLocation(location: Location): Route = ctx => {
    val r = hlService.createLocation(location)
    ctx.complete(t(r))
  }

  def updateLocation(id: Int, locationUpdate: LocationUpdate): Route = ctx => {
    val r = hlService.updateLocation(id, locationUpdate)
    ctx.complete(t(r))
  }

  def getAvarageRating(id: Int, fromDate: Option[Long], toDate: Option[Long], fromAge: Option[Int], toAge: Option[Int], gender: Option[String]): Route = ctx => {
    val r = hlService.getAverageRating(id, fromDate, toDate, fromAge, toAge, gender)
    ctx.complete(t(r))
  }
}
