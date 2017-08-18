package com.github.nyukhalov.highloadcup.core.domain

final case class Visit(id: Int, location: Int, user: Int, visitedAt: Long, mark: Int)

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
