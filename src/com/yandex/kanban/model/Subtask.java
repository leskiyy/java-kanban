package com.yandex.kanban.model;

import com.yandex.kanban.util.TaskTypes;

public class Subtask extends Task {

    private int epicId;

    public Subtask(String title, String description, int epicId) {
        super(title, description);
        this.epicId = epicId;
    }

    public Subtask(String title, String description) {
        super(title, description);
        this.epicId = -1;
    }

    @Override
    public TaskTypes getType() {
        return TaskTypes.SUBTASK;
    }

    public int getEpicId() {
        return epicId;
    }

    public void setEpicId(int epicId) {
        this.epicId = epicId;
    }

    @Override
    public String toString() {
        return super.toString().replaceFirst("Task", "Subtask").replace("}", "") +
                ", epicId=" + epicId +
                '}';
    }
}
