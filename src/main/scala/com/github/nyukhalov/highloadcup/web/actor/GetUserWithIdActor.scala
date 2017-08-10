package com.github.nyukhalov.highloadcup.web.actor

import akka.actor.Actor
import com.github.nyukhalov.highloadcup.core.domain.User
import com.github.nyukhalov.highloadcup.web.domain.{GetUserWithId, UserWithId}

class GetUserWithIdActor extends Actor {
  override def receive: Receive = {
    case GetUserWithId(id) =>
      val user = User(1, "email@dd.d", "Roman", "Niukhalov", "m", 123)
      sender() ! UserWithId(user)
  }
}
