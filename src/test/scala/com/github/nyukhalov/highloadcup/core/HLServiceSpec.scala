package com.github.nyukhalov.highloadcup.core

import com.github.nyukhalov.highloadcup.core.domain._
import org.specs2.mutable.Specification
import com.github.nyukhalov.highloadcup.web.domain._

class HLServiceSpec extends Specification {
  def init: HLServiceImpl = new HLServiceImpl()

  def someValidUser: User = User(1, "email", "fn", "ln", "m", 123456)
  def someValidUser2: User = User(2, "email2", "fn2", "ln2", "m", 123457)

  def someValidLocation: Location = Location(1, "place", "country", "city", 12)
  def someValidLocation2: Location = Location(2, "place2", "country2", "city2", 21)

  "User" should {
    "create valid user properly" in {
      val s = init
      val user = someValidUser

      s.createUser(user) mustEqual SuccessfulOperation
      s.getUser(user.id) mustEqual user
    }

    "update existing user properly" in {
      val s = init
      val user = someValidUser
      s.createUser(user)

      s.updateUser(user.id, UserUpdate(Some("new email"), None, None, None, None)) mustEqual SuccessfulOperation
      s.getUser(user.id) mustEqual user.copy(email = "new email")
    }

    "get user visits" in {
      val s = init
      val user = User(1, "e", "fn", "ln", "m", 123456)
      val loc = Location(2, "p", "c", "ci", 10)
      val visit1 = Visit(1, loc.id, user.id, VisitV.minVisitAt, 5)
      val visit2 = Visit(2, loc.id, user.id, VisitV.maxVisitAt, 3)
      s.createUser(user)
      s.createLocation(loc)
      s.createVisit(visit1)
      s.createVisit(visit2)

      val userVisits = s.getUserVisits(user.id, None, None, None, None).asInstanceOf[UserVisits]

      userVisits mustEqual UserVisits(List(
        UserVisit(visit1.mark, visit1.visitedAt, loc.place),
        UserVisit(visit2.mark, visit2.visitedAt, loc.place)
      ))
    }
  }

  "Location" should {
    "create valid location properly" in {
      val s = init
      val loc = someValidLocation

      s.createLocation(loc) mustEqual SuccessfulOperation
      s.getLocation(loc.id) mustEqual loc
    }

    "update existing location properly" in {
      val s = init
      val loc = someValidLocation
      s.createLocation(loc)

      s.updateLocation(loc.id, LocationUpdate(Some("new place"), None, None, None)) mustEqual SuccessfulOperation
      s.getLocation(loc.id) mustEqual loc.copy(place = "new place")
    }

    "get avg rating" in {
      val s = init
      val user = User(1, "e", "fn", "ln", "m", 123456)
      val user2 = User(2, "e", "fn", "ln", "f", 123456)
      val loc = Location(2, "p", "c", "ci", 10)
      val visit1 = Visit(1, loc.id, user.id, VisitV.minVisitAt, 5)
      val visit2 = Visit(2, loc.id, user2.id, VisitV.maxVisitAt, 3)
      s.createUser(user)
      s.createUser(user2)
      s.createLocation(loc)
      s.createVisit(visit1)
      s.createVisit(visit2)

      s.getAverageRating(loc.id, None, None, None, None, None).asInstanceOf[LocAvgRating].avg mustEqual 4.0
      s.getAverageRating(loc.id, None, None, None, None, Some("m")).asInstanceOf[LocAvgRating].avg mustEqual visit1.mark
      s.getAverageRating(loc.id, None, None, None, None, Some("f")).asInstanceOf[LocAvgRating].avg mustEqual visit2.mark
    }
  }

  "Visit" should {
    "create valid visit properly" in {
      val s = init
      val user = someValidUser
      val loc = someValidLocation
      val visit = Visit(3, loc.id, user.id, VisitV.minVisitAt, 3)
      s.createUser(user)
      s.createLocation(loc)

      s.createVisit(visit) mustEqual SuccessfulOperation
      s.getVisit(visit.id) mustEqual visit

      val expectedVisit2 = Visit2(visit, loc, user)
      s.userMap(user.id).visits.head mustEqual expectedVisit2
      s.locMap(loc.id).visits.head mustEqual expectedVisit2
    }

    "update visit loc properly" in {
      val s = init
      val user = someValidUser
      val loc = someValidLocation
      val loc2 = someValidLocation2
      val visit = Visit(3, loc.id, user.id, VisitV.minVisitAt, 3)
      val visit2 = visit.copy(location = loc2.id)
      s.createUser(user)
      s.createLocation(loc)
      s.createLocation(loc2)
      s.createVisit(visit)

      s.updateVisit(visit.id, VisitUpdate(Some(loc2.id), None, None, None)) mustEqual SuccessfulOperation
      s.getVisit(visit.id) mustEqual visit2

      s.locMap(loc.id).visits.isEmpty must beTrue
      s.locMap(loc2.id).visits.head mustEqual Visit2(visit2, loc2, user)
    }

    "update visit user properly" in {
      val s = init
      val user = someValidUser
      val user2 = someValidUser2
      val loc = someValidLocation
      val visit = Visit(3, loc.id, user.id, VisitV.minVisitAt, 3)
      val visit2 = visit.copy(user = user2.id)
      s.createUser(user)
      s.createUser(user2)
      s.createLocation(loc)
      s.createVisit(visit)

      s.updateVisit(visit.id, VisitUpdate(None, Some(user2.id), None, None)) mustEqual SuccessfulOperation
      s.getVisit(visit.id) mustEqual visit2

      s.userMap(user.id).visits.isEmpty must beTrue
      s.userMap(user2.id).visits.head mustEqual Visit2(visit2, loc, user2)
    }
  }
}
