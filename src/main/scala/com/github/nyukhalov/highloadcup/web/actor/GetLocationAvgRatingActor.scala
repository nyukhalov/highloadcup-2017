package com.github.nyukhalov.highloadcup.web.actor

import akka.actor.Actor
import com.github.nyukhalov.highloadcup.core.AppLogger
import com.github.nyukhalov.highloadcup.database.DB
import com.github.nyukhalov.highloadcup.web.domain._

import scala.util.{Failure, Success}

class GetLocationAvgRatingActor extends Actor with AppLogger {
  implicit val ec = context.dispatcher

  private val AGE_TO_SEC_FACTOR = 365 * 24 * 60 * 60

  private def ageToSec(age: Int): Long = age * AGE_TO_SEC_FACTOR

  override def receive: Receive = {
    case GetLocAvgRating(id, fromDate, toDate, fromAge, toAge, gender) =>
      val to = sender()

      logger.debug(s"Params: id=$id, fromDate=$fromDate, toDate=$toDate, fromAge=$fromAge, toAge=$toAge, gender=$gender")

      val nowSec = System.currentTimeMillis() * 1000
      val fromBirthDate = toAge.map(ta => nowSec - ageToSec(ta))
      val toBirthDate = fromAge.map(fa => nowSec - ageToSec(fa))

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
