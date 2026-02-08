package be.dmon.scheduler.solver;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import jakarta.inject.Inject;

import ai.timefold.solver.core.api.solver.SolverJob;
import ai.timefold.solver.core.api.solver.SolverManager;

import be.dmon.scheduler.domain.AgeGroup;
import be.dmon.scheduler.domain.FieldConfig;
import be.dmon.scheduler.domain.Team;
import be.dmon.scheduler.domain.TimeSlot;
import be.dmon.scheduler.domain.Trainer;
import be.dmon.scheduler.domain.TrainingSchedule;
import be.dmon.scheduler.domain.TrainingSession;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

/**
 * Integration tests that run the solver on realistic datasets
 * and verify that a feasible, valid schedule comes out.
 */
@QuarkusTest
class TrainingScheduleIntegrationTest {

    @Inject
    SolverManager<TrainingSchedule, String> solverManager;

    // ===== Use Case 1: Small club =====
    // 3 teams, 3 trainers, plenty of slots, generous capacity
    // Should easily find a feasible solution.

    @Test
    void smallClub_producesFeasibleSolution() throws ExecutionException, InterruptedException {
        // --- Trainers ---
        Trainer jan = new Trainer("tr1", "Jan", 4,
                Set.of(AgeGroup.U8, AgeGroup.U10), null);
        Trainer piet = new Trainer("tr2", "Piet", 4,
                Set.of(AgeGroup.U10, AgeGroup.U12), null);
        Trainer els = new Trainer("tr3", "Els", 4,
                Set.of(AgeGroup.U8, AgeGroup.U12), null);

        // --- Teams ---
        Team u8a = new Team("t1", "U8A", AgeGroup.U8, 12, 2,
                Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY), 17, 20);
        Team u10a = new Team("t2", "U10A", AgeGroup.U10, 14, 2,
                Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.THURSDAY), 17, 20);
        Team u12a = new Team("t3", "U12A", AgeGroup.U12, 16, 2,
                Set.of(DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY), 17, 20);

        // --- Time slots: Mon-Fri, 17-19 ---
        List<TimeSlot> slots = generateSlots(
                List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                        DayOfWeek.THURSDAY, DayOfWeek.FRIDAY),
                17, 19);

        List<Trainer> trainers = List.of(jan, piet, els);
        List<Team> teams = List.of(u8a, u10a, u12a);
        FieldConfig field = new FieldConfig("field", 60);

        // Generate sessions: 2 per team = 6 total
        List<TrainingSession> sessions = generateSessions(teams);

        TrainingSchedule problem = new TrainingSchedule(trainers, teams, slots,
                List.of(field), sessions);

        // --- Solve ---
        TrainingSchedule solution = solve(problem, "small-club");

        // --- Verify ---
        assertThat(solution.getScore()).isNotNull();
        assertThat(solution.getScore().isFeasible())
                .as("Solution should be feasible (no hard constraint violations), but score was: %s",
                        solution.getScore())
                .isTrue();

        assertAllSessionsAssigned(solution);
        assertTrainersQualified(solution);
        assertTeamsOnAvailableDays(solution);
        assertTeamsWithinHourRange(solution);
        assertNoTrainerConflicts(solution);
        assertNoTeamSameDay(solution);
    }

    // ===== Use Case 2: Realistic club =====
    // 6 teams across different age groups, 5 trainers with varied qualifications,
    // field capacity 50 — a realistic scheduling challenge.

    @Test
    void realisticClub_producesFeasibleSolution() throws ExecutionException, InterruptedException {
        // --- Trainers ---
        // Some trainers are parents who prefer their kid's team
        Trainer jan = new Trainer("tr1", "Jan", 6,
                Set.of(AgeGroup.U6, AgeGroup.U8, AgeGroup.U10), "t2"); // prefers U8A (his kid)
        Trainer piet = new Trainer("tr2", "Piet", 6,
                Set.of(AgeGroup.U10, AgeGroup.U12, AgeGroup.U14), null);
        Trainer els = new Trainer("tr3", "Els", 4,
                Set.of(AgeGroup.U6, AgeGroup.U8), null);
        Trainer koen = new Trainer("tr4", "Koen", 6,
                Set.of(AgeGroup.U12, AgeGroup.U14, AgeGroup.U16), "t5"); // prefers U14A
        Trainer sarah = new Trainer("tr5", "Sarah", 4,
                Set.of(AgeGroup.U14, AgeGroup.U16), null);

        // --- Teams ---
        Team u6a = new Team("t1", "U6A", AgeGroup.U6, 10, 1,
                Set.of(DayOfWeek.WEDNESDAY, DayOfWeek.SATURDAY), 14, 18);
        Team u8a = new Team("t2", "U8A", AgeGroup.U8, 12, 2,
                Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY), 17, 20);
        Team u10a = new Team("t3", "U10A", AgeGroup.U10, 14, 2,
                Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.THURSDAY), 17, 20);
        Team u12a = new Team("t4", "U12A", AgeGroup.U12, 16, 2,
                Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY), 17, 20);
        Team u14a = new Team("t5", "U14A", AgeGroup.U14, 18, 2,
                Set.of(DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY), 18, 21);
        Team u16a = new Team("t6", "U16A", AgeGroup.U16, 16, 2,
                Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY), 18, 21);

        // --- Time slots: Mon-Sat, 14-20 ---
        List<TimeSlot> slots = generateSlots(
                List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                        DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY),
                14, 20);

        List<Trainer> trainers = List.of(jan, piet, els, koen, sarah);
        List<Team> teams = List.of(u6a, u8a, u10a, u12a, u14a, u16a);
        FieldConfig field = new FieldConfig("field", 50);

        // Generate sessions: 1+2+2+2+2+2 = 11 total
        List<TrainingSession> sessions = generateSessions(teams);
        assertThat(sessions).hasSize(11);

        TrainingSchedule problem = new TrainingSchedule(trainers, teams, slots,
                List.of(field), sessions);

        // --- Solve ---
        TrainingSchedule solution = solve(problem, "realistic-club");

        // --- Verify ---
        assertThat(solution.getScore()).isNotNull();
        assertThat(solution.getScore().isFeasible())
                .as("Solution should be feasible (no hard constraint violations), but score was: %s",
                        solution.getScore())
                .isTrue();

        assertAllSessionsAssigned(solution);
        assertTrainersQualified(solution);
        assertTeamsOnAvailableDays(solution);
        assertTeamsWithinHourRange(solution);
        assertNoTrainerConflicts(solution);
        assertNoTeamSameDay(solution);
        assertFieldCapacityRespected(solution, 50);
        assertTrainerMaxHoursRespected(solution);
    }

    // ===== Use Case 3: Tight capacity =====
    // Multiple large teams, small field — tests that H6 (field capacity) is respected.

    @Test
    void tightCapacity_producesFeasibleSolution() throws ExecutionException, InterruptedException {
        Trainer jan = new Trainer("tr1", "Jan", 6,
                Set.of(AgeGroup.U10, AgeGroup.U12, AgeGroup.U14), null);
        Trainer piet = new Trainer("tr2", "Piet", 6,
                Set.of(AgeGroup.U10, AgeGroup.U12, AgeGroup.U14), null);
        Trainer els = new Trainer("tr3", "Els", 6,
                Set.of(AgeGroup.U10, AgeGroup.U12, AgeGroup.U14), null);

        // 3 teams with 20 players each, field capacity 35
        // → at most 1 team per slot (20 fits, 40 doesn't)
        Team u10a = new Team("t1", "U10A", AgeGroup.U10, 20, 2,
                Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY), 17, 20);
        Team u12a = new Team("t2", "U12A", AgeGroup.U12, 20, 2,
                Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.THURSDAY), 17, 20);
        Team u14a = new Team("t3", "U14A", AgeGroup.U14, 20, 2,
                Set.of(DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY), 17, 20);

        List<TimeSlot> slots = generateSlots(
                List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                        DayOfWeek.THURSDAY, DayOfWeek.FRIDAY),
                17, 19);

        List<Trainer> trainers = List.of(jan, piet, els);
        List<Team> teams = List.of(u10a, u12a, u14a);
        FieldConfig field = new FieldConfig("field", 35);

        List<TrainingSession> sessions = generateSessions(teams);

        TrainingSchedule problem = new TrainingSchedule(trainers, teams, slots,
                List.of(field), sessions);

        TrainingSchedule solution = solve(problem, "tight-capacity");

        assertThat(solution.getScore()).isNotNull();
        assertThat(solution.getScore().isFeasible())
                .as("Solution should be feasible (field capacity of 35 with 20-player teams), score: %s",
                        solution.getScore())
                .isTrue();

        assertAllSessionsAssigned(solution);
        assertFieldCapacityRespected(solution, 35);
        assertNoTeamSameDay(solution);
    }

    // ===== Helpers =====

    private TrainingSchedule solve(TrainingSchedule problem, String id)
            throws ExecutionException, InterruptedException {
        SolverJob<TrainingSchedule, String> job = solverManager.solve(id, problem);
        return job.getFinalBestSolution();
    }

    private List<TimeSlot> generateSlots(List<DayOfWeek> days, int fromHour, int toHour) {
        List<TimeSlot> slots = new ArrayList<>();
        for (DayOfWeek day : days) {
            for (int h = fromHour; h <= toHour; h++) {
                slots.add(new TimeSlot(day.name() + "-" + h, day, h));
            }
        }
        return slots;
    }

    private List<TrainingSession> generateSessions(List<Team> teams) {
        List<TrainingSession> sessions = new ArrayList<>();
        int counter = 0;
        for (Team team : teams) {
            for (int i = 0; i < team.getTrainingsPerWeek(); i++) {
                counter++;
                sessions.add(new TrainingSession("session-" + counter, team));
            }
        }
        return sessions;
    }

    // --- Assertion helpers: verify solution properties hold ---

    private void assertAllSessionsAssigned(TrainingSchedule solution) {
        for (TrainingSession session : solution.getSessions()) {
            assertThat(session.getTimeSlot())
                    .as("Session %s for team %s should have a time slot assigned",
                            session.getId(), session.getTeam().getName())
                    .isNotNull();
            assertThat(session.getTrainer())
                    .as("Session %s for team %s should have a trainer assigned",
                            session.getId(), session.getTeam().getName())
                    .isNotNull();
        }
    }

    private void assertTrainersQualified(TrainingSchedule solution) {
        for (TrainingSession session : solution.getSessions()) {
            assertThat(session.getTrainer().getTrainableAgeGroups())
                    .as("Trainer %s should be qualified for %s (%s)",
                            session.getTrainer().getName(),
                            session.getTeam().getName(),
                            session.getTeam().getAgeGroup())
                    .contains(session.getTeam().getAgeGroup());
        }
    }

    private void assertTeamsOnAvailableDays(TrainingSchedule solution) {
        for (TrainingSession session : solution.getSessions()) {
            assertThat(session.getTeam().getAvailableDays())
                    .as("Team %s should be scheduled on an available day, but got %s",
                            session.getTeam().getName(),
                            session.getTimeSlot().getDayOfWeek())
                    .contains(session.getTimeSlot().getDayOfWeek());
        }
    }

    private void assertTeamsWithinHourRange(TrainingSchedule solution) {
        for (TrainingSession session : solution.getSessions()) {
            int startHour = session.getTimeSlot().getStartHour();
            assertThat(startHour)
                    .as("Team %s session at %s should start >= %d",
                            session.getTeam().getName(),
                            session.getTimeSlot(), session.getTeam().getEarliestHour())
                    .isGreaterThanOrEqualTo(session.getTeam().getEarliestHour());
            assertThat(startHour)
                    .as("Team %s session at %s should start < %d",
                            session.getTeam().getName(),
                            session.getTimeSlot(), session.getTeam().getLatestHour())
                    .isLessThan(session.getTeam().getLatestHour());
        }
    }

    private void assertNoTrainerConflicts(TrainingSchedule solution) {
        Map<String, List<TrainingSession>> byTrainerSlot = new HashMap<>();
        for (TrainingSession session : solution.getSessions()) {
            String key = session.getTrainer().getId() + "@" + session.getTimeSlot().getId();
            byTrainerSlot.computeIfAbsent(key, k -> new ArrayList<>()).add(session);
        }
        for (var entry : byTrainerSlot.entrySet()) {
            assertThat(entry.getValue())
                    .as("Trainer should not have >1 session in the same slot: %s", entry.getKey())
                    .hasSize(1);
        }
    }

    private void assertNoTeamSameDay(TrainingSchedule solution) {
        Map<String, List<TrainingSession>> byTeam = new HashMap<>();
        for (TrainingSession session : solution.getSessions()) {
            byTeam.computeIfAbsent(session.getTeam().getId(), k -> new ArrayList<>()).add(session);
        }
        for (var entry : byTeam.entrySet()) {
            List<DayOfWeek> days = entry.getValue().stream()
                    .map(s -> s.getTimeSlot().getDayOfWeek())
                    .toList();
            assertThat(days)
                    .as("Team %s should have sessions on different days, but got %s",
                            entry.getKey(), days)
                    .doesNotHaveDuplicates();
        }
    }

    private void assertFieldCapacityRespected(TrainingSchedule solution, int capacity) {
        Map<String, Integer> playersBySlot = new HashMap<>();
        for (TrainingSession session : solution.getSessions()) {
            String slotId = session.getTimeSlot().getId();
            playersBySlot.merge(slotId, session.getTeam().getSize(), Integer::sum);
        }
        for (var entry : playersBySlot.entrySet()) {
            assertThat(entry.getValue())
                    .as("Slot %s has %d players, exceeding field capacity of %d",
                            entry.getKey(), entry.getValue(), capacity)
                    .isLessThanOrEqualTo(capacity);
        }
    }

    private void assertTrainerMaxHoursRespected(TrainingSchedule solution) {
        Map<String, Integer> sessionsByTrainer = new HashMap<>();
        Map<String, Trainer> trainerById = new HashMap<>();
        for (TrainingSession session : solution.getSessions()) {
            String trainerId = session.getTrainer().getId();
            sessionsByTrainer.merge(trainerId, 1, Integer::sum);
            trainerById.put(trainerId, session.getTrainer());
        }
        for (var entry : sessionsByTrainer.entrySet()) {
            Trainer trainer = trainerById.get(entry.getKey());
            assertThat(entry.getValue())
                    .as("Trainer %s has %d sessions, exceeding max %d hours/week",
                            trainer.getName(), entry.getValue(), trainer.getMaxHoursPerWeek())
                    .isLessThanOrEqualTo(trainer.getMaxHoursPerWeek());
        }
    }
}
