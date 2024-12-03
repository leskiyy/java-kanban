package com.yandex.kanban.service;

import com.yandex.kanban.model.Epic;
import com.yandex.kanban.model.Subtask;
import com.yandex.kanban.model.Task;
import com.yandex.kanban.model.TaskStatus;

import java.util.List;

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

    void removeAllTasks();

    void removeTaskById(int id);

    void updateTitle(String title, int id);

    void updateDescription(String description, int id);

    void updateStatus(TaskStatus status, int id);

    HistoryManager getHistoryManager();
}
