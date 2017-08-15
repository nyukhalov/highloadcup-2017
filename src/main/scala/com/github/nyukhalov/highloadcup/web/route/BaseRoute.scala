package com.github.nyukhalov.highloadcup.web.route

import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.server.{Route, RouteResult}
import com.github.nyukhalov.highloadcup.web.actor.PerRequestCreator
import com.github.nyukhalov.highloadcup.web.domain.RestRequest
import com.github.nyukhalov.highloadcup.web.json.JsonSupport

import scala.concurrent.Promise

trait BaseRoute extends PerRequestCreator with JsonSupport {
  def actorSys: ActorSystem

  def handleRequest(targetProps: Props, message: RestRequest): Route = ctx => {
    val p = Promise[RouteResult]
    implicit val actorSystem = actorSys
    perRequest(ctx, targetProps, message, p)
    p.future
  }
}
