package com.github.nyukhalov.highloadcup.web.route

import akka.actor.ActorSystem
import com.github.nyukhalov.highloadcup.core.repository.EntityRepository
import com.github.nyukhalov.highloadcup.web.actor.PerRequestCreator
import com.github.nyukhalov.highloadcup.web.json.JsonSupport

trait BaseRoute extends PerRequestCreator with JsonSupport {
  def as: ActorSystem
  def entityRepository: EntityRepository
}
