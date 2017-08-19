package com.github.nyukhalov.highloadcup.core.domain

final case class Location(id: Int, place: String, country: String, city: String, distance: Int) {
  def getId = id
  def getPlace = place
  def getCountry = country
  def getCity = city
  def getDistance = distance
}

object LocationV {

  def isValid(l: Location): Boolean = {
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
