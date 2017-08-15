package com.github.nyukhalov.highloadcup.web.route

import akka.actor.Props
import akka.http.scaladsl.server.Directives.{get, path}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Route, RouteResult}
import com.github.nyukhalov.highloadcup.core.domain.Visit
import com.github.nyukhalov.highloadcup.web.actor.{CreateVisitActor, GetVisitWithIdActor, UpdateVisitActor}
import com.github.nyukhalov.highloadcup.web.domain.{CreateVisit, GetVisitWithId, UpdateVisit, VisitUpdate}

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
    path("visits" / IntNumber) { id =>
      get {
        getVisitWithId {
          id
        }
      } ~
        post {
          decodeRequest {
            entity(as[VisitUpdate]) {
              visitUpdate => updateVisit(id, visitUpdate)
            }
          }
        }
    }

  val visitsRoute: Route = getVisitRoute ~ createVisitRoute

  def getVisitWithId(id: Int): Route = {
    handleRequest(Props[GetVisitWithIdActor], GetVisitWithId(id))
  }

  def createVisit(visit: Visit): Route = {
    handleRequest(Props[CreateVisitActor], CreateVisit(visit))
  }

  def updateVisit(id: Int, visitUpdate: VisitUpdate): Route = {
    handleRequest(Props[UpdateVisitActor], UpdateVisit(id, visitUpdate))
  }
}
