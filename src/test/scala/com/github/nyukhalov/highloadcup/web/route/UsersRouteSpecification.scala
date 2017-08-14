package com.github.nyukhalov.highloadcup.web.route

import akka.actor.ActorSystem
import akka.http.scaladsl.testkit.Specs2RouteTest
import org.specs2.mutable.Specification
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.model.ContentTypes._
import akka.http.scaladsl.model.{HttpEntity, HttpRequest, MediaTypes, RequestEntity}
import akka.util.ByteString
import com.github.nyukhalov.highloadcup.core.domain.User
import com.github.nyukhalov.highloadcup.web.json.JsonSupport
import org.specs2.mock.Mockito
import org.mockito.ArgumentMatchers.{eq => meq, _}
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json.DefaultJsonProtocol._

class UsersRouteSpecification extends Specification with Specs2RouteTest with Mockito with JsonSupport {
  implicit val actorSystem = ActorSystem("test")



//  "get existing user" in {
//    val er = mock[EntityRepository]
//    val route = new UsersRoute {
//      override def actorSys: ActorSystem = actorSystem
//      override def entityRepository: EntityRepository = er
//    }.usersRoute
//    val user = User(1, "email", "fn", "sn", "m", 123)
//
//    er.getUser(1) returns Some(user)
//
//    Get("/users/1") ~> route ~> check {
//      status               === OK
//      contentType          === `application/json`
//      responseAs[User]     === user
//    }
//  }
//
//  "get nonexistent user" in {
//    val er = mock[EntityRepository]
//    val route = new UsersRoute {
//      override def actorSys: ActorSystem = actorSystem
//      override def entityRepository: EntityRepository = er
//    }.usersRoute
//
//    er.getUser(1) returns None
//
//    Get("/users/1") ~> route ~> check {
//      status               === NotFound
//      contentType          === `application/json`
//    }
//  }
//
//  "create new user" in {
//    val er = mock[EntityRepository]
//    val route = new UsersRoute {
//      override def actorSys: ActorSystem = actorSystem
//      override def entityRepository: EntityRepository = er
//    }.usersRoute
//
//    er.getUser(any[Int]) returns None
//
//    val id = 1
//    val email = "email@gg.g"
//    val firstName = "fn"
//    val lastName = "sn"
//    val gender = "m"
//    val birthDate = 123
//
//    val expectedUser = User(id, email, firstName, lastName, gender, birthDate)
//
//    val correctRequest = ByteString(
//      s"""
//         |{
//         |  "id": $id,
//         |  "email": "$email",
//         |  "first_name": "$firstName",
//         |  "last_name": "$lastName",
//         |  "gender": "$gender",
//         |  "birth_date": $birthDate
//         |}
//          """.stripMargin
//    )
//
//    Post("/users/new", HttpEntity(MediaTypes.`application/json`, correctRequest)) ~> route ~> check {
//      status           === OK
//      contentType      === `application/json`
//      responseAs[String] === "{}"
//      there was one(er).saveUser(meq(expectedUser))
//    }
//  }
//
//  "create new user when it's exists" in {
//    val er = mock[EntityRepository]
//    val route = new UsersRoute {
//      override def actorSys: ActorSystem = actorSystem
//      override def entityRepository: EntityRepository = er
//    }.usersRoute
//
//    val id = 1
//    val email = "email@gg.g"
//    val firstName = "fn"
//    val lastName = "sn"
//    val gender = "m"
//    val birthDate = 123
//
//    val expectedUser = User(id, email, firstName, lastName, gender, birthDate)
//
//    er.getUser(any[Int]) returns Some(expectedUser)
//
//    val correctRequest = ByteString(
//      s"""
//         |{
//         |  "id": $id,
//         |  "email": "$email",
//         |  "first_name": "$firstName",
//         |  "last_name": "$lastName",
//         |  "gender": "$gender",
//         |  "birth_date": $birthDate
//         |}
//          """.stripMargin
//    )
//
//    Post("/users/new", HttpEntity(MediaTypes.`application/json`, correctRequest)) ~> route ~> check {
//      status           === BadRequest
//      contentType      === `application/json`
//    }
//  }
//
//  "update email of existing user" in {
//    val er = mock[EntityRepository]
//    val route = new UsersRoute {
//      override def actorSys: ActorSystem = actorSystem
//      override def entityRepository: EntityRepository = er
//    }.usersRoute
//
//
//    val id = 1
//    val email = "email@gg.g"
//    val email2 = "----"
//    val firstName = "fn"
//    val lastName = "sn"
//    val gender = "m"
//    val birthDate = 123
//
//    val existingUser = User(id, email, firstName, lastName, gender, birthDate)
//    val expectedUser = User(id, email2, firstName, lastName, gender, birthDate)
//
//    er.getUser(id) returns Some(existingUser)
//
//    val correctRequest = ByteString(
//      s"""
//         |{
//         |  "email": "$email2"
//         |}
//          """.stripMargin
//    )
//
//    Post(s"/users/$id", HttpEntity(MediaTypes.`application/json`, correctRequest)) ~> route ~> check {
//      status           === OK
//      contentType      === `application/json`
//      responseAs[String] === "{}"
//      there was one(er).saveUser(meq(expectedUser))
//    }
//  }
}
