package com.github.nyukhalov.highloadcup.core.domain

import org.rapidoid.data.JSON

import scala.util.control.NonFatal

final case class Visit(id: Int, location: Int, user: Int, visitedAt: Long, mark: Int) {
  def getId = id
  def getLocation = location
  def getUser = user
  def getVisited_at = visitedAt
  def getMark = mark
}

object Visit {
  def fromJson(json: String): Option[Visit] = {
    try {
      val vj = JSON.parse[VisitJ](json, classOf[VisitJ])
      if (vj.hasNullField) None
      else Some(Visit(vj.id, vj.location, vj.user, vj.visitedAt, vj.mark))
    } catch {
      case NonFatal(_) => None
    }
  }
}

object VisitV {
  val minVisitAt = 946684800
  val maxVisitAt = 1420070400

  def isValid(visit: Visit): Boolean = {
    isValidVisitedAt(visit.visitedAt) &&
    isValidMark(visit.mark)
  }

  def isValidVisitedAt(at: Long): Boolean = {
    at >= minVisitAt && at <= maxVisitAt
  }

  def isValidMark(mark: Int): Boolean = {
    mark >= 0 && mark <= 5
  }
}
