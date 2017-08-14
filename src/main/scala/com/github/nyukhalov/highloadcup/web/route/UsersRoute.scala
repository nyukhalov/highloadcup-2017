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

  def getUserWithId(id: Int): Route = ctx => {
    val p = Promise[RouteResult]
    implicit val actorSystem = actorSys
    perRequest(ctx, Props(classOf[GetUserWithIdActor]), GetUserWithId(id), p)
    p.future
  }

  def createUser(user: User): Route = ctx => {
    val p = Promise[RouteResult]
    implicit val actorSystem = actorSys
    perRequest(ctx, Props(classOf[CreateUserActor]), CreateUser(user), p)
    p.future
  }

  def updateUser(id: Int, userUpdate: UserUpdate): Route = ctx => {
    val p = Promise[RouteResult]
    implicit val actorSystem = actorSys
    perRequest(ctx, Props(classOf[UpdateUserActor]), UpdateUser(id, userUpdate), p)
    p.future
  }
}
