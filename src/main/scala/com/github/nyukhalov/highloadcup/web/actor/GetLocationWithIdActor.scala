package com.github.nyukhalov.highloadcup.web.actor

import akka.actor.Actor
import com.github.nyukhalov.highloadcup.core.AppLogger
import com.github.nyukhalov.highloadcup.database.DB
import com.github.nyukhalov.highloadcup.web.domain._

import scala.util.{Failure, Success}

class GetLocationWithIdActor() extends Actor with AppLogger {
  import scala.concurrent.ExecutionContext.Implicits.global

  override def receive: Receive = {
    case GetLocationWithId(id) =>
      val to = sender()

      DB.findLocationById(id).onComplete {
        case Success(res) =>
          res match {
            case Some(location) => to ! LocationWithId(location)
            case None => to ! NotExist(s"Location with id $id does not exist")
          }

        case Failure(ex) =>
          val msg = s"Failed when finding location by id $id: ${ex.getMessage}"
          logger.error(msg, ex)
          to ! Error(msg)
      }
  }
}
