package com.github.nyukhalov.highloadcup.web.actor

import akka.actor.Actor
import com.github.nyukhalov.highloadcup.core.AppLogger
import com.github.nyukhalov.highloadcup.core.domain.{Visit, VisitV}
import com.github.nyukhalov.highloadcup.database.DB
import com.github.nyukhalov.highloadcup.web.domain._

import scala.util.{Failure, Success}

class CreateVisitActor extends Actor with AppLogger {
  implicit val ec = context.dispatcher

  override def receive: Receive = {
    case CreateVisit(visit) =>
      val to = sender()

      if (!VisitV.isValid(visit)) {
        to ! Validation("Invalid visit")
      } else {

        DB.findVisitById(visit.id).onComplete {
          case Success(res) =>
            res match {
              case Some(_) =>
                to ! Validation(s"Visit with id ${visit.id} already exists")

              case None =>
                DB.insertVisit(visit).onComplete {
                  case Success(_) =>
                    to ! SuccessfulOperation

                  case Failure(ex) =>
                    val msg = s"Failed when save new visit $visit: ${ex.getMessage}"
                    logger.error(msg, ex)
                    to ! Error(msg)
                }
            }

          case Failure(ex) =>
            val msg = s"Failed when find visit with id ${visit.id}: ${ex.getMessage}"
            logger.error(msg, ex)
            to ! Error(msg)
        }
      }
  }
}
