package be.dmon.scheduler.domain;

import java.util.Set;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;

public class Trainer {

    @PlanningId
    private String id;
    private String name;
    private int maxHoursPerWeek;
    private Set<AgeGroup> trainableAgeGroups;
    private String preferredTeamId;

    public Trainer() {
    }

    public Trainer(String id, String name, int maxHoursPerWeek, Set<AgeGroup> trainableAgeGroups,
            String preferredTeamId) {
        this.id = id;
        this.name = name;
        this.maxHoursPerWeek = maxHoursPerWeek;
        this.trainableAgeGroups = trainableAgeGroups;
        this.preferredTeamId = preferredTeamId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMaxHoursPerWeek() {
        return maxHoursPerWeek;
    }

    public void setMaxHoursPerWeek(int maxHoursPerWeek) {
        this.maxHoursPerWeek = maxHoursPerWeek;
    }

    public Set<AgeGroup> getTrainableAgeGroups() {
        return trainableAgeGroups;
    }

    public void setTrainableAgeGroups(Set<AgeGroup> trainableAgeGroups) {
        this.trainableAgeGroups = trainableAgeGroups;
    }

    public String getPreferredTeamId() {
        return preferredTeamId;
    }

    public void setPreferredTeamId(String preferredTeamId) {
        this.preferredTeamId = preferredTeamId;
    }

    @Override
    public String toString() {
        return name;
    }
}
