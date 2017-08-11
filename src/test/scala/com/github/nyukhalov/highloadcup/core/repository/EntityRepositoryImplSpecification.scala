package com.github.nyukhalov.highloadcup.core.repository

import com.github.nyukhalov.highloadcup.core.domain.User
import org.specs2.mutable.Specification

class EntityRepositoryImplSpecification extends Specification {
  "user update" in {
    val er = new EntityRepositoryImpl()
    val user1 = User(1, "email", "fn", "ls", "m", 123)
    val user2 = user1.copy(email = "email2")
    er.saveUser(user1)

    er.saveUser(user2)

    er.getUser(user2.id) must beSome(user2)
  }
}
