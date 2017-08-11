package com.github.nyukhalov.highloadcup.web.actor

import akka.actor.Actor
import com.github.nyukhalov.highloadcup.core.domain.Location
import com.github.nyukhalov.highloadcup.core.repository.EntityRepository
import com.github.nyukhalov.highloadcup.web.domain.{NotExist, SuccessfulOperation, UpdateLocation}

class UpdateLocationActor(entityRepository: EntityRepository) extends Actor {
  override def receive: Receive = {
    case UpdateLocation(id, locationUpdate) =>
      val to = sender()
      entityRepository.getLocation(id) match {
        case None =>
          to ! NotExist(s"Location with id $id does not exis")

        case Some(v) =>
          val updatedLocation = Location(id, locationUpdate.place, locationUpdate.country, locationUpdate.city, locationUpdate.distance)
          entityRepository.saveLocation(updatedLocation)
          to ! SuccessfulOperation
      }
  }
}
