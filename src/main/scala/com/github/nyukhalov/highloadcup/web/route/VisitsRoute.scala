package com.github.nyukhalov.highloadcup.web.route

import akka.http.scaladsl.server.Directives.{get, path}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Route, RouteResult}
import com.github.nyukhalov.highloadcup.core.domain.{Visit, VisitV}
import com.github.nyukhalov.highloadcup.database.DB
import com.github.nyukhalov.highloadcup.web.domain._

import scala.concurrent.{Future, Promise}

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
    import scala.concurrent.ExecutionContext.Implicits.global

    DB.findVisitById(id).map {
      case Some(visit) => visit
      case None => NotExist(s"Visit with id $id does not exist")
    }.flatMap(x => ctx.complete(t(x)))
  }

  def createVisit(visit: Visit): Route = ctx => {
    import scala.concurrent.ExecutionContext.Implicits.global

    if (!VisitV.isValid(visit)) {
      ctx.complete(t(Validation("Invalid visit")))
    } else {

      DB.findVisitById(visit.id).flatMap {
        case Some(_) => Future.successful(Validation(s"Visit with id ${visit.id} already exists"))

        case None =>

          DB.findUserById(visit.user).flatMap {
            case None => Future.successful(Validation(s"User with id ${visit.user} does not exist"))

            case Some(_) => DB.findLocationById(visit.location).flatMap {
              case None => Future.successful(Validation(s"Location with id ${visit.location} does not exist"))
              case Some(_) => DB.insertVisit(visit).map(_ => SuccessfulOperation)
            }
          }
      }.flatMap(x => ctx.complete(t(x)))
    }
  }

  private def isValidUpdate(vu: VisitUpdate) = {
    if (vu.mark.isDefined && !VisitV.isValidMark(vu.mark.get)) false
    else if (vu.visitedAt.isDefined && !VisitV.isValidVisitedAt(vu.visitedAt.get)) false
    else true
  }

  def updateVisit(id: Int, visitUpdate: VisitUpdate): Route = ctx => {
    import scala.concurrent.ExecutionContext.Implicits.global

    if (!isValidUpdate(visitUpdate)) {
      ctx.complete(t(Validation("Invalid visit update")))
    } else {

      DB.findVisitById(id).flatMap {
        case None => Future.successful(NotExist(s"Visit with id $id does not exist"))

        case Some(v) =>
          val updatedVisit = Visit(
            id,
            visitUpdate.location.getOrElse(v.location),
            visitUpdate.user.getOrElse(v.user),
            visitUpdate.visitedAt.getOrElse(v.visitedAt),
            visitUpdate.mark.getOrElse(v.mark)
          )

          DB.updateVisit(updatedVisit).map(_ => SuccessfulOperation)
      }.flatMap(x => ctx.complete(t(x)))
    }
  }
}
