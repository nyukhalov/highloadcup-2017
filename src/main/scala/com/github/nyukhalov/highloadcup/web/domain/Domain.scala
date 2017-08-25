package com.github.nyukhalov.highloadcup.web.domain

import com.github.nyukhalov.highloadcup.core.domain.UserUpdateJ
import org.rapidoid.data.JSON

import scala.util.control.NonFatal

object UserUpdate {
  def fromJson(json: String): Option[UserUpdate] = {
    try {
      val uuj = JSON.parse[UserUpdateJ](json, classOf[UserUpdateJ])
      if (uuj.allFieldsNull()) None
      else Some(UserUpdate(Option(uuj.email), Option(uuj.firstName), Option(uuj.lastName), Option(uuj.gender), Option(uuj.birthDate)))
    } catch {
      case NonFatal(_) => None
    }
  }
}

// for updating
final case class UserUpdate(email: Option[String], firstName: Option[String], lastName: Option[String], gender: Option[String], birthDate: Option[Long])
final case class VisitUpdate(location: Option[Int], user: Option[Int], visitedAt: Option[Long], mark: Option[Int])
final case class LocationUpdate(place: Option[String], country: Option[String], city: Option[String], distance: Option[Int])

// other
final case class LocAvgRating(avg: Float) {
  def getAvg = avg
}
final case class UserVisits(visits: List[UserVisit]) {
  import scala.collection.JavaConverters._
  def getVisits = visits.asJava
}
final case class UserVisit(mark: Int, visitedAt: Long, place: String) {
  def getMark = mark
  def getVisited_at = visitedAt
  def getPlace = place
}


// model
object SuccessfulOperation
object Validation
object NotExist

