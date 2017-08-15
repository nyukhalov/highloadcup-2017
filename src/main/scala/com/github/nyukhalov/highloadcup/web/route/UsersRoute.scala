package com.github.nyukhalov.highloadcup.web.route

import akka.actor.Props
import akka.http.scaladsl.server.Directives.{complete, get, path}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Route, RouteResult}
import com.github.nyukhalov.highloadcup.core.domain.User
import com.github.nyukhalov.highloadcup.web.actor.{CreateUserActor, GetUserWithIdActor, UpdateUserActor}
import com.github.nyukhalov.highloadcup.web.domain.{CreateUser, GetUserWithId, UpdateUser, UserUpdate}

import scala.concurrent.Promise

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

  private val getUserVisits =
    path("users" / IntNumber / "visits") {
      id => get {
        complete("заглушка")
      }
    }

  val usersRoute: Route = getUserRoute ~ getUserVisits ~ createUserRoute

  def getUserWithId(id: Int): Route = {
    handleRequest(Props[GetUserWithIdActor], GetUserWithId(id))
  }

  def createUser(user: User): Route = {
    handleRequest(Props[CreateUserActor], CreateUser(user))
  }

  def updateUser(id: Int, userUpdate: UserUpdate): Route = {
    handleRequest(Props[UpdateUserActor], UpdateUser(id, userUpdate))
  }
}
