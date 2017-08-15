package com.github.nyukhalov.highloadcup.core.domain

final case class Visit(id: Int, location: Int, user: Int, visitedAt: Long, mark: Int)

object VisitV {
  def isValid(visit: Visit): Boolean = {
    isValidVisitedAt(visit.visitedAt) &&
    isValidMark(visit.mark)
  }

  def isValidVisitedAt(at: Long): Boolean = {
    at >= 946684800 && at <= 1420070400
  }

  def isValidMark(mark: Int): Boolean = {
    mark >= 0 && mark <= 5
  }
}
