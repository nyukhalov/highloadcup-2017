package com.github.nyukhalov.highloadcup.web.json

import com.github.nyukhalov.highloadcup.core.domain.User
import com.github.nyukhalov.highloadcup.web.domain.UserUpdate
import org.specs2.mutable.Specification
import spray.json._

class JsonSupportSpec extends Specification with JsonSupport {
  "UserUpdate" should {

    "null value" in {
      val str = """
                  |{
                  | "email": null
                  |}
                """.stripMargin

      str.parseJson.convertTo[UserUpdate] must throwA[Exception]
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
      val user = str.parseJson.convertTo[User]

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

      str.parseJson.convertTo[User] must throwA[Exception]
    }
  }
}
