package com.github.nyukhalov.highloadcup.core.domain

import scala.collection.mutable

case class Location2(location: Location, visits: mutable.Set[Visit2])
