package com.github.nyukhalov.highloadcup.web.actor

import akka.actor.Actor
import com.github.nyukhalov.highloadcup.AppLogger
import com.github.nyukhalov.highloadcup.core.domain.Visit
import com.github.nyukhalov.highloadcup.web.domain.{GetVisitWithId, VisitWithId}

class GetVisitWithIdActor extends Actor with AppLogger {

  override def receive: Receive = {
    case GetVisitWithId(id) =>
      val visit = Visit(1, 10, 1, 123, 4)
      sender() ! VisitWithId(visit)
  }
}
