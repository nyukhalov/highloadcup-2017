package com.github.nyukhalov.highloadcup.core.json

import com.github.nyukhalov.highloadcup.core.domain._

trait DomainJsonProtocol extends LowerCaseJsonProtocol {
  implicit val userFormat = jsonFormat6(User)
  implicit val visitFormat = jsonFormat5(Visit)
  implicit val locationFormat = jsonFormat5(Location)

  implicit val usersFormat = jsonFormat1(Users)
  implicit val visitsFormat = jsonFormat1(Visits)
  implicit val locationsFormat = jsonFormat1(Locations)
}
