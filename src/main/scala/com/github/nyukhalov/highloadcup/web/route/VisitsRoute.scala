package com.github.nyukhalov.highloadcup.web.route

import akka.actor.Props
import akka.http.scaladsl.server.Directives.{get, path}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Route, RouteResult}
import com.github.nyukhalov.highloadcup.core.domain.Visit
import com.github.nyukhalov.highloadcup.web.actor.{CreateVisitActor, GetVisitWithIdActor}
import com.github.nyukhalov.highloadcup.web.domain.{CreateVisit, GetVisitWithId}

import scala.concurrent.Promise

trait VisitsRoute extends BaseRoute {
  private val createVisitRoute =
    path("visits" / "new") {
      post {
        decodeRequest {
          entity(as[Visit]) {
            visit => createVisit { visit }
          }
        }
      }
    }

  private val getVisitRoute =
    path("visits" / IntNumber) {
      id => get {
        getVisitWithId {
          id
        }
      }
    }

  val visitsRoute: Route = getVisitRoute ~ createVisitRoute

  def getVisitWithId(id: Int): Route = ctx => {
    val p = Promise[RouteResult]
    implicit val actorSystem = actorSys
    perRequest(ctx, Props(classOf[GetVisitWithIdActor], entityRepository), GetVisitWithId(id), p)
    p.future
  }

  def createVisit(visit: Visit): Route = ctx => {
    val p = Promise[RouteResult]
    implicit val actorSystem = actorSys
    perRequest(ctx, Props(classOf[CreateVisitActor], entityRepository), CreateVisit(visit), p)
    p.future
  }
}
