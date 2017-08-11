package com.github.nyukhalov.highloadcup.web.actor

import akka.actor.Actor
import com.github.nyukhalov.highloadcup.core.repository.EntityRepository
import com.github.nyukhalov.highloadcup.web.domain.{CreateUser, SuccessfulOperation, Validation}

class CreateUserActor(entityRepository: EntityRepository) extends Actor {
  override def receive: Receive = {
    case CreateUser(user) =>
      val to = sender()

      entityRepository.getUser(user.id) match {
        case None =>
          entityRepository.saveUser(user)
          to ! SuccessfulOperation

        case Some(u) =>
          to ! Validation(s"User with id ${user.id} already exists")
      }
  }
}
