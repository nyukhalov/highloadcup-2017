package com.github.nyukhalov.highloadcup.web.route

import akka.http.scaladsl.server.Directives.{get, path}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Route, RouteResult}
import com.github.nyukhalov.highloadcup.core.domain.{Visit, VisitV}
import com.github.nyukhalov.highloadcup.web.domain._

trait VisitsRoute extends BaseRoute {
  private val createVisitRoute =
    path("visits" / "new") {
      post {
        decodeRequest {
          entity(as[Visit]) {
            visit =>
              createVisit {
                visit
              }
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

  def getVisitWithId(id: Int): Route = ctx => {
    val v = hlService.getVisit(id)
    ctx.complete(t(v))
  }

  def createVisit(visit: Visit): Route = ctx => {
    val r = hlService.createVisit(visit)
    ctx.complete(t(r))
  }

  def updateVisit(id: Int, visitUpdate: VisitUpdate): Route = ctx => {
    val r = hlService.updateVisit(id, visitUpdate)
    ctx.complete(t(r))
  }
}
