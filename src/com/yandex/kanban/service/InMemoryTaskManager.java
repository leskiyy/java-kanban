package com.yandex.kanban.service;

import com.yandex.kanban.model.Epic;
import com.yandex.kanban.model.Subtask;
import com.yandex.kanban.model.Task;
import com.yandex.kanban.model.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.BiPredicate;

public class InMemoryTaskManager implements TaskManager {

    protected int id = 0;
    protected final Map<Integer, Task> tasksMap = new HashMap<>();
    protected final Set<Task> prioritizedTasks = new TreeSet<>(Comparator
            .comparing(Task::getStartTime)
            .thenComparing(Task::getDuration)
            .thenComparing(Task::getId));
    protected final HistoryManager historyManager = Managers.getDefaultHistory();

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
        setEpicStatus(epic);
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
        addTaskToPrioritizedTasks(task);
        return task.getId();
    }

    @Override
    public int addEpic(Epic epic) {
        if (epic == null) {
            return -1;
        }
        epic.setId(this.nextId());
        epic.setStatus(TaskStatus.NEW);
        epic.setStartTime(null);
        epic.setDuration(Duration.ZERO);
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
        Epic epic = (Epic) tasksMap.get(epicId);
        epic.getSubtasksIds().add(subtask.getId());
        tasksMap.put(subtask.getId(), subtask);
        addTaskToPrioritizedTasks(subtask);
        setEpicStatus(epic);
        setEpicDuration(epic);
        setEpicStartTime(epic);
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
    public Set<Task> getPrioritizedTasks() {
        return prioritizedTasks;
    }

    @Override
    public void removeAllTasks() {
        tasksMap.clear();
        historyManager.clear();
        prioritizedTasks.clear();
    }

    @Override
    public void removeTaskById(int id) {
        prioritizedTasks.remove(tasksMap.get(id));
        historyManager.remove(id);
        if (tasksMap.get(id) instanceof Epic) {
            removeAllSubtasks(id);
        }
        if (tasksMap.get(id) instanceof Subtask) {
            Subtask subtask = (Subtask) tasksMap.get(id);
            Epic epic = (Epic) tasksMap.get(subtask.getEpicId());
            epic.getSubtasksIds().remove((Integer) id);
            setEpicStatus(epic);
            setEpicDuration(epic);
            setEpicStartTime(epic);
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
        if (status == TaskStatus.DONE) {
            prioritizedTasks.remove(tasksMap.get(id));
        }
        if (tasksMap.get(id) instanceof Subtask) {
            int epicId = ((Subtask) tasksMap.get(id)).getEpicId();
            Epic epic = (Epic) tasksMap.get(epicId);
            setEpicStatus(epic);
            setEpicDuration(epic);
            setEpicStartTime(epic);
        }
    }

    @Override
    public void updateStartTime(LocalDateTime startTime, int id) {
        Task task = tasksMap.get(id);
        if (task == null) {
            throw new NoSuchElementException("can't find task with id " + id);
        }
        if (task instanceof Epic) {
            throw new RuntimeException("can't set epic's start time");
        }
        LocalDateTime oldStartTime = task.getStartTime();
        task.setStartTime(startTime);
        if (oldStartTime == null) {
            addTaskToPrioritizedTasks(task);
        } else {
            if (isCrossingTasks(task)) {
                task.setStartTime(oldStartTime);
                prioritizedTasks.add(task);
                throw new RuntimeException("task id=" + id + " is crossing existing ones because of new start time");
            } else {
                prioritizedTasks.add(task);
            }
        }
    }

    @Override
    public void updateDuration(Duration newDuration, int id) {
        Task task = tasksMap.get(id);
        if (task instanceof Epic) {
            throw new RuntimeException("can't set epic's duration");
        }
        if (task.getStartTime() == null) {
            task.setDuration(newDuration);
            return;
        }
        Duration oldDuration = task.getDuration();
        prioritizedTasks.remove(task);
        task.setDuration(newDuration);
        if (isCrossingTasks(task)) {
            task.setDuration(oldDuration);
            prioritizedTasks.add(task);
            throw new RuntimeException("task id=" + id + " is crossing existing ones because of new duration");
        } else {
            prioritizedTasks.add(task);
        }
    }

    @Override
    public LocalDateTime getEndTime(int id) {
        Task task = tasksMap.get(id);
        if (task.getStartTime() == null) {
            return null;
        }
        if (task instanceof Epic) {
            return getEpicEndTime((Epic) task);
        }
        return task.getStartTime().plus(task.getDuration());
    }

    private boolean isCrossingTasks(Task task) {
        BiPredicate<Task, Task> isCrossingForEarlierPrioritizedTask = (inputTask, prioritizedTask) ->
                prioritizedTask.getStartTime().plus(prioritizedTask.getDuration()).isAfter(inputTask.getStartTime());
        BiPredicate<Task, Task> isCrossingForLaterPrioritizedTask = (inputTask, prioritizedTask) ->
                prioritizedTask.getStartTime().isBefore(inputTask.getStartTime().plus(inputTask.getDuration()));
        BiPredicate<Task, Task> isCrossingForEqualsStartTimePrioritizedTask = (inputTask, prioritizedTask) ->
                !inputTask.getDuration().isZero() && !prioritizedTask.getDuration().isZero();
        return prioritizedTasks.stream().anyMatch(el -> {
            if (el.getStartTime().isBefore(task.getStartTime())) {
                return isCrossingForEarlierPrioritizedTask.test(task, el);
            } else if (el.getStartTime().isAfter(task.getStartTime())) {
                return isCrossingForLaterPrioritizedTask.test(task, el);
            } else {
                return isCrossingForEqualsStartTimePrioritizedTask.test(task, el);
            }
        });
    }

    private void setEpicStatus(Epic epic) {
        List<Integer> subsIds = epic.getSubtasksIds();
        int countNEW = 0;
        int countDONE = 0;
        for (Integer subId : subsIds) {
            TaskStatus status = tasksMap.get(subId).getStatus();
            switch (status) {
                case NEW -> countNEW++;
                case DONE -> countDONE++;
            }
        }
        if (countNEW == subsIds.size()) {
            epic.setStatus(TaskStatus.NEW);
        } else if (countDONE == subsIds.size()) {
            epic.setStatus(TaskStatus.DONE);
        } else {
            epic.setStatus(TaskStatus.IN_PROGRESS);
        }
    }

    private void setEpicStartTime(Epic epic) {
        LocalDateTime startTime = epic.getSubtasksIds().stream()
                .map(tasksMap::get)
                .filter(el -> el.getStatus() != TaskStatus.DONE)
                .map(Task::getStartTime)
                .filter(Objects::nonNull)
                .min(Comparator.naturalOrder()).orElse(null);
        epic.setStartTime(startTime);
    }

    private void setEpicDuration(Epic epic) {
        Duration duration = epic.getSubtasksIds().stream()
                .map(tasksMap::get)
                .filter(el -> el.getStatus() != TaskStatus.DONE)
                .map(Task::getDuration)
                .reduce(Duration::plus).orElse(Duration.ZERO);
        epic.setDuration(duration);
    }

    private LocalDateTime getEpicEndTime(Epic epic) {
        return epic.getSubtasksIds().stream()
                .map(tasksMap::get)
                .filter(el -> el.getStatus() != TaskStatus.DONE)
                .filter(el -> el.getStartTime() != null)
                .map(el -> el.getStartTime().plus(el.getDuration()))
                .max(Comparator.naturalOrder()).orElse(null);
    }

    private void addTaskToPrioritizedTasks(Task task) {
        if (task.getStatus() == TaskStatus.DONE) {
            return;
        }
        if (task.getStartTime() != null) {
            if (!isCrossingTasks(task)) {
                prioritizedTasks.add(task);
            } else {
                task.setStartTime(null);
                task.setDuration(Duration.ZERO);
                throw new RuntimeException("Task id=" + task.getId() +
                        " saved in manager, but its startTime/duration are set to null/zero");
            }
        }
    }

    private void removeAllSubtasks(int epicId) {
        List<Integer> subtasksIds = ((Epic) tasksMap.get(epicId)).getSubtasksIds();
        subtasksIds.forEach(el -> {
            historyManager.remove(el);
            prioritizedTasks.remove(tasksMap.get(el));
            tasksMap.remove(el);
        });
        tasksMap.get(epicId).setDuration(Duration.ZERO);
        tasksMap.get(epicId).setStartTime(null);
    }

    private int nextId() {
        return ++id;
    }

}
