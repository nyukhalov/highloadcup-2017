package com.github.nyukhalov.highloadcup.web.actor

import akka.actor.Actor
import com.github.nyukhalov.highloadcup.core.repository.EntityRepository
import com.github.nyukhalov.highloadcup.web.domain.{GetUserWithId, NotExist, UserWithId}

class GetUserWithIdActor(entityRepository: EntityRepository) extends Actor {
  override def receive: Receive = {
    case GetUserWithId(id) =>
      val to = sender()
      val user = entityRepository.getUser(id)
      user match {
        case None => to ! NotExist(s"User with id $id does not exist")
        case Some(u) => to ! UserWithId(u)
      }
  }
}
