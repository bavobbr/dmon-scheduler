package be.dmon.scheduler.solver;

import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.score.stream.Constraint;
import ai.timefold.solver.core.api.score.stream.ConstraintCollectors;
import ai.timefold.solver.core.api.score.stream.ConstraintFactory;
import ai.timefold.solver.core.api.score.stream.ConstraintProvider;
import ai.timefold.solver.core.api.score.stream.Joiners;

import be.dmon.scheduler.domain.FieldConfig;
import be.dmon.scheduler.domain.TrainerDay;
import be.dmon.scheduler.domain.TrainingSession;

public class TrainingScheduleConstraintProvider implements ConstraintProvider {

    @Override
    public Constraint[] defineConstraints(ConstraintFactory factory) {
        return new Constraint[] {
                // Hard constraints
                teamSlotAvailability(factory),
                trainerQualification(factory),
                trainerConflict(factory),
                teamConflict(factory),
                teamSeparateDays(factory),
                fieldCapacity(factory),
                trainerMaxHours(factory),
                trainerMaxPerDay(factory),
                // Soft constraints
                teamNonConsecutiveDays(factory),
                youngestTeamsFirst(factory),
                trainerNoGaps(factory),
                trainerConsistency(factory),
                trainerPreferredTeam(factory),
        };
    }

    // ===== Hard Constraints =====

    // H1: Session must be within team's available days and hour range
    Constraint teamSlotAvailability(ConstraintFactory factory) {
        return factory.forEach(TrainingSession.class)
                .filter(session ->
                        !session.getTeam().getAvailableDays().contains(session.getTimeSlot().getDayOfWeek())
                        || session.getTimeSlot().getStartHour() < session.getTeam().getEarliestHour()
                        || session.getTimeSlot().getStartHour() >= session.getTeam().getLatestHour())
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Team slot availability");
    }

    // H2: Trainer must be qualified for team's age group
    Constraint trainerQualification(ConstraintFactory factory) {
        return factory.forEach(TrainingSession.class)
                .filter(session ->
                        !session.getTrainer().getTrainableAgeGroups().contains(session.getTeam().getAgeGroup()))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Trainer qualification");
    }

    // H3: Same trainer cannot be assigned to two sessions at the same time
    Constraint trainerConflict(ConstraintFactory factory) {
        return factory.forEachUniquePair(TrainingSession.class,
                        Joiners.equal(TrainingSession::getTrainer),
                        Joiners.equal(TrainingSession::getTimeSlot))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Trainer conflict");
    }

    // H4: Same team cannot have two sessions at the same time
    Constraint teamConflict(ConstraintFactory factory) {
        return factory.forEachUniquePair(TrainingSession.class,
                        Joiners.equal(TrainingSession::getTeam),
                        Joiners.equal(TrainingSession::getTimeSlot))
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Team conflict");
    }

    // H5: Same team's sessions must be on different days
    Constraint teamSeparateDays(ConstraintFactory factory) {
        return factory.forEachUniquePair(TrainingSession.class,
                        Joiners.equal(TrainingSession::getTeam))
                .filter((s1, s2) -> s1.getTimeSlot().getDayOfWeek() == s2.getTimeSlot().getDayOfWeek())
                .penalize(HardSoftScore.ONE_HARD)
                .asConstraint("Team separate days");
    }

    // H6: Total players on field at any time slot must not exceed capacity
    Constraint fieldCapacity(ConstraintFactory factory) {
        return factory.forEach(TrainingSession.class)
                .groupBy(TrainingSession::getTimeSlot,
                        ConstraintCollectors.sum(session -> session.getTeam().getSize()))
                .join(FieldConfig.class)
                .filter((slot, totalPlayers, config) -> totalPlayers > config.getCapacity())
                .penalize(HardSoftScore.ONE_HARD,
                        (slot, totalPlayers, config) -> totalPlayers - config.getCapacity())
                .asConstraint("Field capacity");
    }

    // H7: Trainer must not exceed max hours per week
    Constraint trainerMaxHours(ConstraintFactory factory) {
        return factory.forEach(TrainingSession.class)
                .groupBy(TrainingSession::getTrainer, ConstraintCollectors.count())
                .filter((trainer, count) -> count > trainer.getMaxHoursPerWeek())
                .penalize(HardSoftScore.ONE_HARD,
                        (trainer, count) -> count - trainer.getMaxHoursPerWeek())
                .asConstraint("Trainer max hours");
    }

    // H8: Trainer must not have more than 3 sessions per day
    Constraint trainerMaxPerDay(ConstraintFactory factory) {
        return factory.forEach(TrainingSession.class)
                .groupBy(TrainingSession::getTrainer,
                        session -> session.getTimeSlot().getDayOfWeek(),
                        ConstraintCollectors.count())
                .filter((trainer, day, count) -> count > 3)
                .penalize(HardSoftScore.ONE_HARD,
                        (trainer, day, count) -> count - 3)
                .asConstraint("Trainer max 3 per day");
    }

    // ===== Soft Constraints =====

    // S1: Team sessions should not be on consecutive days (weight 3)
    Constraint teamNonConsecutiveDays(ConstraintFactory factory) {
        return factory.forEachUniquePair(TrainingSession.class,
                        Joiners.equal(TrainingSession::getTeam))
                .filter((s1, s2) -> {
                    int day1 = s1.getTimeSlot().getDayOfWeek().getValue();
                    int day2 = s2.getTimeSlot().getDayOfWeek().getValue();
                    int diff = Math.abs(day1 - day2);
                    // Adjacent days: diff==1, or wrap-around (Sun=7 and Mon=1 → diff==6)
                    return diff == 1 || diff == 6;
                })
                .penalize(HardSoftScore.ofSoft(3))
                .asConstraint("Team non-consecutive days");
    }

    // S2: Younger teams should train earlier (weight 1)
    Constraint youngestTeamsFirst(ConstraintFactory factory) {
        return factory.forEach(TrainingSession.class)
                .penalize(HardSoftScore.ONE_SOFT,
                        session -> {
                            int ageFactor = 20 - session.getTeam().getAgeGroup().getMaxAge();
                            if (ageFactor < 0) {
                                ageFactor = 0;
                            }
                            int hourOffset = session.getTimeSlot().getStartHour()
                                    - session.getTeam().getEarliestHour();
                            if (hourOffset < 0) {
                                hourOffset = 0;
                            }
                            return ageFactor * hourOffset;
                        })
                .asConstraint("Youngest teams first");
    }

    // S3: Trainer should not have gaps between sessions on the same day (weight 4)
    Constraint trainerNoGaps(ConstraintFactory factory) {
        return factory.forEach(TrainingSession.class)
                .groupBy(
                        (TrainingSession session) -> new TrainerDay(session.getTrainer().getId(),
                                session.getTimeSlot().getDayOfWeek()),
                        ConstraintCollectors.<TrainingSession, Integer>min(
                                session -> session.getTimeSlot().getStartHour()),
                        ConstraintCollectors.<TrainingSession, Integer>max(
                                session -> session.getTimeSlot().getStartHour()),
                        ConstraintCollectors.count())
                .filter((trainerDay, minHour, maxHour, count) -> {
                    int span = maxHour - minHour + 1;
                    return span > count; // gap exists
                })
                .penalize(HardSoftScore.ofSoft(4),
                        (trainerDay, minHour, maxHour, count) -> (maxHour - minHour + 1) - count)
                .asConstraint("Trainer no gaps");
    }

    // S4: Same team should have the same trainer across sessions (weight 1)
    Constraint trainerConsistency(ConstraintFactory factory) {
        return factory.forEachUniquePair(TrainingSession.class,
                        Joiners.equal(TrainingSession::getTeam))
                .filter((s1, s2) -> !s1.getTrainer().getId().equals(s2.getTrainer().getId()))
                .penalize(HardSoftScore.ONE_SOFT)
                .asConstraint("Trainer consistency");
    }

    // S5: Trainer should be assigned to their preferred team (weight 5)
    Constraint trainerPreferredTeam(ConstraintFactory factory) {
        return factory.forEach(TrainingSession.class)
                .filter(session -> session.getTrainer().getPreferredTeamId() != null
                        && session.getTrainer().getPreferredTeamId().equals(session.getTeam().getId()))
                .reward(HardSoftScore.ofSoft(5))
                .asConstraint("Trainer preferred team");
    }
}
