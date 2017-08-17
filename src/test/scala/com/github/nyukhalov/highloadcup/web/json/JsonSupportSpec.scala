package com.github.nyukhalov.highloadcup.web.json

import com.github.nyukhalov.highloadcup.core.domain.User
import com.github.nyukhalov.highloadcup.web.domain.{LocAvgRating, UserUpdate}
import org.specs2.mutable.Specification
import io.circe.parser._
import io.circe.syntax._

class JsonSupportSpec extends Specification with JsonSupport {
  "UserUpdate" should {

    "null value" in {
      val str = """
                  |{
                  | "email": null
                  |}
                """.stripMargin

      val dec = decode[UserUpdate](str)
      dec.isLeft must beTrue
    }

    "one value" in {
      val str = """
                  |{
                  | "email": "email"
                  |}
                """.stripMargin

      val update = decode[UserUpdate](str).toOption.get

      update mustEqual UserUpdate(Some("email"), None, None, None, None)
    }
  }

  "User" should {
    "valid json" in {
      val id = 77711336
      val firstName = "fn"
      val lastName = "ln"
      val email = "email"
      val gender = "m"
      val birthDate = -1720915200


      val str = s"""
        |{
        |    "first_name": "$firstName",
        |    "email": "$email",
        |    "id": $id,
        |    "last_name": "$lastName",
        |    "birth_date": $birthDate,
        |    "gender": "$gender"
        |}
      """.stripMargin

      val expectedUser = User(id, email, firstName, lastName, gender, birthDate)
      val user = decode[User](str).toOption.get

      user mustEqual expectedUser
    }

    "null email" in {
      val str = """
        |{
        |    "first_name": "New",
        |    "email": null,
        |    "id": 77711336,
        |    "last_name": "User",
        |    "birth_date": -1720915200,
        |    "gender": "m"
        |}
      """.stripMargin

      decode[User](str).isLeft must beTrue
    }
  }

  "LocAvg" should {
    "5 digits" in {
      val avg = LocAvgRating(3.12345f)

      avg.asJson.noSpaces mustEqual """{"avg":3.12345}"""
    }
  }
}
