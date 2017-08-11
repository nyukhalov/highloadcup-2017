package com.github.nyukhalov.highloadcup.core.repository

import com.github.nyukhalov.highloadcup.core.domain.{Location, User, Visit}

trait EntityRepository {
  def getUser(id: Int): Option[User]

  def addUser(user: User): Unit

  def getVisit(id: Int): Option[Visit]

  def addVisit(visit: Visit): Unit

  def getLocation(id: Int): Option[Location]

  def addLocation(location: Location): Unit
}
