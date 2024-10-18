package com.yandex.kanban.model;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {

    private List<Integer> subtasksIds = new ArrayList<>();

    public Epic(String title, String description) {
        super(title, description);
    }

    public List<Integer> getSubtasksIds() {
        return subtasksIds;
    }

    public void setSubtasksIds(List<Integer> subtasksIds) {
        this.subtasksIds = subtasksIds;
    }

    @Override
    public String toString() {
        return super.toString().replaceFirst("Task", "Epic").replace("}","") +
                ", subtasksIds=" + subtasksIds +
                '}';
    }
}
