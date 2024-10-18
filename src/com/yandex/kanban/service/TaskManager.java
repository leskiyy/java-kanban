package com.yandex.kanban.service;

import com.yandex.kanban.model.Epic;
import com.yandex.kanban.model.Subtask;
import com.yandex.kanban.model.Task;
import com.yandex.kanban.model.TaskStatus;

import java.util.*;

public class TaskManager {

    private int id = 0;
    private Map<Integer, Task> tasksMap = new HashMap<>();

    public Task createTask(String title, String description) {
        Task task = new Task(title, description);
        task.setId(this.nextId());
        tasksMap.put(task.getId(), task);
        return task;
    }

    public Epic createEpic(String title, String description) {
        Epic epic = new Epic(title, description);
        epic.setId(this.nextId());
        tasksMap.put(epic.getId(), epic);
        return epic;
    }

    public Subtask createSubtask(String title, String description, int epicId) {
        if (!(getTaskById(epicId) instanceof Epic)) {
            throw new RuntimeException("wrong epic id");
        }
        Subtask subtask = new Subtask(title, description, epicId);
        subtask.setId(this.nextId());
        Epic epic = (Epic) getTaskById(epicId);
        epic.getSubtasksIds().add(subtask.getId());
        tasksMap.put(subtask.getId(), subtask);
        checkEpicStatus(epicId);
        return subtask;
    }

    public Task getTaskById(int id) {
        return tasksMap.get(id);
    }

    public List<Task> getAllKindOfTasks() {
        return tasksMap.values().stream().toList();
    }

    public List<Task> getAllTasks() {
        return tasksMap.values().stream().filter(el -> !(el instanceof Epic) && !(el instanceof Subtask)).toList();
    }

    public List<Task> getAllEpics() {
        return tasksMap.values().stream().filter(el -> el instanceof Epic).toList();
    }

    public List<Task> getAllSubtasks() {
        return tasksMap.values().stream().filter(el -> el instanceof Subtask).toList();
    }

    public List<Task> getAllSubtasksByEpicId(int epicId) {
        Epic epic = (Epic) getTaskById(epicId);
        List<Task> subtasks = new ArrayList<>();
        for (int i : epic.getSubtasksIds()) {
            subtasks.add(getTaskById(i));
        }
        return subtasks;
    }

    public void removeAllTasks() {
        tasksMap.clear();
    }

    public void removeTaskById(int id) {
        if (getTaskById(id) instanceof Epic) {
            removeAllSubtasks(id);
        }
        if (getTaskById(id) instanceof Subtask) {
            Subtask subtask = (Subtask) getTaskById(id);
            Epic epic = (Epic) getTaskById(subtask.getEpicId());
            epic.getSubtasksIds().remove((Integer) id);
            checkEpicStatus(epic.getId());
        }
        tasksMap.remove(id);
    }

    private void removeAllSubtasks(int epicId) {
        List<Integer> subtasksIds = ((Epic) getTaskById(epicId)).getSubtasksIds();
        for (Integer subId : subtasksIds) {
            tasksMap.remove(subId);
        }
    }

    public void updateTitle(String title, int id) {
        tasksMap.get(id).setTitle(title);
    }

    public void updateDescription(String description, int id) {
        tasksMap.get(id).setDescription(description);
    }

    public void updateStatus(TaskStatus status, int id) {
        if (getTaskById(id) instanceof Epic) {
            throw new RuntimeException("can't set epic status");
        }
        getTaskById(id).setStatus(status);
        if (getTaskById(id) instanceof Subtask) {
            int epicId = ((Subtask) getTaskById(id)).getEpicId();
            checkEpicStatus(epicId);
        }
    }

    private int nextId() {
        return ++id;
    }

    private void checkEpicStatus(int id) {
        List<Integer> subsIds = ((Epic) getTaskById(id)).getSubtasksIds();
        int countNEW = 0;
        int countDONE = 0;
        for (Integer subId : subsIds) {
            TaskStatus status = getTaskById(subId).getStatus();
            switch (status) {
                case NEW:
                    countNEW++;
                    break;
                case DONE:
                    countDONE++;
                    break;
            }
        }
        if (countNEW == subsIds.size()) {
            getTaskById(id).setStatus(TaskStatus.NEW);
        } else if (countDONE == subsIds.size()) {
            getTaskById(id).setStatus(TaskStatus.DONE);
        } else {
            getTaskById(id).setStatus(TaskStatus.IN_PROGRESS);
        }
    }

}
