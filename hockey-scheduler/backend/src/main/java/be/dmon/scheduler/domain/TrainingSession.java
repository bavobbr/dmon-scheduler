package be.dmon.scheduler.domain;

import ai.timefold.solver.core.api.domain.entity.PlanningEntity;
import ai.timefold.solver.core.api.domain.lookup.PlanningId;
import ai.timefold.solver.core.api.domain.variable.PlanningVariable;

@PlanningEntity
public class TrainingSession {

    @PlanningId
    private String id;

    private Team team;

    @PlanningVariable
    private TimeSlot timeSlot;

    @PlanningVariable
    private Trainer trainer;

    public TrainingSession() {
    }

    public TrainingSession(String id, Team team) {
        this.id = id;
        this.team = team;
    }

    public TrainingSession(String id, Team team, TimeSlot timeSlot, Trainer trainer) {
        this.id = id;
        this.team = team;
        this.timeSlot = timeSlot;
        this.trainer = trainer;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public TimeSlot getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(TimeSlot timeSlot) {
        this.timeSlot = timeSlot;
    }

    public Trainer getTrainer() {
        return trainer;
    }

    public void setTrainer(Trainer trainer) {
        this.trainer = trainer;
    }

    @Override
    public String toString() {
        return team + " @ " + timeSlot + " [" + trainer + "]";
    }
}
