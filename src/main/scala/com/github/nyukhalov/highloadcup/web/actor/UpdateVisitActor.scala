package com.github.nyukhalov.highloadcup.web.actor

import akka.actor.Actor
import com.github.nyukhalov.highloadcup.core.domain.Visit
import com.github.nyukhalov.highloadcup.core.repository.EntityRepository
import com.github.nyukhalov.highloadcup.web.domain.{NotExist, SuccessfulOperation, UpdateVisit}

class UpdateVisitActor(entityRepository: EntityRepository) extends Actor {
  override def receive: Receive = {
    case UpdateVisit(id, visitUpdate) =>
      val to = sender()
      entityRepository.getVisit(id) match {
        case None =>
          to ! NotExist(s"Visit with id $id does not exis")

        case Some(v) =>
          val updatedVisit = Visit(
            id,
            visitUpdate.location.getOrElse(v.location),
            visitUpdate.user.getOrElse(v.user),
            visitUpdate.visitedAt.getOrElse(v.visitedAt),
            visitUpdate.mark.getOrElse(v.mark)
          )
          entityRepository.saveVisit(updatedVisit)
          to ! SuccessfulOperation
      }
  }
}
