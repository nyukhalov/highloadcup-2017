package com.github.nyukhalov.highloadcup.web.actor

import akka.actor.Actor
import com.github.nyukhalov.highloadcup.core.AppLogger
import com.github.nyukhalov.highloadcup.core.domain.{User, UserV}
import com.github.nyukhalov.highloadcup.database.DB
import com.github.nyukhalov.highloadcup.web.domain.{CreateUser, Error, SuccessfulOperation, Validation}

import scala.util.{Failure, Success}

class CreateUserActor extends Actor with AppLogger {
  implicit val ec = context.dispatcher

  override def receive: Receive = {
    case CreateUser(user) =>
      val to = sender()

      if (!UserV.isValid(user)) {
        to ! Validation("Invalid user")
      } else {

        DB.findUserById(user.id).onComplete {
          case Success(res) =>
            res match {
              case Some(u) =>
                to ! Validation(s"User with id ${user.id} already exists")

              case None =>
                DB.insertUser(user).onComplete {
                  case Success(_) =>
                    to ! SuccessfulOperation

                  case Failure(ex) =>
                    val msg = s"Failed when save new user $user: ${ex.getMessage}"
                    logger.error(msg, ex)
                    to ! Error(msg)
                }
            }

          case Failure(ex) =>
            val msg = s"Failed when find user with id ${user.id}: ${ex.getMessage}"
            logger.error(msg, ex)
            to ! Error(msg)
        }
      }
  }
}
