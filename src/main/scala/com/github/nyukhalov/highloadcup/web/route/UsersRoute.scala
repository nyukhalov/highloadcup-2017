package com.github.nyukhalov.highloadcup.web.route

import akka.http.scaladsl.server.Directives.{complete, get, path}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Route, RouteResult}
import com.github.nyukhalov.highloadcup.core.HLService
import com.github.nyukhalov.highloadcup.core.domain._
import com.github.nyukhalov.highloadcup.database.DB
import com.github.nyukhalov.highloadcup.web.domain._
import com.github.nyukhalov.highloadcup.web.json.JsonSupport

import scala.concurrent.{Future, Promise}

trait UsersRoute extends BaseRoute with JsonSupport {

  private val createUserRoute =
    path("users" / "new") {
      post {
        decodeRequest {
          entity(as[User]) {
            user =>
              createUser {
                user
              }
          }
        }
      }
    }

  private val getUserRoute =
    path("users" / IntNumber) { id =>
      get {
        getUserWithId {
          id
        }
      } ~
        post {
          decodeRequest {
            entity(as[UserUpdate]) {
              userUpdate => updateUser(id, userUpdate)
            }
          }
        }
    }

  private val getUserVisitsRoute =
    path("users" / IntNumber / "visits") {
      id =>
        get {
          parameters('fromDate.as[Long] ?, 'toDate.as[Long] ?, 'country.as[String] ?, 'toDistance.as[Int] ?) {
            (fromDate, toDate, country, toDistance) =>
              getUserVisits(id, fromDate, toDate, country, toDistance)
          }
        }
    }

  val usersRoute: Route = getUserRoute ~ getUserVisitsRoute ~ createUserRoute

  def getUserWithId(id: Int): Route = ctx => {
    val r = hlService.getUser(id)
    ctx.complete(t(r))
  }

  def createUser(user: User): Route = ctx => {
    import scala.concurrent.ExecutionContext.Implicits.global
    hlService
      .createUser(user)
      .flatMap(x => ctx.complete(t(x)))
  }

  def updateUser(id: Int, userUpdate: UserUpdate): Route = ctx => {
    import scala.concurrent.ExecutionContext.Implicits.global
    hlService
      .updateUser(id, userUpdate)
      .flatMap(x => ctx.complete(t(x)))
  }

  def getUserVisits(id: Int, fromDate: Option[Long], toDate: Option[Long], country: Option[String], toDistance: Option[Int]): Route = ctx => {
    import scala.concurrent.ExecutionContext.Implicits.global
    hlService
      .getUserVisits(id, fromDate, toDate, country, toDistance)
      .flatMap(x => ctx.complete(t(x)))
  }
}
