package com.github.nyukhalov.highloadcup.core.domain

final case class User(id: Int, email: String, firstName: String, lastName: String, gender: String, birthDate: Long) {

  // for serialization in rapidoid
  def getId = id
  def getEmail = email
  def getFirst_name = firstName
  def getLast_name = lastName
  def getGender = gender
  def getBirth_date = birthDate
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
