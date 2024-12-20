package com.yandex.kanban.service;

import com.yandex.kanban.model.Epic;
import com.yandex.kanban.model.Subtask;
import com.yandex.kanban.model.Task;
import com.yandex.kanban.model.TaskStatus;

import java.util.*;

public class InMemoryTaskManager implements TaskManager {

    protected int id = 0;
    protected final Map<Integer, Task> tasksMap = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistory();

    @Override
    public HistoryManager getHistoryManager() {
        return historyManager;
    }

    @Override
    public Task createTask(String title, String description) {
        Task task = new Task(title, description);
        task.setId(this.nextId());
        tasksMap.put(task.getId(), task);
        return task;
    }

    @Override
    public Epic createEpic(String title, String description) {
        Epic epic = new Epic(title, description);
        epic.setId(this.nextId());
        tasksMap.put(epic.getId(), epic);
        return epic;
    }

    @Override
    public Subtask createSubtask(String title, String description, int epicId) {
        if (!(tasksMap.get(epicId) instanceof Epic)) {
            throw new RuntimeException("wrong epic id");
        }
        Subtask subtask = new Subtask(title, description, epicId);
        subtask.setId(this.nextId());
        Epic epic = (Epic) tasksMap.get(epicId);
        epic.getSubtasksIds().add(subtask.getId());
        tasksMap.put(subtask.getId(), subtask);
        setEpicStatus(epicId);
        return subtask;
    }

    @Override
    public int addTask(Task task) {
        if (task == null) {
            return -1;
        }
        if (task instanceof Epic) {
            return addEpic((Epic) task);
        }
        if (task instanceof Subtask) {
            return addSubtask((Subtask) task);
        }
        task.setId(this.nextId());
        tasksMap.put(task.getId(), task);
        return task.getId();
    }

    @Override
    public int addEpic(Epic epic) {
        if (epic == null) {
            return -1;
        }
        epic.setId(this.nextId());
        epic.setStatus(TaskStatus.NEW);
        epic.setSubtasksIds(new ArrayList<>());
        tasksMap.put(epic.getId(), epic);
        return epic.getId();
    }

    @Override
    public int addSubtask(Subtask subtask) {
        if (subtask == null) {
            return -1;
        }
        int epicId = subtask.getEpicId();
        if (!(tasksMap.get(epicId) instanceof Epic)) {
            throw new RuntimeException("wrong epic id");
        }
        subtask.setId(nextId());
        ((Epic) tasksMap.get(epicId)).getSubtasksIds().add(subtask.getId());
        tasksMap.put(subtask.getId(), subtask);
        setEpicStatus(epicId);
        return subtask.getId();
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasksMap.get(id);
        if (task != null) {
            historyManager.add(task);
        }
        return task;
    }

    @Override
    public List<Task> getAllKindOfTasks() {
        return tasksMap.values().stream().toList();
    }

    @Override
    public List<Task> getAllTasks() {
        return tasksMap.values().stream().filter(el -> !(el instanceof Epic) && !(el instanceof Subtask)).toList();
    }

    @Override
    public List<Epic> getAllEpics() {
        return tasksMap.values().stream().filter(el -> el instanceof Epic).map(el -> (Epic) el).toList();
    }

    @Override
    public List<Subtask> getAllSubtasks() {
        return tasksMap.values().stream().filter(el -> el instanceof Subtask).map(el -> (Subtask) el).toList();
    }

    @Override
    public List<Subtask> getAllSubtasksByEpicId(int epicId) {
        if (!tasksMap.containsKey(id)) {
            throw new NoSuchElementException("can't find task with id " + id);
        }
        Task task = tasksMap.get(epicId);
        if (!(task instanceof Epic)) {
            throw new RuntimeException("wrong epic id");
        }
        Epic epic = (Epic) task;
        List<Subtask> subtasks = new ArrayList<>();
        for (int i : epic.getSubtasksIds()) {
            subtasks.add((Subtask) tasksMap.get(i));
        }
        return subtasks;
    }

    @Override
    public void removeAllTasks() {
        tasksMap.clear();
        historyManager.clear();
    }

    @Override
    public void removeTaskById(int id) {
        historyManager.remove(id);
        if (tasksMap.get(id) instanceof Epic) {
            removeAllSubtasks(id);
        }
        if (tasksMap.get(id) instanceof Subtask) {
            Subtask subtask = (Subtask) tasksMap.get(id);
            Epic epic = (Epic) tasksMap.get(subtask.getEpicId());
            epic.getSubtasksIds().remove((Integer) id);
            setEpicStatus(epic.getId());
        }
        tasksMap.remove(id);
    }

    @Override
    public void updateTitle(String title, int id) {
        if (!tasksMap.containsKey(id)) {
            throw new NoSuchElementException("can't find task with id " + id);
        }
        tasksMap.get(id).setTitle(title);
    }

    @Override
    public void updateDescription(String description, int id) {
        if (!tasksMap.containsKey(id)) {
            throw new NoSuchElementException("can't find task with id " + id);
        }
        tasksMap.get(id).setDescription(description);
    }

    @Override
    public void updateStatus(TaskStatus status, int id) {
        if (!tasksMap.containsKey(id)) {
            throw new NoSuchElementException("can't find task with id " + id);
        }
        if (tasksMap.get(id) instanceof Epic) {
            throw new RuntimeException("can't set epic status");
        }
        tasksMap.get(id).setStatus(status);
        if (tasksMap.get(id) instanceof Subtask) {
            int epicId = ((Subtask) tasksMap.get(id)).getEpicId();
            setEpicStatus(epicId);
        }
    }

    private int nextId() {
        return ++id;
    }

    private void setEpicStatus(int id) {
        List<Integer> subsIds = ((Epic) tasksMap.get(id)).getSubtasksIds();
        int countNEW = 0;
        int countDONE = 0;
        for (Integer subId : subsIds) {
            TaskStatus status = tasksMap.get(subId).getStatus();
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
            tasksMap.get(id).setStatus(TaskStatus.NEW);
        } else if (countDONE == subsIds.size()) {
            tasksMap.get(id).setStatus(TaskStatus.DONE);
        } else {
            tasksMap.get(id).setStatus(TaskStatus.IN_PROGRESS);
        }
    }

    private void removeAllSubtasks(int epicId) {
        List<Integer> subtasksIds = ((Epic) tasksMap.get(epicId)).getSubtasksIds();
        for (Integer subId : subtasksIds) {
            historyManager.remove(subId);
            tasksMap.remove(subId);
        }
    }

}
