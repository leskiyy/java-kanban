package com.yandex.kanban.model;

import com.yandex.kanban.util.TaskTypes;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {

    private List<Integer> subtasksIds = new ArrayList<>();

    public Epic(String title, String description) {
        super(title, description);
    }

    @Override
    public TaskTypes getType() {
        return TaskTypes.EPIC;
    }

    public List<Integer> getSubtasksIds() {
        return subtasksIds;
    }

    public void setSubtasksIds(List<Integer> subtasksIds) {
        this.subtasksIds = subtasksIds;
    }

    @Override
    public void setDuration(Duration duration) {
        super.setDuration(duration);
    }

    @Override
    public void setStartTime(LocalDateTime startTime) {
        super.setStartTime(startTime);
    }

    @Override
    public String toString() {
        return super.toString().replaceFirst("Task", "Epic").replace("}", "") +
                ", subtasksIds=" + subtasksIds +
                '}';
    }
}
