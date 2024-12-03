package com.yandex.kanban.service;

import com.yandex.kanban.model.Epic;
import com.yandex.kanban.model.Subtask;
import com.yandex.kanban.model.Task;
import com.yandex.kanban.model.TaskStatus;
import com.yandex.kanban.util.Converter;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.stream.Stream;

public class FileBackedTaskManager extends InMemoryTaskManager {
    private final Path path;

    public FileBackedTaskManager(String fileName) {
        this.path = Paths.get(fileName);
        createSaveFile(this.path);
        load();
    }

    public FileBackedTaskManager(Path path) {
        this.path = path;
        createSaveFile(this.path);
        load();
    }

    @Override
    public Task createTask(String title, String description) {
        Task task = super.createTask(title, description);
        save();
        return task;
    }

    @Override
    public Epic createEpic(String title, String description) {
        Epic epic = super.createEpic(title, description);
        save();
        return epic;
    }

    @Override
    public Subtask createSubtask(String title, String description, int epicId) {
        Subtask subtask = super.createSubtask(title, description, epicId);
        save();
        return subtask;
    }

    @Override
    public int addTask(Task task) {
        int id = super.addTask(task);
        save();
        return id;
    }

    @Override
    public int addEpic(Epic epic) {
        int id = super.addEpic(epic);
        save();
        return id;
    }

    @Override
    public int addSubtask(Subtask subtask) {
        int id = super.addSubtask(subtask);
        save();
        return id;
    }

    @Override
    public void removeAllTasks() {
        super.removeAllTasks();
        save();
    }

    @Override
    public void removeTaskById(int id) {
        super.removeTaskById(id);
        save();
    }

    @Override
    public void updateTitle(String title, int id) {
        super.updateTitle(title, id);
        save();
    }

    @Override
    public void updateDescription(String description, int id) {
        super.updateDescription(description, id);
        save();
    }

    @Override
    public void updateStatus(TaskStatus status, int id) {
        super.updateStatus(status, id);
        save();
    }

    private void save() {
        try {
            Files.write(path, tasksMap.values().stream().map(Converter::taskToString).toList());
        } catch (IOException e) {
            throw new RuntimeException("Ошибка чтения файла сохранения");
        }
    }

    private void load() {
        try (Stream<String> lines = Files.lines(path)) {
            lines.map(Converter::stringToTask).filter(Objects::nonNull).forEach(el -> tasksMap.put(el.getId(), el));
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при чтении файла: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Некорректные данные в файле: " + e.getMessage());
        }
        this.id = tasksMap.keySet().stream().mapToInt(el -> el).max().orElse(0);
    }

    private void createSaveFile(Path path) {
        if (!Files.exists(path)) {
            try {
                Files.createFile(path);
            } catch (IOException e) {
                throw new RuntimeException("Не удалось создаь файл по заданому пути");
            }
        }
    }
}
