package com.yandex.kanban.service;

import com.yandex.kanban.model.Epic;
import com.yandex.kanban.model.Subtask;
import com.yandex.kanban.model.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    private final List<Task> history = new ArrayList<>();

    @Override
    public void add(Task task) {
        if (history.size() == 10) {
            history.removeFirst();
        }
        Task copy = this.copyTask(task);
        history.add(copy);
    }

    @Override
    public List<Task> getHistory() {
        return history;
    }

    private Task copyTask(Task task) {
        if (task instanceof Epic) {
            Epic original = (Epic) task;
            Epic epic = new Epic(task.getTitle(), task.getDescription());
            epic.setSubtasksIds(new ArrayList<>(original.getSubtasksIds()));
            epic.setId(original.getId());
            epic.setStatus(original.getStatus());
            return epic;
        }
        if (task instanceof Subtask) {
            Subtask original = (Subtask) task;
            Subtask subtask = new Subtask(task.getTitle(), task.getDescription(), original.getEpicId());
            subtask.setId(original.getId());
            subtask.setStatus(original.getStatus());
            return subtask;
        }
        Task copy = new Task(task.getTitle(), task.getDescription());
        copy.setId(task.getId());
        copy.setStatus(task.getStatus());
        return copy;
    }
}
