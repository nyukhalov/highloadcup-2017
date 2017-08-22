package com.github.nyukhalov.highloadcup.core.domain

import scala.beans.BeanProperty

final case class Location(@BeanProperty id: Int,
                          @BeanProperty place: String,
                          @BeanProperty country: String,
                          @BeanProperty city: String,
                          @BeanProperty distance: Int) {

  // initialization with invalid data
  def this() = this(-1, "", "", "", -1)
}

object LocationV {

  def isValid(l: Location): Boolean = {
    l.id > 0 &&
    l.place != null &&
    l.country != null &&
    l.city != null &&
    isValidPlace(l.place) &&
    isValidCountry(l.country) &&
    isValidCity(l.city) &&
    isValidDistance(l.distance)
  }

  def isValidPlace(place: String): Boolean = {
    place.nonEmpty
  }

  def isValidCountry(country: String): Boolean = {
    country.nonEmpty && country.length <= 50
  }

  def isValidCity(city: String): Boolean = {
    city.nonEmpty && city.length <= 50
  }

  def isValidDistance(distance: Int): Boolean = {
    distance >= 0
  }
}
