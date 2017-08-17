package com.github.nyukhalov.highloadcup.web.route

import akka.http.scaladsl.server.Directives.{complete, get, path}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Route, RouteResult}
import com.github.nyukhalov.highloadcup.core.domain.{Location, User, UserV, Visit}
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
    import scala.concurrent.ExecutionContext.Implicits.global
    DB.findUserById(id).map {
      case Some(user) => user
      case None => NotExist(s"User with id $id does not exist")
    }.flatMap(x => ctx.complete(t(x)))
  }

  def createUser(user: User): Route = ctx => {
    import scala.concurrent.ExecutionContext.Implicits.global

    if (!UserV.isValid(user)) {
      ctx.complete(t(Validation("Invalid user")))
    } else {

      DB.findUserById(user.id).flatMap {
        case Some(_) => Future.successful(Validation(s"User with id ${user.id} already exists"))
        case None => DB.insertUser(user).map(_ => SuccessfulOperation)
      }.flatMap(x => ctx.complete(t(x)))
    }

    // handleRequest(Props[CreateUserActor], CreateUser(user))
  }

  private def isValidUpdate(uu: UserUpdate) = {
    if (uu.email.isDefined && !UserV.isValidEmail(uu.email.get)) false
    else if (uu.firstName.isDefined && !UserV.isValidName(uu.firstName.get)) false
    else if (uu.lastName.isDefined && !UserV.isValidName(uu.lastName.get)) false
    else if (uu.gender.isDefined && !UserV.isValidGender(uu.gender.get)) false
    else if (uu.birthDate.isDefined && !UserV.isValidBirthDate(uu.birthDate.get)) false
    else true
  }

  def updateUser(id: Int, userUpdate: UserUpdate): Route = ctx => {
    import scala.concurrent.ExecutionContext.Implicits.global

    if (!isValidUpdate(userUpdate)) {
      ctx.complete(t(Validation("Invalid user update")))
    } else {

      DB.findUserById(id).flatMap {
        case None => Future.successful(NotExist(s"User with id $id does not exist"))

        case Some(u) =>
          val updatedUser = User(
            id,
            userUpdate.email.getOrElse(u.email),
            userUpdate.firstName.getOrElse(u.firstName),
            userUpdate.lastName.getOrElse(u.lastName),
            userUpdate.gender.getOrElse(u.gender),
            userUpdate.birthDate.getOrElse(u.birthDate)
          )

          DB.updateUser(updatedUser).map(_ => SuccessfulOperation)
      }.flatMap(x => ctx.complete(t(x)))
    }

    //    handleRequest(Props[UpdateUserActor], UpdateUser(id, userUpdate))
  }

  def getUserVisits(id: Int, fromDate: Option[Long], toDate: Option[Long], country: Option[String], toDistance: Option[Int]): Route = ctx => {
    import scala.concurrent.ExecutionContext.Implicits.global

    DB.findUserById(id).flatMap {
      case None => Future.successful(NotExist(s"User with id $id does not exist"))
      case Some(_) => DB.getUserVisits(id, fromDate, toDate, country, toDistance).map(v => UserVisits(v.sortBy(_.visitedAt)))
    }.flatMap(x => ctx.complete(t(x)))

    //    handleRequest(Props[GetUserVisitsActor], GetUserVisits(id, fromDate, toDate, country, toDistance))
  }

}
