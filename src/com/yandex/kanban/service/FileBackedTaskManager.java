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
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
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
    public void updateDuration(Duration newDuration, int id) {
        super.updateDuration(newDuration, id);
        save();
    }

    @Override
    public void updateStartTime(LocalDateTime startTime, int id) {
        super.updateStartTime(startTime, id);
        save();
    }

    @Override
    public Task getTaskById(int id) {
        Task taskById = super.getTaskById(id);
        save();
        return taskById;
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
            Files.write(path, managerToLines());
        } catch (IOException e) {
            throw new RuntimeException("Ошибка записи файла сохранения", e);
        }
    }

    private List<String> managerToLines() {
        List<Task> history = historyManager.getHistory();
        return tasksMap.values().stream().map(el -> {
            int historyIndex = history.indexOf(el);
            if (el.getStartTime() != null && prioritizedTasks.contains(el)) {
                return Converter.taskToString(el, historyIndex, true);
            } else {
                return Converter.taskToString(el, historyIndex, false);
            }
        }).toList();
    }

    private void load() {
        List<Integer> prioritizedTasksIds = new ArrayList<>();
        Map<Integer, Integer> historyManagerIdsIndexes = new TreeMap<>();
        try (Stream<String> lines = Files.lines(path)) {
            List<String> listOfLines = lines.toList();
            fillPrioritizedTasksIds(prioritizedTasksIds, listOfLines);
            fillHistoryManagerIdsIndexes(historyManagerIdsIndexes, listOfLines);
            fillTaskMap(listOfLines);
            prioritizedTasksIds.forEach(el -> prioritizedTasks.add(tasksMap.get(el)));
            historyManagerIdsIndexes.forEach((index, id) -> historyManager.add(tasksMap.get(id)));
        } catch (IOException e) {
            throw new RuntimeException("Ошибка при чтении файла: " + e.getMessage(), e);
        } catch (Exception e) {
            throw new RuntimeException("Некорректные данные в файле: " + e.getMessage(), e);
        }
        this.id = tasksMap.keySet().stream().mapToInt(el -> el).max().orElse(0);
    }

    private void fillPrioritizedTasksIds(List<Integer> prioritizedTasksIds, List<String> lines) {
        lines.forEach(el -> {
            String[] saveLineParts = el.split(";");
            int id = Integer.parseInt(saveLineParts[0]);
            if (saveLineParts[saveLineParts.length - 1].equals("true")) {
                prioritizedTasksIds.add(id);
            }
        });
    }

    private void fillHistoryManagerIdsIndexes(Map<Integer, Integer> historyManagerIdsIndexes, List<String> lines) {
        lines.forEach(el -> {
            String[] saveLineParts = el.split(";");
            int id = Integer.parseInt(saveLineParts[0]);
            if (!saveLineParts[saveLineParts.length - 2].equals("-1")) {
                int indexInHistory = Integer.parseInt(saveLineParts[saveLineParts.length - 2]);
                historyManagerIdsIndexes.put(indexInHistory, id);
            }
        });
    }

    private void fillTaskMap(List<String> lines) {
        lines.stream()
                .map(Converter::stringToTask)
                .filter(Objects::nonNull)
                .forEach(el -> tasksMap.put(el.getId(), el));
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
