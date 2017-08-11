package com.github.nyukhalov.highloadcup.web.domain

import com.github.nyukhalov.highloadcup.core.domain.{Location, User, Visit}

// rest messages
trait RestMessage
trait RestRequest

// for updating
final case class UserUpdate(email: String, firstName: String, lastName: String, gender: String, birthDate: Long)
final case class VisitUpdate(location: Int, user: Int, visitedAt: Long, mark: Int)
final case class LocationUpdate(place: String, country: String, city: String, distance: Int)

// update entity
final case class UpdateUser(id: Int, userUpdate: UserUpdate) extends RestRequest
final case class UpdateVisit(id: Int, updateVisit: VisitUpdate) extends RestRequest
final case class UpdateLocation(id: Int, updateLocation: LocationUpdate) extends RestRequest

// create user
final case class CreateUser(user: User) extends RestRequest
// get user by id
final case class GetUserWithId(id: Int) extends RestRequest
final case class UserWithId(user: User) extends RestMessage
// create visit
final case class CreateVisit(visit: Visit) extends RestRequest
// get visit by id
final case class GetVisitWithId(id: Int) extends RestRequest
final case class VisitWithId(visit: Visit) extends RestMessage
// create location
final case class CreateLocation(location: Location) extends RestRequest
// get location by id
final case class GetLocationWithId(id: Int) extends RestRequest
final case class LocationWithId(location: Location) extends RestMessage


// model
object SuccessfulOperation
final case class Validation(msg: String)
final case class Error(msg: String)
final case class NotExist(msg: String)

