package com.github.nyukhalov.highloadcup.web.actor

import akka.actor.Actor
import com.github.nyukhalov.highloadcup.core.AppLogger
import com.github.nyukhalov.highloadcup.database.DB
import com.github.nyukhalov.highloadcup.web.domain._

import scala.util.{Failure, Success}

class CreateLocationActor extends Actor with AppLogger {
  implicit val ec = context.dispatcher

  override def receive: Receive = {
    case CreateLocation(location) =>
      val to = sender()

      DB.findLocationById(location.id).onComplete {
        case Success(res) =>
          res match {
            case Some(_) =>
              to ! Validation(s"Location with id ${location.id} already exists")

            case None =>
              DB.insertLocation(location).onComplete {
                case Success(_) =>
                  to ! SuccessfulOperation

                case Failure(ex) =>
                  val msg = s"Failed when save new location $location: ${ex.getMessage}"
                  logger.error(msg, ex)
                  to ! Error(msg)
              }
          }

        case Failure(ex) =>
          val msg = s"Failed when find location with id ${location.id}: ${ex.getMessage}"
          logger.error(msg, ex)
          to ! Error(msg)
      }
  }
}
