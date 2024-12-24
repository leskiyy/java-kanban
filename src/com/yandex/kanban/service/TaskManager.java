package com.yandex.kanban.service;

import com.yandex.kanban.model.Epic;
import com.yandex.kanban.model.Subtask;
import com.yandex.kanban.model.Task;
import com.yandex.kanban.model.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface TaskManager {
    Task createTask(String title, String description);

    Epic createEpic(String title, String description);

    Subtask createSubtask(String title, String description, int epicId);

    int addTask(Task task);

    int addEpic(Epic epic);

    int addSubtask(Subtask subtask);

    Task getTaskById(int id);

    List<Task> getAllKindOfTasks();

    List<Task> getAllTasks();

    List<Epic> getAllEpics();

    List<Subtask> getAllSubtasks();

    List<Subtask> getAllSubtasksByEpicId(int epicId);

    Set<Task> getPrioritizedTasks();

    void removeAllTasks();

    void removeTaskById(int id);

    void updateTitle(String title, int id);

    void updateDescription(String description, int id);

    void updateStatus(TaskStatus status, int id);

    void updateStartTime(LocalDateTime startTime, int id);

    void updateDuration(Duration newDuration, int id);

    HistoryManager getHistoryManager();

    LocalDateTime getEndTime(int id);
}
