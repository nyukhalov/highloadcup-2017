package com.github.nyukhalov.highloadcup.web.actor

import akka.actor.Actor
import com.github.nyukhalov.highloadcup.core.repository.EntityRepository
import com.github.nyukhalov.highloadcup.web.domain.{CreateVisit, SuccessfulOperation, Validation}

class CreateVisitActor(entityRepository: EntityRepository) extends Actor {
  override def receive: Receive = {
    case CreateVisit(visit) =>
      val to = sender()

      entityRepository.getVisit(visit.id) match {
        case None =>
          entityRepository.saveVisit(visit)
          to ! SuccessfulOperation

        case Some(u) =>
          to ! Validation(s"Visit with id ${visit.id} already exists")
      }
  }
}
