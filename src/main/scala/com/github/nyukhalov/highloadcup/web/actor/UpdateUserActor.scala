package com.github.nyukhalov.highloadcup.web.actor

import akka.actor.Actor
import com.github.nyukhalov.highloadcup.core.domain.User
import com.github.nyukhalov.highloadcup.core.repository.EntityRepository
import com.github.nyukhalov.highloadcup.web.domain.{NotExist, SuccessfulOperation, UpdateUser}

class UpdateUserActor(entityRepository: EntityRepository) extends Actor {
  override def receive: Receive = {
    case UpdateUser(id, userUpdate) =>
      val to = sender()
      entityRepository.getUser(id) match {
        case None =>
          to ! NotExist(s"User with id $id does not exis")

        case Some(u) =>
          val updatedUser = User(id, userUpdate.email, userUpdate.firstName, userUpdate.lastName, userUpdate.gender, userUpdate.birthDate)
          entityRepository.saveUser(updatedUser)
          to ! SuccessfulOperation
      }
  }
}
