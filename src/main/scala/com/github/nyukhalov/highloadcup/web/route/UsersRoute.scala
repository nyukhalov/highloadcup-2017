package com.github.nyukhalov.highloadcup.web.route

import akka.http.scaladsl.server.Directives.{complete, get, path}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Route, RouteResult}
import com.github.nyukhalov.highloadcup.core.domain._
import com.github.nyukhalov.highloadcup.web.domain._
import com.github.nyukhalov.highloadcup.web.json.JsonSupport

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
    val r = hlService.createUser(user)
    ctx.complete(t(r))
  }

  def updateUser(id: Int, userUpdate: UserUpdate): Route = ctx => {
    val r = hlService.updateUser(id, userUpdate)
    ctx.complete(t(r))
  }

  def getUserVisits(id: Int, fromDate: Option[Long], toDate: Option[Long], country: Option[String], toDistance: Option[Int]): Route = ctx => {
    val r = hlService.getUserVisits(id, fromDate, toDate, country, toDistance)
    ctx.complete(t(r))
  }
}
