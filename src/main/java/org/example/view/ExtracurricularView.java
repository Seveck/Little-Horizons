package org.example.view;

import org.example.controller.ExtracurricularController;
import org.example.controller.StudentController;
import org.example.controller.TeacherController;
import org.example.model.Club;
import org.example.model.ClubStatus;
import org.example.model.Event;
import org.example.model.EventParticipant;
import org.example.model.EventRank;
import org.example.model.EventType;
import org.example.model.Student;
import org.example.model.Teacher;
import org.example.util.InputValidator;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class ExtracurricularView {

    private final Scanner consola;
    private final ExtracurricularController controller;
    private final StudentController studentController;
    private final TeacherController teacherController;

    // Constructor
    public ExtracurricularView(Scanner consola) {
        this.consola = consola;
        this.controller = new ExtracurricularController();
        this.studentController = new StudentController();
        this.teacherController = new TeacherController();
    }

    // Main menu loop for Clubs & Events
    public void showMenu() {
        while (true) {
            System.out.println("\n==================================================");
            System.out.println("     MODULO 2: ACTIVIDADES EXTRACURRICULARES");
            System.out.println("==================================================");
            System.out.println("1. Registrar nuevo club");
            System.out.println("2. Listar catalogo de clubes y cupos");
            System.out.println("3. Inscribir estudiante en un club");
            System.out.println("4. Retirar estudiante de un club");
            System.out.println("5. Consultar miembros de un club / clubes por estudiante");
            System.out.println("6. Registrar nuevo evento institucional");
            System.out.println("7. Consultar calendario de eventos");
            System.out.println("8. Registrar participante en un evento");
            System.out.println("9. Actualizar puesto de participante en competencia");
            System.out.println("10. Consultar participacion y resumen de eventos");
            System.out.println("0. Volver al menu principal");

            int opcion = InputValidator.readMenuOption(this.consola, 0, 10);

            switch (opcion) {
                case 1:
                    this.registerClubMenu();
                    break;
                case 2:
                    this.listClubsMenu();
                    break;
                case 3:
                    this.enrollStudentInClubMenu();
                    break;
                case 4:
                    this.withdrawStudentFromClubMenu();
                    break;
                case 5:
                    this.queryClubMembershipMenu();
                    break;
                case 6:
                    this.registerEventMenu();
                    break;
                case 7:
                    this.listEventsMenu();
                    break;
                case 8:
                    this.registerParticipantMenu();
                    break;
                case 9:
                    this.updateParticipantRankMenu();
                    break;
                case 10:
                    this.eventReportsMenu();
                    break;
                case 0:
                    return;
            }
        }
    }

    // 1. Register new club
    private void registerClubMenu() {
        System.out.println("\n--- REGISTRAR NUEVO CLUB ---");
        String name = InputValidator.readString(this.consola, "Nombre del club: ");
        String desc = InputValidator.readOptionalString(this.consola, "Descripcion breve: ");

        int teacherId = InputValidator.readInt(this.consola, "ID del profesor encargado: ");
        Teacher teacher = this.teacherController.findTeacherById(teacherId);
        if (teacher == null) {
            System.out.println("Profesor no encontrado.");
            return;
        }
        System.out.println("Profesor encargado: " + teacher.getFullName());

        int maxCap = InputValidator.readIntRange(this.consola, "Cupo maximo (5 - 200): ", 5, 200);
        String schedule = InputValidator.readString(this.consola, "Horario de reunion (ej. Viernes 15:00 - 16:30): ");
        String location = InputValidator.readString(this.consola, "Lugar de reunion: ");

        Club club = new Club(0, name, desc, teacherId, maxCap, schedule, location, ClubStatus.ACTIVE);
        int id = this.controller.registerClub(club);
        if (id > 0) {
            System.out.println("Club registrado exitosamente. ID: " + id);
        } else {
            System.out.println("Error al registrar club (verifique si ya existe ese nombre).");
        }
    }

    // 2. List clubs with live available spots
    private void listClubsMenu() {
        System.out.println("\n--- CATALOGO DE CLUBES ---");
        System.out.println("1. Ver solo clubes activos");
        System.out.println("2. Ver todos los clubes");
        int op = InputValidator.readMenuOption(this.consola, 1, 2);

        List<Club> clubs = (op == 1) ? this.controller.listActiveClubs() : this.controller.listAllClubs();
        if (clubs.isEmpty()) {
            System.out.println("No hay clubes para mostrar.");
            return;
        }
        for (Club c : clubs) {
            int activeMembers = this.controller.getActiveMembersCount(c.getId());
            int freeSpots = this.controller.getAvailableSpots(c.getId());
            System.out.println(c.toString() + " | Inscritos activos: " + activeMembers + " | Cupos libres: " + freeSpots);
        }
    }

    // 3. Enroll student in club (validates max capacity)
    private void enrollStudentInClubMenu() {
        System.out.println("\n--- INSCRIBIR ESTUDIANTE EN CLUB ---");
        int clubId = InputValidator.readInt(this.consola, "ID del club: ");
        Club club = this.controller.findClubById(clubId);
        if (club == null) {
            System.out.println("Club no encontrado.");
            return;
        }
        int freeSpots = this.controller.getAvailableSpots(clubId);
        System.out.println("Club: " + club.getName() + " | Cupos libres: " + freeSpots);

        int studentId = InputValidator.readInt(this.consola, "ID del estudiante: ");
        Student student = this.studentController.findStudentById(studentId);
        if (student == null) {
            System.out.println("Estudiante no encontrado.");
            return;
        }
        System.out.println("Estudiante: " + student.getFullName());

        LocalDate joinDate = InputValidator.readDate(this.consola, "Fecha de ingreso (AAAA-MM-DD): ");
        int result = this.controller.enrollStudentInClub(clubId, studentId, joinDate);

        if (result > 0) {
            System.out.println("Inscripcion al club exitosa. Membresia ID: " + result);
        } else if (result == -2) {
            System.out.println("No se pudo inscribir: el club alcanzo su cupo maximo (" + club.getMaxCapacity() + ").");
        } else if (result == -3) {
            System.out.println("El estudiante ya tiene una membresia activa en este club.");
        } else if (result == -4) {
            System.out.println("El club se encuentra inactivo.");
        } else {
            System.out.println("Error al inscribir estudiante en el club.");
        }
    }

    // 4. Withdraw student from club
    private void withdrawStudentFromClubMenu() {
        System.out.println("\n--- RETIRAR ESTUDIANTE DE CLUB ---");
        int clubId = InputValidator.readInt(this.consola, "ID del club: ");
        int studentId = InputValidator.readInt(this.consola, "ID del estudiante: ");

        if (this.controller.withdrawStudentFromClub(clubId, studentId)) {
            System.out.println("Estudiante retirado del club correctamente.");
        } else {
            System.out.println("No se encontro una membresia activa para ese estudiante en ese club.");
        }
    }

    // 5. Query members of a club or clubs of a student
    private void queryClubMembershipMenu() {
        System.out.println("\n--- CONSULTA DE MEMBRESIAS ---");
        System.out.println("1. Ver miembros inscritos de un club");
        System.out.println("2. Ver clubes en los que participa un estudiante");
        int op = InputValidator.readMenuOption(this.consola, 1, 2);

        if (op == 1) {
            int clubId = InputValidator.readInt(this.consola, "ID del club: ");
            List<String> members = this.controller.listClubMembers(clubId);
            if (members.isEmpty()) {
                System.out.println("No hay miembros registrados en ese club.");
                return;
            }
            for (String line : members) {
                System.out.println(line);
            }
        } else {
            int studentId = InputValidator.readInt(this.consola, "ID del estudiante: ");
            List<String> clubs = this.controller.listStudentClubs(studentId);
            if (clubs.isEmpty()) {
                System.out.println("El estudiante no pertenece a ningun club.");
                return;
            }
            for (String line : clubs) {
                System.out.println(line);
            }
        }
    }

    // 6. Register new event
    private void registerEventMenu() {
        System.out.println("\n--- REGISTRAR NUEVO EVENTO ---");
        String name = InputValidator.readString(this.consola, "Nombre del evento: ");
        EventType type = this.askEventType();
        String desc = InputValidator.readOptionalString(this.consola, "Descripcion: ");
        LocalDate eventDate = InputValidator.readDate(this.consola, "Fecha del evento (AAAA-MM-DD): ");
        String location = InputValidator.readString(this.consola, "Lugar del evento: ");
        boolean hasCompetition = InputValidator.readConfirmation(this.consola, "¿El evento tiene competencia y puestos?");

        Event ev = new Event(0, name, type, desc, eventDate, location, hasCompetition);
        int id = this.controller.registerEvent(ev);
        if (id > 0) {
            System.out.println("Evento registrado exitosamente. ID: " + id);
        } else {
            System.out.println("Error al registrar el evento.");
        }
    }

    // 7. List events calendar
    private void listEventsMenu() {
        System.out.println("\n--- CALENDARIO DE EVENTOS ---");
        System.out.println("1. Ver todos los eventos");
        System.out.println("2. Filtrar por tipo de evento");
        System.out.println("3. Ver solo eventos con competencia");
        int op = InputValidator.readMenuOption(this.consola, 1, 3);

        List<Event> events;
        if (op == 1) {
            events = this.controller.listAllEvents();
        } else if (op == 2) {
            EventType type = this.askEventType();
            events = this.controller.listEventsByType(type);
        } else {
            events = this.controller.listCompetitiveEvents();
        }

        if (events.isEmpty()) {
            System.out.println("No hay eventos registrados con ese filtro.");
            return;
        }
        for (Event e : events) {
            System.out.println(e.toString());
        }
    }

    // 8. Register student participant in event
    private void registerParticipantMenu() {
        System.out.println("\n--- REGISTRAR PARTICIPANTE EN EVENTO ---");
        int eventId = InputValidator.readInt(this.consola, "ID del evento: ");
        Event ev = this.controller.findEventById(eventId);
        if (ev == null) {
            System.out.println("Evento no encontrado.");
            return;
        }
        System.out.println("Evento: " + ev.getName() + " | Competencia: " + (ev.isHasCompetition() ? "Si" : "No"));

        int studentId = InputValidator.readInt(this.consola, "ID del estudiante: ");
        Student student = this.studentController.findStudentById(studentId);
        if (student == null) {
            System.out.println("Estudiante no encontrado.");
            return;
        }
        System.out.println("Estudiante: " + student.getFullName());

        EventRank rank = ev.isHasCompetition() ? this.askEventRank() : EventRank.PARTICIPANT;
        String remarks = InputValidator.readOptionalString(this.consola, "Observaciones (ej. proyecto o categoria): ");

        EventParticipant ep = new EventParticipant(0, eventId, studentId, rank, remarks);
        int id = this.controller.registerParticipant(ep);
        if (id > 0) {
            System.out.println("Participante registrado exitosamente. ID: " + id);
        } else {
            System.out.println("No se pudo registrar (verifique si el estudiante ya esta inscrito en este evento).");
        }
    }

    // 9. Update competition position of a participant
    private void updateParticipantRankMenu() {
        System.out.println("\n--- ACTUALIZAR PUESTO EN COMPETENCIA ---");
        int eventId = InputValidator.readInt(this.consola, "ID del evento: ");
        Event ev = this.controller.findEventById(eventId);
        if (ev == null) {
            System.out.println("Evento no encontrado.");
            return;
        }
        if (!ev.isHasCompetition()) {
            System.out.println("Este evento no esta marcado como competencia.");
            return;
        }

        int studentId = InputValidator.readInt(this.consola, "ID del estudiante: ");
        EventRank newRank = this.askEventRank();
        String remarks = InputValidator.readOptionalString(this.consola, "Observaciones actualizadas: ");

        if (this.controller.updateParticipantRank(eventId, studentId, newRank, remarks)) {
            System.out.println("Puesto actualizado exitosamente.");
        } else {
            System.out.println("No se encontro participacion registrada para ese estudiante en el evento.");
        }
    }

    // 10. Event participation reports & summary by type (Streams)
    private void eventReportsMenu() {
        System.out.println("\n--- REPORTES DE PARTICIPACION EN EVENTOS ---");
        System.out.println("1. Ver participantes de un evento");
        System.out.println("2. Ver historial de eventos de un estudiante");
        System.out.println("3. Ver resumen de eventos por tipo");
        int op = InputValidator.readMenuOption(this.consola, 1, 3);

        if (op == 1) {
            int eventId = InputValidator.readInt(this.consola, "ID del evento: ");
            List<String> list = this.controller.listEventParticipants(eventId);
            if (list.isEmpty()) {
                System.out.println("No hay participantes registrados en este evento.");
                return;
            }
            for (String line : list) {
                System.out.println(line);
            }
        } else if (op == 2) {
            int studentId = InputValidator.readInt(this.consola, "ID del estudiante: ");
            List<String> list = this.controller.listStudentEvents(studentId);
            if (list.isEmpty()) {
                System.out.println("El estudiante no registra participacion en eventos.");
                return;
            }
            for (String line : list) {
                System.out.println(line);
            }
        } else {
            Map<String, Long> summary = this.controller.countEventsByType();
            System.out.println("Cantidad de eventos por tipo:");
            summary.forEach((typeName, count) ->
                    System.out.println("  - " + typeName + ": " + count + " evento(s)"));
        }
    }

    private EventType askEventType() {
        System.out.println("Tipo de evento (1. Academico | 2. Cultural | 3. Deportivo | 4. Social | 5. Cientifico):");
        int op = InputValidator.readMenuOption(this.consola, 1, 5);
        switch (op) {
            case 2:
                return EventType.CULTURAL;
            case 3:
                return EventType.SPORTS;
            case 4:
                return EventType.SOCIAL;
            case 5:
                return EventType.SCIENTIFIC;
            default:
                return EventType.ACADEMIC;
        }
    }

    private EventRank askEventRank() {
        System.out.println("Puesto obtenido (1. 1er Lugar | 2. 2do Lugar | 3. 3er Lugar | 4. Participante):");
        int op = InputValidator.readMenuOption(this.consola, 1, 4);
        switch (op) {
            case 1:
                return EventRank.FIRST_PLACE;
            case 2:
                return EventRank.SECOND_PLACE;
            case 3:
                return EventRank.THIRD_PLACE;
            default:
                return EventRank.PARTICIPANT;
        }
    }
}
