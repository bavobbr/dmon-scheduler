package be.dmon.scheduler.rest;

import java.util.List;

import be.dmon.scheduler.domain.Team;
import be.dmon.scheduler.domain.TimeSlot;
import be.dmon.scheduler.domain.Trainer;

public class InputDataset {

    private List<Trainer> trainers;
    private List<Team> teams;
    private List<TimeSlot> timeSlots;
    private int fieldCapacity;

    public InputDataset() {
    }

    public InputDataset(List<Trainer> trainers, List<Team> teams, List<TimeSlot> timeSlots, int fieldCapacity) {
        this.trainers = trainers;
        this.teams = teams;
        this.timeSlots = timeSlots;
        this.fieldCapacity = fieldCapacity;
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

    public int getFieldCapacity() {
        return fieldCapacity;
    }

    public void setFieldCapacity(int fieldCapacity) {
        this.fieldCapacity = fieldCapacity;
    }
}
