package be.dmon.scheduler.solver;

import java.time.DayOfWeek;
import java.util.Set;

import jakarta.inject.Inject;

import ai.timefold.solver.test.api.score.stream.ConstraintVerifier;

import be.dmon.scheduler.domain.AgeGroup;
import be.dmon.scheduler.domain.FieldConfig;
import be.dmon.scheduler.domain.Team;
import be.dmon.scheduler.domain.TimeSlot;
import be.dmon.scheduler.domain.Trainer;
import be.dmon.scheduler.domain.TrainingSchedule;
import be.dmon.scheduler.domain.TrainingSession;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class TrainingScheduleConstraintProviderTest {

    @Inject
    ConstraintVerifier<TrainingScheduleConstraintProvider, TrainingSchedule> constraintVerifier;

    // --- Shared test data helpers ---

    private static final TimeSlot MON_17 = new TimeSlot("MON-17", DayOfWeek.MONDAY, 17);
    private static final TimeSlot MON_18 = new TimeSlot("MON-18", DayOfWeek.MONDAY, 18);
    private static final TimeSlot MON_19 = new TimeSlot("MON-19", DayOfWeek.MONDAY, 19);
    private static final TimeSlot TUE_17 = new TimeSlot("TUE-17", DayOfWeek.TUESDAY, 17);
    private static final TimeSlot WED_17 = new TimeSlot("WED-17", DayOfWeek.WEDNESDAY, 17);
    private static final TimeSlot THU_17 = new TimeSlot("THU-17", DayOfWeek.THURSDAY, 17);
    private static final TimeSlot FRI_17 = new TimeSlot("FRI-17", DayOfWeek.FRIDAY, 17);

    private Trainer trainer(String id, String name, int maxHours, Set<AgeGroup> groups, String preferredTeamId) {
        return new Trainer(id, name, maxHours, groups, preferredTeamId);
    }

    private Team team(String id, String name, AgeGroup age, int size, int trainingsPerWeek,
            Set<DayOfWeek> availDays, int earliest, int latest) {
        return new Team(id, name, age, size, trainingsPerWeek, availDays, earliest, latest);
    }

    // ===== H1: Team slot availability =====

    @Test
    void teamSlotAvailability_unavailableDay() {
        Team teamA = team("t1", "U10A", AgeGroup.U10, 14, 2,
                Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY), 17, 20);
        Trainer trainerA = trainer("tr1", "Jan", 6, Set.of(AgeGroup.U10), null);
        // Tuesday is not in the team's available days
        TrainingSession session = new TrainingSession("s1", teamA, TUE_17, trainerA);

        constraintVerifier.verifyThat(TrainingScheduleConstraintProvider::teamSlotAvailability)
                .given(session)
                .penalizes(1);
    }

    @Test
    void teamSlotAvailability_tooEarly() {
        Team teamA = team("t1", "U10A", AgeGroup.U10, 14, 2,
                Set.of(DayOfWeek.MONDAY), 18, 20);
        Trainer trainerA = trainer("tr1", "Jan", 6, Set.of(AgeGroup.U10), null);
        // Slot starts at 17 but team's earliest is 18
        TrainingSession session = new TrainingSession("s1", teamA, MON_17, trainerA);

        constraintVerifier.verifyThat(TrainingScheduleConstraintProvider::teamSlotAvailability)
                .given(session)
                .penalizes(1);
    }

    @Test
    void teamSlotAvailability_valid() {
        Team teamA = team("t1", "U10A", AgeGroup.U10, 14, 2,
                Set.of(DayOfWeek.MONDAY), 17, 20);
        Trainer trainerA = trainer("tr1", "Jan", 6, Set.of(AgeGroup.U10), null);
        TrainingSession session = new TrainingSession("s1", teamA, MON_17, trainerA);

        constraintVerifier.verifyThat(TrainingScheduleConstraintProvider::teamSlotAvailability)
                .given(session)
                .penalizes(0);
    }

    // ===== H2: Trainer qualification =====

    @Test
    void trainerQualification_notQualified() {
        Team teamA = team("t1", "U10A", AgeGroup.U10, 14, 2,
                Set.of(DayOfWeek.MONDAY), 17, 20);
        // Trainer can only train U6, not U10
        Trainer trainerA = trainer("tr1", "Jan", 6, Set.of(AgeGroup.U6), null);
        TrainingSession session = new TrainingSession("s1", teamA, MON_17, trainerA);

        constraintVerifier.verifyThat(TrainingScheduleConstraintProvider::trainerQualification)
                .given(session)
                .penalizes(1);
    }

    @Test
    void trainerQualification_qualified() {
        Team teamA = team("t1", "U10A", AgeGroup.U10, 14, 2,
                Set.of(DayOfWeek.MONDAY), 17, 20);
        Trainer trainerA = trainer("tr1", "Jan", 6, Set.of(AgeGroup.U10), null);
        TrainingSession session = new TrainingSession("s1", teamA, MON_17, trainerA);

        constraintVerifier.verifyThat(TrainingScheduleConstraintProvider::trainerQualification)
                .given(session)
                .penalizes(0);
    }

    // ===== H3: Trainer conflict =====

    @Test
    void trainerConflict_sameTrainerSameSlot() {
        Team teamA = team("t1", "U10A", AgeGroup.U10, 14, 2,
                Set.of(DayOfWeek.MONDAY), 17, 20);
        Team teamB = team("t2", "U12A", AgeGroup.U12, 16, 2,
                Set.of(DayOfWeek.MONDAY), 17, 20);
        Trainer trainerA = trainer("tr1", "Jan", 6, Set.of(AgeGroup.U10, AgeGroup.U12), null);
        TrainingSession s1 = new TrainingSession("s1", teamA, MON_17, trainerA);
        TrainingSession s2 = new TrainingSession("s2", teamB, MON_17, trainerA);

        constraintVerifier.verifyThat(TrainingScheduleConstraintProvider::trainerConflict)
                .given(s1, s2)
                .penalizes(1);
    }

    @Test
    void trainerConflict_differentSlots() {
        Team teamA = team("t1", "U10A", AgeGroup.U10, 14, 2,
                Set.of(DayOfWeek.MONDAY), 17, 20);
        Team teamB = team("t2", "U12A", AgeGroup.U12, 16, 2,
                Set.of(DayOfWeek.MONDAY), 17, 20);
        Trainer trainerA = trainer("tr1", "Jan", 6, Set.of(AgeGroup.U10, AgeGroup.U12), null);
        TrainingSession s1 = new TrainingSession("s1", teamA, MON_17, trainerA);
        TrainingSession s2 = new TrainingSession("s2", teamB, MON_18, trainerA);

        constraintVerifier.verifyThat(TrainingScheduleConstraintProvider::trainerConflict)
                .given(s1, s2)
                .penalizes(0);
    }

    // ===== H4: Team conflict =====

    @Test
    void teamConflict_sameTeamSameSlot() {
        Team teamA = team("t1", "U10A", AgeGroup.U10, 14, 2,
                Set.of(DayOfWeek.MONDAY), 17, 20);
        Trainer trainerA = trainer("tr1", "Jan", 6, Set.of(AgeGroup.U10), null);
        Trainer trainerB = trainer("tr2", "Piet", 6, Set.of(AgeGroup.U10), null);
        TrainingSession s1 = new TrainingSession("s1", teamA, MON_17, trainerA);
        TrainingSession s2 = new TrainingSession("s2", teamA, MON_17, trainerB);

        constraintVerifier.verifyThat(TrainingScheduleConstraintProvider::teamConflict)
                .given(s1, s2)
                .penalizes(1);
    }

    // ===== H5: Team separate days =====

    @Test
    void teamSeparateDays_sameDayPenalized() {
        Team teamA = team("t1", "U10A", AgeGroup.U10, 14, 2,
                Set.of(DayOfWeek.MONDAY), 17, 20);
        Trainer trainerA = trainer("tr1", "Jan", 6, Set.of(AgeGroup.U10), null);
        TrainingSession s1 = new TrainingSession("s1", teamA, MON_17, trainerA);
        TrainingSession s2 = new TrainingSession("s2", teamA, MON_18, trainerA);

        constraintVerifier.verifyThat(TrainingScheduleConstraintProvider::teamSeparateDays)
                .given(s1, s2)
                .penalizes(1);
    }

    @Test
    void teamSeparateDays_differentDaysOk() {
        Team teamA = team("t1", "U10A", AgeGroup.U10, 14, 2,
                Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY), 17, 20);
        Trainer trainerA = trainer("tr1", "Jan", 6, Set.of(AgeGroup.U10), null);
        TrainingSession s1 = new TrainingSession("s1", teamA, MON_17, trainerA);
        TrainingSession s2 = new TrainingSession("s2", teamA, WED_17, trainerA);

        constraintVerifier.verifyThat(TrainingScheduleConstraintProvider::teamSeparateDays)
                .given(s1, s2)
                .penalizes(0);
    }

    // ===== H6: Field capacity =====

    @Test
    void fieldCapacity_exceeded() {
        Team teamA = team("t1", "U10A", AgeGroup.U10, 30, 2,
                Set.of(DayOfWeek.MONDAY), 17, 20);
        Team teamB = team("t2", "U12A", AgeGroup.U12, 35, 2,
                Set.of(DayOfWeek.MONDAY), 17, 20);
        Trainer trainerA = trainer("tr1", "Jan", 6, Set.of(AgeGroup.U10), null);
        Trainer trainerB = trainer("tr2", "Piet", 6, Set.of(AgeGroup.U12), null);
        FieldConfig fieldConfig = new FieldConfig("field", 60);
        TrainingSession s1 = new TrainingSession("s1", teamA, MON_17, trainerA);
        TrainingSession s2 = new TrainingSession("s2", teamB, MON_17, trainerB);

        // 30 + 35 = 65 > 60 → penalize by 5
        constraintVerifier.verifyThat(TrainingScheduleConstraintProvider::fieldCapacity)
                .given(s1, s2, fieldConfig)
                .penalizesBy(5);
    }

    @Test
    void fieldCapacity_withinLimit() {
        Team teamA = team("t1", "U10A", AgeGroup.U10, 20, 2,
                Set.of(DayOfWeek.MONDAY), 17, 20);
        Team teamB = team("t2", "U12A", AgeGroup.U12, 20, 2,
                Set.of(DayOfWeek.MONDAY), 17, 20);
        Trainer trainerA = trainer("tr1", "Jan", 6, Set.of(AgeGroup.U10), null);
        Trainer trainerB = trainer("tr2", "Piet", 6, Set.of(AgeGroup.U12), null);
        FieldConfig fieldConfig = new FieldConfig("field", 60);
        TrainingSession s1 = new TrainingSession("s1", teamA, MON_17, trainerA);
        TrainingSession s2 = new TrainingSession("s2", teamB, MON_17, trainerB);

        // 20 + 20 = 40 <= 60
        constraintVerifier.verifyThat(TrainingScheduleConstraintProvider::fieldCapacity)
                .given(s1, s2, fieldConfig)
                .penalizes(0);
    }

    // ===== H7: Trainer max hours =====

    @Test
    void trainerMaxHours_exceeded() {
        Team teamA = team("t1", "U10A", AgeGroup.U10, 14, 2,
                Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY), 17, 20);
        Trainer trainerA = trainer("tr1", "Jan", 2, Set.of(AgeGroup.U10), null);
        TrainingSession s1 = new TrainingSession("s1", teamA, MON_17, trainerA);
        TrainingSession s2 = new TrainingSession("s2", teamA, TUE_17, trainerA);
        TrainingSession s3 = new TrainingSession("s3", teamA, WED_17, trainerA);

        // 3 sessions, max 2 → penalize by 1
        constraintVerifier.verifyThat(TrainingScheduleConstraintProvider::trainerMaxHours)
                .given(s1, s2, s3)
                .penalizesBy(1);
    }

    @Test
    void trainerMaxHours_withinLimit() {
        Team teamA = team("t1", "U10A", AgeGroup.U10, 14, 2,
                Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY), 17, 20);
        Trainer trainerA = trainer("tr1", "Jan", 4, Set.of(AgeGroup.U10), null);
        TrainingSession s1 = new TrainingSession("s1", teamA, MON_17, trainerA);
        TrainingSession s2 = new TrainingSession("s2", teamA, TUE_17, trainerA);

        // 2 sessions, max 4
        constraintVerifier.verifyThat(TrainingScheduleConstraintProvider::trainerMaxHours)
                .given(s1, s2)
                .penalizes(0);
    }

    // ===== H8: Trainer max 3 per day =====

    @Test
    void trainerMaxPerDay_exceeded() {
        Team t1 = team("t1", "U10A", AgeGroup.U10, 14, 1, Set.of(DayOfWeek.MONDAY), 17, 21);
        Team t2 = team("t2", "U10B", AgeGroup.U10, 14, 1, Set.of(DayOfWeek.MONDAY), 17, 21);
        Team t3 = team("t3", "U12A", AgeGroup.U12, 16, 1, Set.of(DayOfWeek.MONDAY), 17, 21);
        Team t4 = team("t4", "U12B", AgeGroup.U12, 16, 1, Set.of(DayOfWeek.MONDAY), 17, 21);
        Trainer trainerA = trainer("tr1", "Jan", 10,
                Set.of(AgeGroup.U10, AgeGroup.U12), null);
        TrainingSession s1 = new TrainingSession("s1", t1, MON_17, trainerA);
        TrainingSession s2 = new TrainingSession("s2", t2, MON_18, trainerA);
        TrainingSession s3 = new TrainingSession("s3", t3, MON_19, trainerA);
        TimeSlot mon20 = new TimeSlot("MON-20", DayOfWeek.MONDAY, 20);
        TrainingSession s4 = new TrainingSession("s4", t4, mon20, trainerA);

        // 4 sessions on Monday, max 3 → penalize by 1
        constraintVerifier.verifyThat(TrainingScheduleConstraintProvider::trainerMaxPerDay)
                .given(s1, s2, s3, s4)
                .penalizesBy(1);
    }

    // ===== S1: Team non-consecutive days =====

    @Test
    void teamNonConsecutiveDays_adjacentPenalized() {
        Team teamA = team("t1", "U10A", AgeGroup.U10, 14, 2,
                Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY), 17, 20);
        Trainer trainerA = trainer("tr1", "Jan", 6, Set.of(AgeGroup.U10), null);
        // Monday and Tuesday are consecutive
        TrainingSession s1 = new TrainingSession("s1", teamA, MON_17, trainerA);
        TrainingSession s2 = new TrainingSession("s2", teamA, TUE_17, trainerA);

        constraintVerifier.verifyThat(TrainingScheduleConstraintProvider::teamNonConsecutiveDays)
                .given(s1, s2)
                .penalizes(1);
    }

    @Test
    void teamNonConsecutiveDays_nonAdjacentOk() {
        Team teamA = team("t1", "U10A", AgeGroup.U10, 14, 2,
                Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY), 17, 20);
        Trainer trainerA = trainer("tr1", "Jan", 6, Set.of(AgeGroup.U10), null);
        // Monday and Wednesday are not consecutive
        TrainingSession s1 = new TrainingSession("s1", teamA, MON_17, trainerA);
        TrainingSession s2 = new TrainingSession("s2", teamA, WED_17, trainerA);

        constraintVerifier.verifyThat(TrainingScheduleConstraintProvider::teamNonConsecutiveDays)
                .given(s1, s2)
                .penalizes(0);
    }

    // ===== S2: Youngest teams first =====

    @Test
    void youngestTeamsFirst_youngTeamLateSlot() {
        // U6 team (maxAge=6) in a late slot (19) with earliest 17
        Team teamA = team("t1", "U6A", AgeGroup.U6, 10, 1,
                Set.of(DayOfWeek.MONDAY), 17, 20);
        Trainer trainerA = trainer("tr1", "Jan", 6, Set.of(AgeGroup.U6), null);
        TrainingSession session = new TrainingSession("s1", teamA, MON_19, trainerA);

        // ageFactor = 20 - 6 = 14, hourOffset = 19 - 17 = 2, penalty = 14 * 2 = 28
        constraintVerifier.verifyThat(TrainingScheduleConstraintProvider::youngestTeamsFirst)
                .given(session)
                .penalizesBy(28);
    }

    @Test
    void youngestTeamsFirst_youngTeamEarlySlot() {
        // U6 team in earliest slot → penalty = (20-6) * (17-17) = 0
        Team teamA = team("t1", "U6A", AgeGroup.U6, 10, 1,
                Set.of(DayOfWeek.MONDAY), 17, 20);
        Trainer trainerA = trainer("tr1", "Jan", 6, Set.of(AgeGroup.U6), null);
        TrainingSession session = new TrainingSession("s1", teamA, MON_17, trainerA);

        constraintVerifier.verifyThat(TrainingScheduleConstraintProvider::youngestTeamsFirst)
                .given(session)
                .penalizesBy(0);
    }

    // ===== S3: Trainer no gaps =====

    @Test
    void trainerNoGaps_gapExists() {
        Team t1 = team("t1", "U10A", AgeGroup.U10, 14, 1, Set.of(DayOfWeek.MONDAY), 17, 20);
        Team t2 = team("t2", "U12A", AgeGroup.U12, 16, 1, Set.of(DayOfWeek.MONDAY), 17, 20);
        Trainer trainerA = trainer("tr1", "Jan", 6, Set.of(AgeGroup.U10, AgeGroup.U12), null);
        // Sessions at 17 and 19, gap at 18 → span=3, count=2, gap=1
        TrainingSession s1 = new TrainingSession("s1", t1, MON_17, trainerA);
        TrainingSession s2 = new TrainingSession("s2", t2, MON_19, trainerA);

        constraintVerifier.verifyThat(TrainingScheduleConstraintProvider::trainerNoGaps)
                .given(s1, s2)
                .penalizesBy(1);
    }

    @Test
    void trainerNoGaps_consecutive() {
        Team t1 = team("t1", "U10A", AgeGroup.U10, 14, 1, Set.of(DayOfWeek.MONDAY), 17, 20);
        Team t2 = team("t2", "U12A", AgeGroup.U12, 16, 1, Set.of(DayOfWeek.MONDAY), 17, 20);
        Trainer trainerA = trainer("tr1", "Jan", 6, Set.of(AgeGroup.U10, AgeGroup.U12), null);
        // Sessions at 17 and 18, no gap → span=2, count=2, gap=0
        TrainingSession s1 = new TrainingSession("s1", t1, MON_17, trainerA);
        TrainingSession s2 = new TrainingSession("s2", t2, MON_18, trainerA);

        constraintVerifier.verifyThat(TrainingScheduleConstraintProvider::trainerNoGaps)
                .given(s1, s2)
                .penalizes(0);
    }

    // ===== S4: Trainer consistency =====

    @Test
    void trainerConsistency_differentTrainers() {
        Team teamA = team("t1", "U10A", AgeGroup.U10, 14, 2,
                Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY), 17, 20);
        Trainer trainerA = trainer("tr1", "Jan", 6, Set.of(AgeGroup.U10), null);
        Trainer trainerB = trainer("tr2", "Piet", 6, Set.of(AgeGroup.U10), null);
        TrainingSession s1 = new TrainingSession("s1", teamA, MON_17, trainerA);
        TrainingSession s2 = new TrainingSession("s2", teamA, WED_17, trainerB);

        constraintVerifier.verifyThat(TrainingScheduleConstraintProvider::trainerConsistency)
                .given(s1, s2)
                .penalizes(1);
    }

    @Test
    void trainerConsistency_sameTrainer() {
        Team teamA = team("t1", "U10A", AgeGroup.U10, 14, 2,
                Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY), 17, 20);
        Trainer trainerA = trainer("tr1", "Jan", 6, Set.of(AgeGroup.U10), null);
        TrainingSession s1 = new TrainingSession("s1", teamA, MON_17, trainerA);
        TrainingSession s2 = new TrainingSession("s2", teamA, WED_17, trainerA);

        constraintVerifier.verifyThat(TrainingScheduleConstraintProvider::trainerConsistency)
                .given(s1, s2)
                .penalizes(0);
    }

    // ===== S5: Trainer preferred team =====

    @Test
    void trainerPreferredTeam_matchRewards() {
        Team teamA = team("t1", "U10A", AgeGroup.U10, 14, 1,
                Set.of(DayOfWeek.MONDAY), 17, 20);
        Trainer trainerA = trainer("tr1", "Jan", 6, Set.of(AgeGroup.U10), "t1");
        TrainingSession session = new TrainingSession("s1", teamA, MON_17, trainerA);

        constraintVerifier.verifyThat(TrainingScheduleConstraintProvider::trainerPreferredTeam)
                .given(session)
                .rewards(1);
    }

    @Test
    void trainerPreferredTeam_noMatch() {
        Team teamA = team("t1", "U10A", AgeGroup.U10, 14, 1,
                Set.of(DayOfWeek.MONDAY), 17, 20);
        Trainer trainerA = trainer("tr1", "Jan", 6, Set.of(AgeGroup.U10), "t2");
        TrainingSession session = new TrainingSession("s1", teamA, MON_17, trainerA);

        constraintVerifier.verifyThat(TrainingScheduleConstraintProvider::trainerPreferredTeam)
                .given(session)
                .rewards(0);
    }

    @Test
    void trainerPreferredTeam_noPreference() {
        Team teamA = team("t1", "U10A", AgeGroup.U10, 14, 1,
                Set.of(DayOfWeek.MONDAY), 17, 20);
        Trainer trainerA = trainer("tr1", "Jan", 6, Set.of(AgeGroup.U10), null);
        TrainingSession session = new TrainingSession("s1", teamA, MON_17, trainerA);

        constraintVerifier.verifyThat(TrainingScheduleConstraintProvider::trainerPreferredTeam)
                .given(session)
                .rewards(0);
    }
}
