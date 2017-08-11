package com.github.nyukhalov.highloadcup.web.actor

import akka.actor.Actor
import com.github.nyukhalov.highloadcup.core.repository.EntityRepository
import com.github.nyukhalov.highloadcup.web.domain.{GetLocationWithId, LocationWithId, NotExist}

class GetLocationWithIdActor(entityRepository: EntityRepository) extends Actor {
  override def receive: Receive = {
    case GetLocationWithId(id) =>
      val to = sender()
      val location = entityRepository.getLocation(id)
      location match {
        case None => to ! NotExist(s"Location with id $id does not exist")
        case Some(l) => to ! LocationWithId(l)
      }
  }
}
