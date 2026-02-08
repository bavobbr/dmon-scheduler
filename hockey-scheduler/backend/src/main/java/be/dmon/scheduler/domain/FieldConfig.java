package be.dmon.scheduler.domain;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;

public class FieldConfig {

    @PlanningId
    private String id;
    private int capacity;

    public FieldConfig() {
    }

    public FieldConfig(String id, int capacity) {
        this.id = id;
        this.capacity = capacity;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
}
