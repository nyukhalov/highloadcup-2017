package com.github.nyukhalov.highloadcup.core.repository

import com.github.nyukhalov.highloadcup.core.domain.{Location, User, Visit}

trait EntityRepository {
  def getUser(id: Int): Option[User]

  def saveUser(user: User): Unit

  def getVisit(id: Int): Option[Visit]

  def saveVisit(visit: Visit): Unit

  def getLocation(id: Int): Option[Location]

  def saveLocation(location: Location): Unit
}
