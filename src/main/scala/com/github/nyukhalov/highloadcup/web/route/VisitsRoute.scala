package com.github.nyukhalov.highloadcup.web.route

import akka.actor.Props
import akka.http.scaladsl.server.Directives.{get, path}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Route, RouteResult}
import com.github.nyukhalov.highloadcup.web.actor.GetVisitWithIdActor
import com.github.nyukhalov.highloadcup.web.domain.GetVisitWithId

import scala.concurrent.Promise

trait VisitsRoute extends BaseRoute {
  private val getVisitRoute =
    path("visits" / IntNumber) {
      id => get {
        getVisitWithId {
          id
        }
      }
    }

  val visitsRoute: Route = getVisitRoute

  def getVisitWithId(id: Int): Route = ctx => {
    val p = Promise[RouteResult]
    implicit val actorSystem = as
    perRequest(ctx, Props(classOf[GetVisitWithIdActor]), GetVisitWithId(id), p)
    p.future
  }
}
