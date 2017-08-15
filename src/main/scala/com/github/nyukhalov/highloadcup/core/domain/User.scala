package com.github.nyukhalov.highloadcup.core.domain

final case class User(id: Int, email: String, firstName: String, lastName: String, gender: String, birthDate: Long)

object UserV {
  def isValid(user: User): Boolean = {
    isValidEmail(user.email) &&
    isValidName(user.firstName) &&
    isValidName(user.lastName) &&
    isValidGender(user.gender) &&
    isValidBirthDate(user.birthDate)
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

  def isValidBirthDate(bd: Long): Boolean = {
    bd >= -1262304000 && bd <= 915148800
  }
}
