package com.github.nyukhalov.highloadcup.web.actor

import akka.actor.Actor
import com.github.nyukhalov.highloadcup.core.AppLogger
import com.github.nyukhalov.highloadcup.database.DB
import com.github.nyukhalov.highloadcup.web.domain._
import org.joda.time.DateTime

import scala.util.{Failure, Success}

class GetLocationAvgRatingActor extends Actor with AppLogger {
  implicit val ec = context.dispatcher

  override def receive: Receive = {
    case GetLocAvgRating(id, fromDate, toDate, fromAge, toAge, gender) =>
      val to = sender()

      logger.debug(s"Params: id=$id, fromDate=$fromDate, toDate=$toDate, fromAge=$fromAge, toAge=$toAge, gender=$gender")

      val now = DateTime.now()

      val fromBirthDate = toAge.map(ta => now.minusYears(ta).getMillis * 1000)
      val toBirthDate = fromAge.map(fa => now.minusYears(fa).getMillis * 1000)

      DB.findLocationById(id).onComplete {
        case Success(l) =>
          l match {
            case Some(_) =>
              DB.getLocAvgRating(id, fromDate, toDate, fromBirthDate, toBirthDate, gender).onComplete {
                case Success(res) =>
                  to ! LocAvgRating(res)

                case Failure(ex) =>
                  to ! Error(ex.getMessage)
              }

            case None =>
              to ! NotExist(s"Location with id $id does not exist")
          }

        case Failure(ex) =>
          to ! Error(ex.getMessage)
      }
  }
}
