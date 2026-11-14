package org.example.controller;

import org.example.dao.ClubDao;
import org.example.dao.EventDao;
import org.example.model.Club;
import org.example.model.ClubStatus;
import org.example.model.Event;
import org.example.model.EventParticipant;
import org.example.model.EventRank;
import org.example.model.EventType;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ExtracurricularController {

    private final ClubDao clubDao;
    private final EventDao eventDao;

    // Constructor
    public ExtracurricularController() {
        this.clubDao = new ClubDao();
        this.eventDao = new EventDao();
    }

    // Register a new club
    public int registerClub(Club club) {
        return this.clubDao.insertClub(club);
    }

    // List all clubs
    public List<Club> listAllClubs() {
        return this.clubDao.findAllClubs();
    }

    // List only active clubs using Java Stream API
    public List<Club> listActiveClubs() {
        return this.clubDao.findAllClubs().stream()
                .filter(c -> c.getStatus() == ClubStatus.ACTIVE)
                .collect(Collectors.toList());
    }

    // Find club by id
    public Club findClubById(int clubId) {
        return this.clubDao.findById(clubId);
    }

    // Count active members of a club
    public int getActiveMembersCount(int clubId) {
        return this.clubDao.countActiveMembers(clubId);
    }

    // Calculate available spots remaining in a club
    public int getAvailableSpots(int clubId) {
        Club club = this.clubDao.findById(clubId);
        if (club == null) {
            return 0;
        }
        int active = this.clubDao.countActiveMembers(clubId);
        int free = club.getMaxCapacity() - active;
        return Math.max(free, 0);
    }

    // Enroll student in a club (checks max capacity inside DAO transaction)
    public int enrollStudentInClub(int clubId, int studentId, LocalDate joinDate) {
        return this.clubDao.enrollStudent(clubId, studentId, joinDate);
    }

    // Withdraw student from a club
    public boolean withdrawStudentFromClub(int clubId, int studentId) {
        return this.clubDao.withdrawStudent(clubId, studentId);
    }

    // List members of a club
    public List<String> listClubMembers(int clubId) {
        return this.clubDao.findClubMembersDetails(clubId);
    }

    // List clubs a student belongs to
    public List<String> listStudentClubs(int studentId) {
        return this.clubDao.findClubsByStudentDetails(studentId);
    }

    // Register a new event
    public int registerEvent(Event event) {
        return this.eventDao.insertEvent(event);
    }

    // List all events
    public List<Event> listAllEvents() {
        return this.eventDao.findAllEvents();
    }

    // Filter events by type using Java Stream API
    public List<Event> listEventsByType(EventType type) {
        return this.eventDao.findAllEvents().stream()
                .filter(e -> e.getType() == type)
                .collect(Collectors.toList());
    }

    // Filter only events with competition using Java Stream API
    public List<Event> listCompetitiveEvents() {
        return this.eventDao.findAllEvents().stream()
                .filter(Event::isHasCompetition)
                .collect(Collectors.toList());
    }

    // Find event by id
    public Event findEventById(int eventId) {
        return this.eventDao.findById(eventId);
    }

    // Register a student in an event
    public int registerParticipant(EventParticipant participant) {
        return this.eventDao.registerParticipant(participant);
    }

    // Update participant position in a competitive event
    public boolean updateParticipantRank(int eventId, int studentId, EventRank rank, String remarks) {
        return this.eventDao.updateParticipantRank(eventId, studentId, rank, remarks);
    }

    // List participants of an event
    public List<String> listEventParticipants(int eventId) {
        return this.eventDao.findEventParticipantsDetails(eventId);
    }

    // List events attended by a student
    public List<String> listStudentEvents(int studentId) {
        return this.eventDao.findEventsByStudentDetails(studentId);
    }

    // Summary count of events grouped by event type using Java Stream API
    public Map<String, Long> countEventsByType() {
        return this.eventDao.findAllEvents().stream()
                .collect(Collectors.groupingBy(
                        e -> e.getType().getDbValue(),
                        Collectors.counting()
                ));
    }
}
