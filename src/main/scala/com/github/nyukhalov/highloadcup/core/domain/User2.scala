package com.github.nyukhalov.highloadcup.core.domain

import scala.collection.mutable

case class User2(user: User, visits: mutable.Set[Visit2])
