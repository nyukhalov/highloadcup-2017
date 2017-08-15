package com.github.nyukhalov.highloadcup.web.actor

import akka.actor.Actor
import com.github.nyukhalov.highloadcup.core.AppLogger
import com.github.nyukhalov.highloadcup.core.domain.{Visit, VisitV}
import com.github.nyukhalov.highloadcup.database.DB
import com.github.nyukhalov.highloadcup.web.domain._

import scala.util.{Failure, Success}

class UpdateVisitActor extends Actor with AppLogger {
  implicit val ec = context.dispatcher

  private def isValidUpdate(vu: VisitUpdate) = {
    if (vu.mark.isDefined && !VisitV.isValidMark(vu.mark.get)) false
    else if (vu.visitedAt.isDefined && !VisitV.isValidVisitedAt(vu.visitedAt.get)) false
    else true
  }

  override def receive: Receive = {
    case UpdateVisit(id, visitUpdate) =>
      val to = sender()

      if (!isValidUpdate(visitUpdate)) {
        to ! Validation("Invalid visit update")
      } else {

        DB.findVisitById(id).onComplete {
          case Success(res) =>
            res match {
              case None =>
                to ! NotExist(s"Visit with id $id does not exist")

              case Some(v) =>
                val updatedVisit = Visit(
                  id,
                  visitUpdate.location.getOrElse(v.location),
                  visitUpdate.user.getOrElse(v.user),
                  visitUpdate.visitedAt.getOrElse(v.visitedAt),
                  visitUpdate.mark.getOrElse(v.mark)
                )

                DB.updateVisit(updatedVisit).onComplete {
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
