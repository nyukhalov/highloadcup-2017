package com.github.nyukhalov.highloadcup.core.domain

import com.github.nyukhalov.highloadcup.core.AppLogger
import com.github.nyukhalov.highloadcup.web.domain.UserUpdate
import com.github.nyukhalov.highloadcup.web.json.JsonSupport
import com.jsoniter.JsonIterator
import com.jsoniter.spi.{Config, Extension}
import org.rapidoid.data.JSON

import scala.util.control.NonFatal

final case class User(id: Int, email: String, firstName: String, lastName: String, gender: String, birthDate: Long) {

  // for serialization in rapidoid
  def getId = id

  def getEmail = email

  def getFirst_name = firstName

  def getLast_name = lastName

  def getGender = gender

  def getBirth_date = birthDate
}

object User {
  def fromJson(json: String): Option[User] = {
    try {
      val uj = JSON.parse[UserJ](json, classOf[UserJ])
      if (uj.hasNullField) None
      else Some(fromUserJ(uj))
    } catch {
      case NonFatal(_) => None
    }
  }

  def fromJson2(json: String): Option[User] = {
    try {
      val uj = JsonIterator.deserialize[UserJ](json, classOf[UserJ])
      Some(fromUserJ(uj))
    } catch {
      case NonFatal(_) => None
    }
  }

  def fromUserJ(uj: UserJ): User = User(uj.id, uj.email, uj.firstName, uj.lastName, uj.gender, uj.birthDate)
}

object Test extends App with AppLogger with JsonSupport {


  val json =
    """
      |{
      | "id": 1,
      | "email": "email.com",
      | "first_name": "fn",
      | "last_name": "ln",
      | "gender": "m",
      | "birth_date": 12345
      |}
    """.stripMargin

  import io.circe.parser._
  import scala.collection.JavaConverters._

  def fromJson1(js: String): User = {
    decode[User](js).right.get
  }

  def fromJson2(js: String): User = {
    val map = JSON.parseMap(js).asScala
    val id = map("id").asInstanceOf[Int]
    val email = map("email").asInstanceOf[String]
    val fn = map("first_name").asInstanceOf[String]
    val ln = map("last_name").asInstanceOf[String]
    val gender = map("gender").asInstanceOf[String]
    val birthDay: Long = map("birth_date").asInstanceOf[Int]
    User(id, email, fn, ln, gender, birthDay)
  }

  def fromJson3(js: String): User = {
    val uj = JSON.parse[UserJ](js, classOf[UserJ])
    User.fromUserJ(uj)
  }

  def fromJson4(js: String): User = {
    val uj = JsonIterator.deserialize[UserJ](js, classOf[UserJ])
    User.fromUserJ(uj)
  }

  val start = System.currentTimeMillis()

  //  (1 to 100).foreach{_ => {
  //    fromJson4(json)
  //  }}

  val updStr =
    s"""
       |{
       |  "email": null,
       |  "birth_date": 12345
       |}
    """.stripMargin

//  val fuck = JsonIterator.deserialize[UserUpdateJ](updStr, classOf[UserUpdateJ])

  def f1(js: String): Option[UserUpdate] = {
    try {
      val uu = JsonIterator.deserialize[UserUpdateJ](updStr, classOf[UserUpdateJ])
      Some(UserUpdate(Option(uu.email), Option(uu.firstName), Option(uu.lastName), Option(uu.gender), Option(uu.birthDate)))
    } catch {
      case NonFatal(_) => None
    }
  }

  def f2(js: String): Option[UserUpdate] = {
    decode[UserUpdate](js).toOption
  }

  (1 to 100).foreach { _ => {
    f1(updStr)
  }}

  logger.info(s"Elapsed time: ${System.currentTimeMillis() - start} ms")
}

object UserV {
  def isValid(user: User): Boolean = {
    isValidEmail(user.email) &&
      isValidName(user.firstName) &&
      isValidName(user.lastName) &&
      isValidGender(user.gender)
  }

  def isValidEmail(email: String): Boolean = {
    email.nonEmpty && email.length <= 100
  }

  def isValidName(fn: String): Boolean = {
    fn.nonEmpty && fn.length <= 50
  }

  def isValidGender(g: String): Boolean = {
    g == "m" || g == "f"
  }
}
