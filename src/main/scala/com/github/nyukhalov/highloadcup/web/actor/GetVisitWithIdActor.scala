package com.github.nyukhalov.highloadcup.web.actor

import akka.actor.Actor
import com.github.nyukhalov.highloadcup.core.AppLogger
import com.github.nyukhalov.highloadcup.database.DB
import com.github.nyukhalov.highloadcup.web.domain._

import scala.util.{Failure, Success}

class GetVisitWithIdActor() extends Actor with AppLogger {
  import scala.concurrent.ExecutionContext.Implicits.global

  override def receive: Receive = {
    case GetVisitWithId(id) =>
      val to = sender()

      DB.findVisitById(id).onComplete {
        case Success(res) =>
          res match {
            case Some(visit) => to ! VisitWithId(visit)
            case None => to ! NotExist(s"Visit with id $id does not exist")
          }

        case Failure(ex) =>
          val msg = s"Failed when finding visit by id $id: ${ex.getMessage}"
          logger.error(msg, ex)
          to ! Error(msg)
      }
  }
}
