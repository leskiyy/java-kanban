package com.yandex.kanban.service;

import com.yandex.kanban.model.Epic;
import com.yandex.kanban.model.Subtask;
import com.yandex.kanban.model.Task;
import com.yandex.kanban.model.TaskStatus;
import com.yandex.kanban.util.Converter;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends TaskManagerTest<FileBackedTaskManager> {
    private static final Path temp;
    private static FileBackedTaskManager testManager;

    static {
        try {
            temp = Files.createTempFile("temp", ".txt");
        } catch (IOException e) {
            throw new RuntimeException("Ошибка создания temp файла");
        }
    }

    @Override
    TaskManager getTaskManager() {
        clearTempFile();
        return new FileBackedTaskManager(temp);
    }

    @Test
    void loadManager() {
        initTestManagerWithThreeTasks();
        TaskManager manager = new FileBackedTaskManager(temp);

        List<Task> initManagerTasks = manager.getAllKindOfTasks();
        List<Task> testManagerTasks = testManager.getAllKindOfTasks();

        for (int i = 0; i < testManagerTasks.size(); i++) {
            assertEquals(testManagerTasks.get(i), initManagerTasks.get(i));
        }

        manager.createTask("additionalTask", DESCRIPTION);
        initManagerTasks = manager.getAllKindOfTasks();
        assertEquals(4, initManagerTasks.size(), "Не добавлен таск после инициализации");

        Subtask subtask = (Subtask) initManagerTasks.get(2);
        assertEquals(2, subtask.getEpicId(), "Не восстановлена связь сабтакски с эпиком");

        Epic epic = (Epic) initManagerTasks.get(1);
        assertEquals(1, epic.getSubtasksIds().size(), "Некорректо востановленны id сабтасков");
        assertEquals(subtask.getId(), epic.getSubtasksIds().getFirst(), "Некорректо востановленны id сабтасков");

    }

    @Test
    void saveToFile() {
        //инициализация и дальшейшая работа менеджера с пустого файла
        clearTempFile();
        TaskManager manager = getTaskManager();

        List<String> list = readSaveFile();
        assertEquals(0, list.size(), "менеджер без задач имеет строку в файле сохранения");

        List<Task> tasks = initTasks();
        manager.addTask(tasks.getFirst());
        list = readSaveFile();
        assertEquals(1, list.size(), "Количество строк в файле сохранения не соответствует числу" +
                " задач в менеджере");
        assertEquals(list.getFirst(), Converter.taskToString(tasks.getFirst(), -1, false), "Не соответсвие добавленной " +
                "задачи и задачи в файле сохраниеия");
        manager.addEpic((Epic) tasks.get(1));

        list = readSaveFile();
        assertEquals(2, list.size(), "Количество строк в файле сохранения не соответствует числу" +
                " задач в менеджере");
        assertEquals(list.get(1), Converter.taskToString(tasks.get(1), -1, false), "Не соответсвие добавленной " +
                "задачи и задачи в файле сохраниеия");

        Subtask subtask = (Subtask) tasks.get(2);
        subtask.setEpicId(2);
        manager.addSubtask(subtask);

        list = readSaveFile();
        assertEquals(3, list.size(), "Количество строк в файле сохранения не соответствует числу" +
                " задач в менеджере");
        assertEquals(list.get(2), Converter.taskToString(tasks.get(2), -1, false), "Не соответсвие добавленной " +
                "задачи и задачи в файле сохраниеия");

    }

    @Test
    void inCaseOfIncorrectSaveFile() {
        writeIncorrectSave();
        RuntimeException exception = assertThrows(RuntimeException.class, () -> new FileBackedTaskManager(temp));
        assertTrue(exception.getMessage().startsWith("Некорректные данные в файле: "));
    }

    @Test
    void complexLoadSaveTest() {
        TaskManager saveManager = initManagerWithHistoryAndPrioritizedTasks();
        TaskManager loadManager = new FileBackedTaskManager(temp);

        List<Task> saveTasks = saveManager.getAllKindOfTasks();
        List<Task> loadTasks = loadManager.getAllKindOfTasks();
        for (int i = 0; i < saveTasks.size(); i++) {
            assertEquals(saveTasks.get(i).toString(), loadTasks.get(i).toString(),
                    "Сохраненная и загруженная таски не совпадают");
        }

        Set<Task> savePrioritizedTasks = saveManager.getPrioritizedTasks();
        Set<Task> loadPrioritizedTasks = loadManager.getPrioritizedTasks();
        List<Task> saveFromSet = new ArrayList<>(savePrioritizedTasks);
        List<Task> loadFromSet = new ArrayList<>(loadPrioritizedTasks);
        for (int i = 0; i < saveFromSet.size(); i++) {
            assertEquals(saveFromSet.get(i).toString(), loadFromSet.get(i).toString(),
                    "Сохраненная и загруженная таски не совпадают");
        }

        List<Task> saveHistory = saveManager.getHistoryManager().getHistory();
        List<Task> loadHistory = loadManager.getHistoryManager().getHistory();
        for (int i = 0; i < saveHistory.size(); i++) {
            assertEquals(saveHistory.get(i).toString(), loadHistory.get(i).toString(),
                    "Сохраненная и загруженная таски не совпадают");
        }
    }

    void initTestManagerWithThreeTasks() {
        clearTempFile();
        testManager = new FileBackedTaskManager(temp);
        testManager.createTask("Task", DESCRIPTION);
        testManager.createEpic("Epic", DESCRIPTION);
        testManager.createSubtask("Subtask", DESCRIPTION, 2);
    }

    List<Task> initTasks() {
        Task task = new Task("task1", DESCRIPTION);
        Epic epic = new Epic("epic1", DESCRIPTION);
        Subtask subtask1 = new Subtask("subtask1", DESCRIPTION);
        Subtask subtask2 = new Subtask("subtask2", DESCRIPTION);
        List<Task> tasks = new ArrayList<>();
        Collections.addAll(tasks, task, epic, subtask1, subtask2);
        return tasks;
    }

    List<String> readSaveFile() {
        List<String> list;
        try {
            list = Files.readAllLines(temp);
        } catch (IOException e) {
            throw new RuntimeException("Ошибка чтения файла сохранения");
        }
        return list;
    }

    void clearTempFile() {
        try {
            Files.writeString(temp, "");
        } catch (IOException e) {
            throw new RuntimeException("Не прошла отчистка temp файла");
        }
    }

    void writeIncorrectSave() {
        try {
            Files.writeString(temp, "N_PROGRESS;18-11-1998 03:08;100;3");
        } catch (IOException e) {
            throw new RuntimeException("Не записалась некорректная строчка в сейф");
        }
    }

    TaskManager initManagerWithHistoryAndPrioritizedTasks() {
        clearTempFile();
        TaskManager manager = new FileBackedTaskManager(temp);
        Task taskWithDateTime = new Task("Task 1", DESCRIPTION);
        taskWithDateTime.setStartTime(LocalDateTime.of(2022, 12, 11, 9, 0));
        taskWithDateTime.setDuration(Duration.ofMinutes(30));
        manager.addTask(taskWithDateTime);
        manager.getTaskById(1);

        Task taskWithDateTime2 = new Task("Task 2", DESCRIPTION);
        taskWithDateTime2.setStartTime(LocalDateTime.of(2022, 12, 11, 9, 30));
        taskWithDateTime2.setDuration(Duration.ofHours(1));
        manager.addTask(taskWithDateTime2);

        Task taskWithDateTime3 = new Task("Task 3", DESCRIPTION);
        taskWithDateTime3.setStartTime(LocalDateTime.of(2022, 12, 11, 9, 0));
        taskWithDateTime3.setDuration(Duration.ZERO);
        manager.addTask(taskWithDateTime3);
        manager.getTaskById(3);

        Epic epic = new Epic("Epic", DESCRIPTION);
        manager.addEpic(epic);
        manager.getTaskById(4);
        Subtask subtask1 = new Subtask("Sub1", DESCRIPTION, 4);
        subtask1.setStartTime(LocalDateTime.of(2022, 12, 11, 10, 30));
        subtask1.setDuration(Duration.ofMinutes(60));
        manager.addSubtask(subtask1);
        Subtask subtask2 = new Subtask("Sub2", DESCRIPTION, 4);
        subtask2.setStartTime(LocalDateTime.of(2022, 12, 11, 12, 30));
        subtask2.setDuration(Duration.ofMinutes(60));
        manager.addSubtask(subtask2);
        manager.updateStatus(TaskStatus.DONE, 5);
        manager.updateStatus(TaskStatus.IN_PROGRESS, 6);
        manager.getTaskById(6);
        return manager;
    }


}
