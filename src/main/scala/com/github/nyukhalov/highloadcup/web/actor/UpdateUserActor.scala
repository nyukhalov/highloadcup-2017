package com.github.nyukhalov.highloadcup.web.actor

import akka.actor.Actor
import com.github.nyukhalov.highloadcup.core.domain.User
import com.github.nyukhalov.highloadcup.database.DB
import com.github.nyukhalov.highloadcup.web.domain.{Error, NotExist, SuccessfulOperation, UpdateUser}

import scala.util.{Failure, Success}

class UpdateUserActor() extends Actor {
  implicit val ec = context.dispatcher

  override def receive: Receive = {
    case UpdateUser(id, userUpdate) =>
      val to = sender()

      DB.findUserById(id).onComplete {
        case Success(res) =>
          res match {
            case None =>
              to ! NotExist(s"User with id $id does not exist")

            case Some(u) =>
              val updatedUser = User(
                id,
                userUpdate.email.getOrElse(u.email),
                userUpdate.firstName.getOrElse(u.firstName),
                userUpdate.lastName.getOrElse(u.lastName),
                userUpdate.gender.getOrElse(u.gender),
                userUpdate.birthDate.getOrElse(u.birthDate)
              )

              DB.updateUser(updatedUser).onComplete {
                case Success(_) =>
                  to ! SuccessfulOperation

                case Failure(ex) =>
                  to ! Error(ex.getMessage)
              }
          }

        case Failure(ex) =>
          to ! Error(ex.getMessage)
      }
  }
}
