package com.github.nyukhalov.highloadcup.core.domain

import org.rapidoid.data.JSON

import scala.beans.BeanProperty

final case class User(@BeanProperty var id: Int,
                      @BeanProperty var email: String,
                      @BeanProperty var first_name: String,
                      @BeanProperty var last_name: String,
                      @BeanProperty var gender: String,
                      @BeanProperty var birth_date: Long) {

  def this() = this(-1, "", "", "", "", -1)
}

object Test extends App {
  val str =
    """
      |{
      | "id": 1,
      | "email": "asd",
      | "last_name": "n2",
      | "gender": "g",
      | "birth_date": 123456
      |}
    """.stripMargin

  val user = JSON.parse[User](str, classOf[User])
  val a =0
}

object UserV {
  def isValid(user: User): Boolean = {
    isValidEmail(user.email) &&
    isValidName(user.first_name) &&
    isValidName(user.last_name) &&
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
