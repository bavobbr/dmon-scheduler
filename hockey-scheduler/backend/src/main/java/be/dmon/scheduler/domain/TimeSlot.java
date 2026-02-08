package be.dmon.scheduler.domain;

import java.time.DayOfWeek;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;

public class TimeSlot {

    @PlanningId
    private String id;
    private DayOfWeek dayOfWeek;
    private int startHour;

    public TimeSlot() {
    }

    public TimeSlot(String id, DayOfWeek dayOfWeek, int startHour) {
        this.id = id;
        this.dayOfWeek = dayOfWeek;
        this.startHour = startHour;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public int getStartHour() {
        return startHour;
    }

    public void setStartHour(int startHour) {
        this.startHour = startHour;
    }

    public int getEndHour() {
        return startHour + 1;
    }

    @Override
    public String toString() {
        return dayOfWeek + " " + startHour + ":00";
    }
}
