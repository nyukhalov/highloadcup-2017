package com.github.nyukhalov.highloadcup.web.actor

import akka.actor.Actor
import com.github.nyukhalov.highloadcup.core.domain.Location
import com.github.nyukhalov.highloadcup.web.domain.{GetLocationWithId, LocationWithId}

class GetLocationWithIdActor extends Actor {
  override def receive: Receive = {
    case GetLocationWithId(id) =>
      val location = Location(id, "asd", "cs", "city", 100)
      sender() ! LocationWithId(location)
  }
}
