package com.github.nyukhalov.highloadcup.web.route

import akka.http.scaladsl.server.Directives.{complete, get, path}
import akka.http.scaladsl.server.{Route, RouteResult}
import akka.http.scaladsl.server.Directives._
import com.github.nyukhalov.highloadcup.core.domain.{Location, LocationV, UserV}
import com.github.nyukhalov.highloadcup.database.DB
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
    import scala.concurrent.ExecutionContext.Implicits.global

    DB.findLocationById(id).map {
      case Some(location) => location
      case None => NotExist(s"Location with id $id does not exist")
    }.flatMap(x => ctx.complete(t(x)))
  }

  def createLocation(location: Location): Route = ctx => {
    import scala.concurrent.ExecutionContext.Implicits.global

    if (!LocationV.isValid(location)) {
      ctx.complete(t(Validation("Invalid location")))
    } else {
      DB.findLocationById(location.id).flatMap {
        case Some(_) => Future.successful(Validation(s"Location with id ${location.id} already exists"))
        case None => DB.insertLocation(location).map(_ => SuccessfulOperation)
      }.flatMap(x => ctx.complete(t(x)))
    }
  }

  private def isValidUpdate(lu: LocationUpdate) = {
    if (lu.place.isDefined && !LocationV.isValidPlace(lu.place.get)) false
    else if (lu.country.isDefined && !LocationV.isValidCountry(lu.country.get)) false
    else if (lu.city.isDefined && !LocationV.isValidCity(lu.city.get)) false
    else if (lu.distance.isDefined && !LocationV.isValidDistance(lu.distance.get)) false
    else true
  }

  def updateLocation(id: Int, locationUpdate: LocationUpdate): Route = ctx => {
    import scala.concurrent.ExecutionContext.Implicits.global

    if (!isValidUpdate(locationUpdate)) {
      ctx.complete(t(Validation("Invalid location update")))
    } else {

      DB.findLocationById(id).flatMap {
        case None => Future.successful(NotExist(s"Location with id $id does not exist"))

        case Some(l) =>
          val updatedLocation = Location(
            id,
            locationUpdate.place.getOrElse(l.place),
            locationUpdate.country.getOrElse(l.country),
            locationUpdate.city.getOrElse(l.city),
            locationUpdate.distance.getOrElse(l.distance)
          )

          DB.updateLocation(updatedLocation).map(_ => SuccessfulOperation)
      }.flatMap(x => ctx.complete(t(x)))
    }
  }

  private def isValidParams(fromDate: Option[Long], toDate: Option[Long],
                            fromAge: Option[Int], toAge: Option[Int], gender: Option[String]): Boolean = {
    fromAge.getOrElse(1) > 0 &&
      toAge.getOrElse(1) > 0 &&
      UserV.isValidGender(gender.getOrElse("m"))
  }

  def getAvarageRating(id: Int, fromDate: Option[Long], toDate: Option[Long], fromAge: Option[Int], toAge: Option[Int], gender: Option[String]): Route = ctx => {
    import scala.concurrent.ExecutionContext.Implicits.global

    if (!isValidParams(fromDate, toDate, fromAge, toAge, gender)) {
      ctx.complete(t(Validation("Invalid params")))
    } else {

      val now = DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay()

      val fromBirthDate = toAge.map(ta => now.minusYears(ta).getMillis / 1000)
      val toBirthDate = fromAge.map(fa => now.minusYears(fa).getMillis / 1000)

      DB.findLocationById(id).flatMap {
        case Some(_) =>
          DB.getLocAvgRating(id, fromDate, toDate, fromBirthDate, toBirthDate, gender).map(res => LocAvgRating(res))

        case None => Future.successful(NotExist(s"Location with id $id does not exist"))
      }.flatMap(x => ctx.complete(t(x)))
    }
  }
}
