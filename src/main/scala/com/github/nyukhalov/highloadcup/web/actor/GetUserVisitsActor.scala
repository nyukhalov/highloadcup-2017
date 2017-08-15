package com.github.nyukhalov.highloadcup.web.actor

import akka.actor.Actor
import com.github.nyukhalov.highloadcup.core.AppLogger
import com.github.nyukhalov.highloadcup.database.DB
import com.github.nyukhalov.highloadcup.web.domain.{Error, GetUserVisits, UserVisits}

import scala.util.{Failure, Success}

class GetUserVisitsActor extends Actor with AppLogger {
  implicit val ec = context.dispatcher

  override def receive: Receive = {
    case GetUserVisits(id, fromDate, toDate, country, toDistance) =>
      val to = sender()

      DB.findUserById(id).onComplete {
        case Success(_) =>
          DB.getUserVisits(id, fromDate, toDate, country, toDistance).onComplete {
            case Success(visits) =>
              to ! UserVisits(visits)

            case Failure(ex) =>
              to ! Error(ex.getMessage)
          }

        case Failure(ex) =>
          to ! Error(ex.getMessage)
      }
  }
}
