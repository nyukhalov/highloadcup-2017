package com.github.nyukhalov.highloadcup.web.actor

import akka.actor.Actor
import com.github.nyukhalov.highloadcup.core.AppLogger
import com.github.nyukhalov.highloadcup.database.DB
import com.github.nyukhalov.highloadcup.web.domain.{Error, GetUserVisits, NotExist, UserVisits}

import scala.util.{Failure, Success}

class GetUserVisitsActor extends Actor with AppLogger {
  import scala.concurrent.ExecutionContext.Implicits.global

  override def receive: Receive = {
    case GetUserVisits(id, fromDate, toDate, country, toDistance) =>
      val to = sender()

      DB.findUserById(id).onComplete {
        case Success(user) =>
          user match {
            case Some(_) =>
              DB.getUserVisits(id, fromDate, toDate, country, toDistance).onComplete {
                case Success(visits) =>
                  to ! UserVisits(visits.sortBy(_.visitedAt))

                case Failure(ex) =>
                  to ! Error(ex.getMessage)
              }

            case None =>
              to ! NotExist(s"User with id $id does not exist")
          }

        case Failure(ex) =>
          to ! Error(ex.getMessage)
      }
  }
}
