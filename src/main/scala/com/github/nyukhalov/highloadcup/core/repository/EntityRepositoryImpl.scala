package com.github.nyukhalov.highloadcup.core.repository

import com.github.nyukhalov.highloadcup.core.domain.{Location, User, Visit}

class EntityRepositoryImpl extends EntityRepository {
  var id2User: Map[Int, User] = Map.empty
  var id2Visit: Map[Int, Visit] = Map.empty
  var id2Location: Map[Int, Location] = Map.empty

  override def getUser(id: Int): Option[User] = id2User.get(id)

  override def saveUser(user: User): Unit = {
    id2User.synchronized {
      id2User += (user.id -> user)
    }
  }

  override def getVisit(id: Int): Option[Visit] = id2Visit.get(id)

  override def saveVisit(visit: Visit): Unit = {
    id2Visit.synchronized {
      id2Visit += (visit.id -> visit)
    }
  }

  override def getLocation(id: Int): Option[Location] = id2Location.get(id)

  override def saveLocation(location: Location): Unit = {
    id2Location.synchronized {
      id2Location += (location.id -> location)
    }
  }
}
