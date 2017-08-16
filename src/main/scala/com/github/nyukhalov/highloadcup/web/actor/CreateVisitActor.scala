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
          case Failure(ex) => to ! Error(ex.getMessage)
          case Success(Some(_)) => to ! Validation(s"Visit with id ${visit.id} already exists")

          case Success(None) =>

            DB.findUserById(visit.user).onComplete {
              case Failure(ex) => to ! Error(ex.getMessage)
              case Success(None) => to ! Validation(s"User with id ${visit.user} does not exist")

              case Success(Some(_)) =>

                DB.findLocationById(visit.location).onComplete {
                  case Failure(ex) => to ! Error(ex.getMessage)
                  case Success(None) => to ! Validation(s"Location with id ${visit.location} does not exist")

                  case Success(Some(_)) =>
                    DB.insertVisit(visit).onComplete {
                      case Success(_) => to ! SuccessfulOperation
                      case Failure(ex) => to ! Error(ex.getMessage)
                    }
                }
            }
        }
      }
  }
}
