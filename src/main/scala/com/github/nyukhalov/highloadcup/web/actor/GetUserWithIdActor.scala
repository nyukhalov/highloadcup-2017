package com.github.nyukhalov.highloadcup.web.actor

import akka.actor.Actor
import com.github.nyukhalov.highloadcup.core.AppLogger
import com.github.nyukhalov.highloadcup.database.DB
import com.github.nyukhalov.highloadcup.web.domain.{Error, GetUserWithId, NotExist, UserWithId}

import scala.util.{Failure, Success}

class GetUserWithIdActor() extends Actor with AppLogger {
  import scala.concurrent.ExecutionContext.Implicits.global

  override def receive: Receive = {
    case GetUserWithId(id) =>
      val to = sender()

      DB.findUserById(id).onComplete {
        case Success(res) =>
          res match {
            case Some(user) => to ! UserWithId(user)
            case None => to ! NotExist(s"User with id $id does not exist")
          }

        case Failure(ex) =>
          val msg = s"Failed when finding user by id $id: ${ex.getMessage}"
          logger.error(msg, ex)
          to ! Error(msg)
      }
  }
}
