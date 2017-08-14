package com.github.nyukhalov.highloadcup.web.actor

import akka.actor.Actor
import com.github.nyukhalov.highloadcup.core.AppLogger
import com.github.nyukhalov.highloadcup.core.domain.{Location, User}
import com.github.nyukhalov.highloadcup.database.DB
import com.github.nyukhalov.highloadcup.web.domain._

import scala.util.{Failure, Success}

class UpdateLocationActor extends Actor with AppLogger {
  implicit val ec = context.dispatcher

  override def receive: Receive = {
    case UpdateLocation(id, locationUpdate) =>
      val to = sender()

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
