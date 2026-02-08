package be.dmon.scheduler.domain;

import java.util.List;

import ai.timefold.solver.core.api.domain.solution.PlanningEntityCollectionProperty;
import ai.timefold.solver.core.api.domain.solution.PlanningScore;
import ai.timefold.solver.core.api.domain.solution.PlanningSolution;
import ai.timefold.solver.core.api.domain.solution.ProblemFactCollectionProperty;
import ai.timefold.solver.core.api.domain.valuerange.ValueRangeProvider;
import ai.timefold.solver.core.api.score.buildin.hardsoft.HardSoftScore;
import ai.timefold.solver.core.api.solver.SolverStatus;

@PlanningSolution
public class TrainingSchedule {

    @ProblemFactCollectionProperty
    @ValueRangeProvider
    private List<Trainer> trainers;

    @ProblemFactCollectionProperty
    private List<Team> teams;

    @ProblemFactCollectionProperty
    @ValueRangeProvider
    private List<TimeSlot> timeSlots;

    @ProblemFactCollectionProperty
    private List<FieldConfig> fieldConfigs;

    @PlanningEntityCollectionProperty
    private List<TrainingSession> sessions;

    @PlanningScore
    private HardSoftScore score;

    private SolverStatus solverStatus;

    public TrainingSchedule() {
    }

    public TrainingSchedule(List<Trainer> trainers, List<Team> teams, List<TimeSlot> timeSlots,
            List<FieldConfig> fieldConfigs, List<TrainingSession> sessions) {
        this.trainers = trainers;
        this.teams = teams;
        this.timeSlots = timeSlots;
        this.fieldConfigs = fieldConfigs;
        this.sessions = sessions;
    }

    public TrainingSchedule(HardSoftScore score, SolverStatus solverStatus) {
        this.score = score;
        this.solverStatus = solverStatus;
    }

    public List<Trainer> getTrainers() {
        return trainers;
    }

    public void setTrainers(List<Trainer> trainers) {
        this.trainers = trainers;
    }

    public List<Team> getTeams() {
        return teams;
    }

    public void setTeams(List<Team> teams) {
        this.teams = teams;
    }

    public List<TimeSlot> getTimeSlots() {
        return timeSlots;
    }

    public void setTimeSlots(List<TimeSlot> timeSlots) {
        this.timeSlots = timeSlots;
    }

    public List<FieldConfig> getFieldConfigs() {
        return fieldConfigs;
    }

    public void setFieldConfigs(List<FieldConfig> fieldConfigs) {
        this.fieldConfigs = fieldConfigs;
    }

    public List<TrainingSession> getSessions() {
        return sessions;
    }

    public void setSessions(List<TrainingSession> sessions) {
        this.sessions = sessions;
    }

    public HardSoftScore getScore() {
        return score;
    }

    public void setScore(HardSoftScore score) {
        this.score = score;
    }

    public SolverStatus getSolverStatus() {
        return solverStatus;
    }

    public void setSolverStatus(SolverStatus solverStatus) {
        this.solverStatus = solverStatus;
    }
}
