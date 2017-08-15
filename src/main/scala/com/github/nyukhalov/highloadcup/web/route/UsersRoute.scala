package com.github.nyukhalov.highloadcup.web.route

import akka.actor.Props
import akka.http.scaladsl.server.Directives.{complete, get, path}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Route, RouteResult}
import com.github.nyukhalov.highloadcup.core.domain.User
import com.github.nyukhalov.highloadcup.web.actor._
import com.github.nyukhalov.highloadcup.web.domain._

trait UsersRoute extends BaseRoute {
  private val createUserRoute =
    path("users" / "new") {
      post {
        decodeRequest {
          entity(as[User]) {
            user => createUser { user }
          }
        }
      }
    }

  private val getUserRoute =
    path("users" / IntNumber) { id =>
      get {
        getUserWithId { id }
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
      id => get {
        parameters('fromDate.as[Long] ?, 'toDate.as[Long] ?, 'country.as[String] ?, 'toDistance.as[Int] ?) {
          (fromDate, toDate, country, toDistance) =>
            getUserVisits(id, fromDate, toDate, country, toDistance)
        }
      }
    }

  val usersRoute: Route = getUserRoute ~ getUserVisitsRoute ~ createUserRoute

  def getUserWithId(id: Int): Route = {
    handleRequest(Props[GetUserWithIdActor], GetUserWithId(id))
  }

  def createUser(user: User): Route = {
    handleRequest(Props[CreateUserActor], CreateUser(user))
  }

  def updateUser(id: Int, userUpdate: UserUpdate): Route = {
    handleRequest(Props[UpdateUserActor], UpdateUser(id, userUpdate))
  }

  def getUserVisits(id: Int, fromDate: Option[Long], toDate: Option[Long], country: Option[String], toDistance: Option[Int]): Route = {
    handleRequest(Props[GetUserVisitsActor], GetUserVisits(id, fromDate, toDate, country, toDistance))
  }
}
