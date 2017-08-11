package com.github.nyukhalov.highloadcup.web.route

import akka.actor.Props
import akka.http.scaladsl.server.Directives.{complete, get, path}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Route, RouteResult}
import com.github.nyukhalov.highloadcup.web.actor.GetUserWithIdActor
import com.github.nyukhalov.highloadcup.web.domain.GetUserWithId

import scala.concurrent.Promise

trait UsersRoute extends BaseRoute {
 private val getUserRoute =
  path("users" / IntNumber) {
   id => get {
    getUserWithId {
     id
    }
   }
  }

 private val getUserVisits =
  path("users" / IntNumber / "visits") {
   id => get {
    complete("заглушка")
   }
  }

  val usersRoute: Route = getUserRoute ~ getUserVisits

  def getUserWithId(id: Int): Route = ctx => {
    val p = Promise[RouteResult]
    implicit val actorSystem = as
    perRequest(ctx, Props(classOf[GetUserWithIdActor], entityRepository), GetUserWithId(id), p)
    p.future
  }
}
