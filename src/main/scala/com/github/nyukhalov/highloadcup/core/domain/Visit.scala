package com.github.nyukhalov.highloadcup.core.domain

import scala.beans.BeanProperty

final case class Visit(@BeanProperty id: Int,
                       @BeanProperty location: Int,
                       @BeanProperty user: Int,
                       @BeanProperty visited_at: Long,
                       @BeanProperty mark: Int) {
  // initialization with invalid data
  def this() = this(-1, -1, -1, -1, -1)
}

object VisitV {
  val minVisitAt = 946684800
  val maxVisitAt = 1420070400

  def isValid(visit: Visit): Boolean = {
    visit.id > 0 &&
    visit.user > 0 &&
    visit.location > 0 &&
    isValidVisitedAt(visit.visited_at) &&
    isValidMark(visit.mark)
  }

  def isValidVisitedAt(at: Long): Boolean = {
    at >= minVisitAt && at <= maxVisitAt
  }

  def isValidMark(mark: Int): Boolean = {
    mark >= 0 && mark <= 5
  }
}
