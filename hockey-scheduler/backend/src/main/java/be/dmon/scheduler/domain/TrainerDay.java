package be.dmon.scheduler.domain;

import java.time.DayOfWeek;

public record TrainerDay(String trainerId, DayOfWeek day) {
}
