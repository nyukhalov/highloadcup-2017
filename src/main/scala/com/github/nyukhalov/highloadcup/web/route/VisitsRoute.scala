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
    import scala.concurrent.ExecutionContext.Implicits.global
    hlService
      .createVisit(visit)
      .flatMap(x => ctx.complete(t(x)))
  }

  def updateVisit(id: Int, visitUpdate: VisitUpdate): Route = ctx => {
    import scala.concurrent.ExecutionContext.Implicits.global
    hlService
      .updateVisit(id, visitUpdate)
      .flatMap(x => ctx.complete(t(x)))
  }
}
