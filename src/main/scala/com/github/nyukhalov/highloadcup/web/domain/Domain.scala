package com.github.nyukhalov.highloadcup.web.domain

// rest messages
trait RestMessage
trait RestRequest

// for updating
final case class UserUpdate(email: Option[String], firstName: Option[String], lastName: Option[String], gender: Option[String], birthDate: Option[Long])
final case class VisitUpdate(location: Option[Int], user: Option[Int], visitedAt: Option[Long], mark: Option[Int])
final case class LocationUpdate(place: Option[String], country: Option[String], city: Option[String], distance: Option[Int])

// other
final case class LocAvgRating(avg: Float)
final case class GetUserVisits(id: Int, fromDate: Option[Long], toDate: Option[Long], country: Option[String], toDistance: Option[Int]) extends RestRequest
final case class UserVisits(visits: List[UserVisit])
final case class UserVisit(mark: Int, visitedAt: Long, place: String)


// model
object SuccessfulOperation
object Validation
object NotExist

