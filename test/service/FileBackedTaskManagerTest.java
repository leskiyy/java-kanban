package service;

import com.yandex.kanban.model.Epic;
import com.yandex.kanban.model.Subtask;
import com.yandex.kanban.model.Task;
import com.yandex.kanban.service.FileBackedTaskManager;
import com.yandex.kanban.util.Converter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest {
    private static final Path temp;
    private static final String DESCRIPTION = "description";
    private static FileBackedTaskManager testManager;

    static {
        try {
            temp = Files.createTempFile("temp", ".txt");
        } catch (IOException e) {
            throw new RuntimeException("Ошибка создания temp файла");
        }
    }

    @BeforeEach
    void initTestManager() {
        clearTempFile();
        testManager = new FileBackedTaskManager(temp);
        testManager.createTask("Task", DESCRIPTION);
        testManager.createEpic("Epic", DESCRIPTION);
        testManager.createSubtask("Subtask", DESCRIPTION, 2);
    }

    @Test
    void loadManager() {
        FileBackedTaskManager manager = new FileBackedTaskManager(temp);

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
        FileBackedTaskManager manager = new FileBackedTaskManager(temp);

        List<String> list = readSaveFile();
        assertEquals(0, list.size(), "менеджер без задач имеет строку в файле сохранения");

        List<Task> tasks = initTasks();
        manager.addTask(tasks.getFirst());
        list = readSaveFile();
        assertEquals(1, list.size(), "Количество строк в файле сохранения не соответствует числу" +
                " задач в менеджере");
        assertEquals(list.getFirst(), Converter.taskToString(tasks.getFirst()), "Не соответсвие добавленной " +
                "задачи и задачи в файле сохраниеия");
        manager.addEpic((Epic) tasks.get(1));

        list = readSaveFile();
        assertEquals(2, list.size(), "Количество строк в файле сохранения не соответствует числу" +
                " задач в менеджере");
        assertEquals(list.get(1), Converter.taskToString(tasks.get(1)), "Не соответсвие добавленной " +
                "задачи и задачи в файле сохраниеия");

        Subtask subtask = (Subtask) tasks.get(2);
        subtask.setEpicId(2);
        manager.addSubtask(subtask);

        list = readSaveFile();
        assertEquals(3, list.size(), "Количество строк в файле сохранения не соответствует числу" +
                " задач в менеджере");
        assertEquals(list.get(2), Converter.taskToString(tasks.get(2)), "Не соответсвие добавленной " +
                "задачи и задачи в файле сохраниеия");

    }

    @Test
    void inCaseOfIncorrectSaveFile() {
        writeIncorrectSave();
        RuntimeException exception = assertThrows(RuntimeException.class, () -> new FileBackedTaskManager(temp));
        assertTrue(exception.getMessage().startsWith("Некорректные данные в файле: "));
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
            Files.writeString(temp, "damaged save file");
        } catch (IOException e) {
            throw new RuntimeException("Не прошла отчистка temp файла");
        }
    }


}
