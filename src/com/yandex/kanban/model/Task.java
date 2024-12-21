package com.yandex.kanban.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public class Task {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    private String title;
    private String description;
    private int id;
    private TaskStatus status;
    private LocalDateTime startTime;
    private Duration duration;

    public Task(String title, String description) {
        this.title = title;
        this.description = description;
        this.id = -1;
        this.status = TaskStatus.NEW;
        this.duration = Duration.ZERO;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        if (duration.isNegative()) {
            throw new IllegalArgumentException("Отрицательная длительность выпполнения задачи");
        }
        this.duration = duration;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        String startTimeStr = startTime == null ? "NoData" : FORMATTER.format(startTime);
        String durationStr = duration.toDaysPart() + "days " + duration.toHoursPart() + "hours "
                + duration.toMinutesPart() + "minutes";

        return "Task{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", id=" + id +
                ", status=" + status +
                ", startTime=" + startTimeStr +
                ", duration=" + durationStr +
                '}';
    }
}
