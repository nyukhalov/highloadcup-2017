package com.github.nyukhalov.highloadcup.web.actor

import akka.actor.Actor
import com.github.nyukhalov.highloadcup.core.AppLogger
import com.github.nyukhalov.highloadcup.core.domain.{Location, LocationV}
import com.github.nyukhalov.highloadcup.database.DB
import com.github.nyukhalov.highloadcup.web.domain._

import scala.util.{Failure, Success}

class UpdateLocationActor extends Actor with AppLogger {
  import scala.concurrent.ExecutionContext.Implicits.global

  private def isValidUpdate(lu: LocationUpdate) = {
    if (lu.place.isDefined && !LocationV.isValidPlace(lu.place.get)) false
    else if (lu.country.isDefined && !LocationV.isValidCountry(lu.country.get)) false
    else if (lu.city.isDefined && !LocationV.isValidCity(lu.city.get)) false
    else if (lu.distance.isDefined && !LocationV.isValidDistance(lu.distance.get)) false
    else true
  }

  override def receive: Receive = {
    case UpdateLocation(id, locationUpdate) =>
      val to = sender()

      if (!isValidUpdate(locationUpdate)) {
        to ! Validation("Invalid location update")
      } else {

        DB.findLocationById(id).onComplete {
          case Success(res) =>
            res match {
              case None =>
                to ! NotExist(s"Location with id $id does not exist")

              case Some(l) =>
                val updatedLocation = Location(
                  id,
                  locationUpdate.place.getOrElse(l.place),
                  locationUpdate.country.getOrElse(l.country),
                  locationUpdate.city.getOrElse(l.city),
                  locationUpdate.distance.getOrElse(l.distance)
                )

                DB.updateLocation(updatedLocation).onComplete {
                  case Success(_) =>
                    to ! SuccessfulOperation

                  case Failure(ex) =>
                    to ! Error(ex.getMessage)
                }
            }

          case Failure(ex) =>
            to ! Error(ex.getMessage)
        }
      }
  }
}
