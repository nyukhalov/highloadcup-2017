package com.github.nyukhalov.highloadcup.core.domain

import scala.collection.JavaConverters._
import org.rapidoid.data.JSON

import scala.util.control.NonFatal

final case class Users(users: List[User])

object Users {
  def fromJson(json: String): Option[Users] = {
    try {
      val usersJ = JSON.parse[UsersJ](json, classOf[UsersJ])
      val users = usersJ.users.asScala.map(User.fromUserJ).toList
      Some(Users(users))
    } catch {
      case NonFatal(_) => None
    }
  }
}
