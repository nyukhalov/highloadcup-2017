package com.github.nyukhalov.highloadcup.web.actor

import akka.actor.Actor
import com.github.nyukhalov.highloadcup.core.AppLogger
import com.github.nyukhalov.highloadcup.core.repository.EntityRepository
import com.github.nyukhalov.highloadcup.web.domain.{GetVisitWithId, NotExist, VisitWithId}

class GetVisitWithIdActor(entityRepository: EntityRepository) extends Actor with AppLogger {

  override def receive: Receive = {
    case GetVisitWithId(id) =>
      val to = sender()
      val visit = entityRepository.getVisit(id)
      visit match {
        case None => to ! NotExist(s"Visit with id $id does not exist")
        case Some(v) => to ! VisitWithId(v)
      }
  }
}
