package com.github.nyukhalov.highloadcup.web.domain

import com.github.nyukhalov.highloadcup.core.domain.User

// rest messages
trait RestMessage
trait RestRequest
// get user
final case class GetUserWithId(id: Int) extends RestRequest
final case class UserWithId(user: User) extends RestMessage


// model
final case class Validation(msg: String)
final case class Error(msg: String)

