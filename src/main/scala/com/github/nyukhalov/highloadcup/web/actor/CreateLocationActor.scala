package com.github.nyukhalov.highloadcup.web.actor

import akka.actor.Actor
import com.github.nyukhalov.highloadcup.core.repository.EntityRepository
import com.github.nyukhalov.highloadcup.web.domain.{CreateLocation, SuccessfulOperation, Validation}

class CreateLocationActor(entityRepository: EntityRepository) extends Actor {
  override def receive: Receive = {
    case CreateLocation(location) =>
      val to = sender()

      entityRepository.getLocation(location.id) match {
        case None =>
          entityRepository.saveLocation(location)
          to ! SuccessfulOperation

        case Some(u) =>
          to ! Validation(s"Location with id ${location.id} already exists")
      }
  }
}
