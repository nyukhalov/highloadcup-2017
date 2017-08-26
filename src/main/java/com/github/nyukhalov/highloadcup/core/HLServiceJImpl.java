package com.github.nyukhalov.highloadcup.core;

import com.github.nyukhalov.highloadcup.core.domain.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class HLServiceJImpl implements HLServiceJ {
    public final Map<Integer, UserJ> userMap = new ConcurrentHashMap<>();
    public final Map<Integer, LocationJ> locMap = new ConcurrentHashMap<>();
    public final Map<Integer, VisitJ> visitMap = new ConcurrentHashMap<>();

    @Override
    public void addUsers(List<UserJ> users) {
        users.forEach(u -> userMap.put(u.id, u));
    }

    @Override
    public void addLocations(List<LocationJ> locations) {
        locations.forEach(l -> locMap.put(l.id, l));
    }

    @Override
    public void addVisits(List<VisitJ> visits) {
        visits.forEach(v -> visitMap.put(v.id, v));
    }

    @Override
    public boolean createUser(UserJ user) {
        if (isUserExist(user.id)) return false;

        userMap.put(user.id, user);
        return true;
    }

    @Override
    public UserJ getUser(int id) {
        return userMap.get(id);
    }

    @Override
    public boolean isUserExist(int id) {
        return userMap.containsKey(id);
    }

    @Override
    public Object updateUser(int id, UserUpdateJ userUpdate) {
        return null;
    }

    @Override
    public Object getUserVisits(int id, Long fromDate, Long toDate, String country, Integer toDistance) {
        return null;
    }

    @Override
    public boolean createLocation(LocationJ loc) {
        if (isLocationExist(loc.id)) return false;

        locMap.put(loc.id, loc);
        return true;
    }

    @Override
    public LocationJ getLocation(int id) {
        return locMap.get(id);
    }

    @Override
    public boolean isLocationExist(int id) {
        return locMap.containsKey(id);
    }

    @Override
    public Object updateLocation(int id, LocationUpdateJ locationUpdate) {
        return null;
    }

    @Override
    public Object getLocAvgRating(int id, Long fromDate, Long toDate, Integer fromAge, Integer toAge, String gender) {
        return null;
    }

    @Override
    public boolean createVisit(VisitJ visit) {
        if (isVisitExist(visit.id)) return false;

        visitMap.put(visit.id, visit);
        return true;
    }

    @Override
    public VisitJ getVisit(int id) {
        return visitMap.get(id);
    }

    @Override
    public boolean isVisitExist(int id) {
        return visitMap.containsKey(id);
    }

    @Override
    public Object updateVisit(int id, VisitUpdateJ visitUpdate) {
        return null;
    }
}
