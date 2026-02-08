package be.dmon.scheduler.domain;

import java.time.DayOfWeek;
import java.util.Set;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;

public class Team {

    @PlanningId
    private String id;
    private String name;
    private AgeGroup ageGroup;
    private int size;
    private int trainingsPerWeek;
    private Set<DayOfWeek> availableDays;
    private int earliestHour;
    private int latestHour;

    public Team() {
    }

    public Team(String id, String name, AgeGroup ageGroup, int size, int trainingsPerWeek,
            Set<DayOfWeek> availableDays, int earliestHour, int latestHour) {
        this.id = id;
        this.name = name;
        this.ageGroup = ageGroup;
        this.size = size;
        this.trainingsPerWeek = trainingsPerWeek;
        this.availableDays = availableDays;
        this.earliestHour = earliestHour;
        this.latestHour = latestHour;
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

    public AgeGroup getAgeGroup() {
        return ageGroup;
    }

    public void setAgeGroup(AgeGroup ageGroup) {
        this.ageGroup = ageGroup;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getTrainingsPerWeek() {
        return trainingsPerWeek;
    }

    public void setTrainingsPerWeek(int trainingsPerWeek) {
        this.trainingsPerWeek = trainingsPerWeek;
    }

    public Set<DayOfWeek> getAvailableDays() {
        return availableDays;
    }

    public void setAvailableDays(Set<DayOfWeek> availableDays) {
        this.availableDays = availableDays;
    }

    public int getEarliestHour() {
        return earliestHour;
    }

    public void setEarliestHour(int earliestHour) {
        this.earliestHour = earliestHour;
    }

    public int getLatestHour() {
        return latestHour;
    }

    public void setLatestHour(int latestHour) {
        this.latestHour = latestHour;
    }

    @Override
    public String toString() {
        return name;
    }
}
