package com.github.nyukhalov.highloadcup.core.domain

import org.rapidoid.data.JSON

import scala.util.control.NonFatal

final case class Location(id: Int, place: String, country: String, city: String, distance: Int) {
  def getId = id
  def getPlace = place
  def getCountry = country
  def getCity = city
  def getDistance = distance
}

object Location {
  def fromJson(json: String): Option[Location] = {
    try {
      val lj = JSON.parse[LocationJ](json, classOf[LocationJ])
      if (lj.hasNullFields) None
      else Some(Location(lj.id, lj.place, lj.country, lj.city, lj.distance))
    } catch {
      case NonFatal(_) => None
    }
  }
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
