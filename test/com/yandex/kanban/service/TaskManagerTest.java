package com.yandex.kanban.service;

import com.yandex.kanban.model.Epic;
import com.yandex.kanban.model.Subtask;
import com.yandex.kanban.model.Task;
import com.yandex.kanban.model.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {
    public static final String DESCRIPTION = "description";
    public static TaskManager taskManager;

    abstract TaskManager getTaskManager();

    @BeforeEach
    void initInMemoryTaskManager() {
        taskManager = getTaskManager();
    }

    @Test
    void addTask() {
        Task task = new Task("Test addNewTask", DESCRIPTION);
        final int taskId = taskManager.addTask(task);

        final Task savedTask = taskManager.getTaskById(taskId);

        assertNotNull(savedTask, "Задача не найдена.");
        assertSame(task, savedTask, "Задачи не совпадают.");

        final List<Task> tasks = taskManager.getAllTasks();

        assertNotNull(tasks, "Задачи не возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertSame(task, tasks.getFirst(), "Задачи не совпадают.");
    }

    @Test
    void addEpic() {
        Epic epic = new Epic("epic", DESCRIPTION);
        epic.setSubtasksIds(List.of(1, 2, 3));
        epic.setStatus(TaskStatus.IN_PROGRESS);

        final int taskId = taskManager.addEpic(epic);
        Task savedTask = taskManager.getTaskById(taskId);
        assertInstanceOf(Epic.class, savedTask, "Возврашен не эпик");
        final Epic savedEpic = (Epic) savedTask;
        assertSame(epic, savedEpic, "Задачи не совпадают.");
        assertTrue(epic.getSubtasksIds().isEmpty(), "Перед добавлением в менеджер не очищен список подзадач");
        assertSame(TaskStatus.NEW, epic.getStatus(), "Только добавленный эпик имеет статус не новой");

        final List<Epic> epics = taskManager.getAllEpics();

        assertNotNull(epics, "Задачи не возвращаются.");
        assertEquals(1, epics.size(), "Неверное количество задач.");
        assertSame(epic, epics.getFirst(), "Задачи не совпадают.");
    }

    @Test
    void addSubtaskWithEpicId() {
        Epic epic = taskManager.createEpic("TestSubs", DESCRIPTION);
        Subtask subtask = new Subtask("Sub", DESCRIPTION);
        subtask.setEpicId(epic.getId());
        final int subId = taskManager.addSubtask(subtask);
        assertEquals(2, subId, "Неправильно задан id");

        Task savedTask = taskManager.getTaskById(subId);
        assertInstanceOf(Subtask.class, savedTask, "Возврашена не подзадача");
        final Subtask savedSub = (Subtask) savedTask;

        assertSame(subtask, savedSub, "Задачи не совпадают.");
        assertEquals(2, epic.getSubtasksIds().getFirst(), "Подзадача не добавилась в епик");
    }

    @Test
    void addSubtaskWithoutEpicId() {
        Epic epic = taskManager.createEpic("TestSubs", DESCRIPTION);
        Subtask subtask = new Subtask("Sub", "description", 1);
        final int subId = taskManager.addSubtask(subtask);
        assertEquals(2, subId, "Неправильно задан id");

        Task savedTask = taskManager.getTaskById(subId);
        assertInstanceOf(Subtask.class, savedTask, "Возврашена не подзадача");
        final Subtask savedSub = (Subtask) savedTask;

        assertSame(subtask, savedSub, "Задачи не совпадают.");
        assertEquals(2, epic.getSubtasksIds().getFirst(), "Подзадача не добавилась в епик");
    }

    @Test
    void updatingEpicStatus() {
        Epic epic = taskManager.createEpic("TestStatus", DESCRIPTION);

        assertTrue(epic.getSubtasksIds().isEmpty(), "Вновь созданный эпик имеет подзадачи");
        assertSame(TaskStatus.NEW, epic.getStatus(), "Вновь созданный эпик имеет статус не новой");

        Subtask subtask1 = taskManager.createSubtask("sub1", DESCRIPTION, 1);
        Subtask subtask2 = taskManager.createSubtask("sub2", DESCRIPTION, 1);

        assertSame(TaskStatus.NEW, epic.getStatus()
                , "Эпик при добавлении подзадачи со статусом новая поменял статус");

        Subtask subtask3 = new Subtask("sub3", DESCRIPTION, 1);
        subtask3.setStatus(TaskStatus.DONE);
        taskManager.addSubtask(subtask3);

        assertSame(TaskStatus.IN_PROGRESS, epic.getStatus()
                , "Эпик при добавлении подзадачи со статусом сделанная не поменял статус");
        assertEquals(3, epic.getSubtasksIds().size(), "Неверно добавились подзадачи в список эпика");

        taskManager.updateStatus(TaskStatus.IN_PROGRESS, subtask1.getId());
        taskManager.updateStatus(TaskStatus.DONE, subtask2.getId());
        assertSame(TaskStatus.IN_PROGRESS, epic.getStatus(), "Эпик неверно поменял");

        taskManager.updateStatus(TaskStatus.DONE, subtask1.getId());
        assertSame(TaskStatus.DONE, epic.getStatus(), "Эпик не поменял стутус при выполнении всех подзадач");

        Subtask subtask4 = taskManager.createSubtask("sub4", DESCRIPTION, 1);

        assertSame(TaskStatus.IN_PROGRESS, epic.getStatus()
                , "Эпик при добавлении подзадачи со статусом сделанная не поменял статус");
        assertEquals(4, epic.getSubtasksIds().size(), "Неверно добавились подзадачи в список эпика");

        List<Subtask> subtasks = taskManager.getAllSubtasksByEpicId(epic.getId());
        assertSame(subtask1, subtasks.get(0), "Неверная работа метода getAllSubtasksByEpicId()");
        assertSame(subtask2, subtasks.get(1), "Неверная работа метода getAllSubtasksByEpicId()");
        assertSame(subtask3, subtasks.get(2), "Неверная работа метода getAllSubtasksByEpicId()");
        assertSame(subtask4, subtasks.get(3), "Неверная работа метода getAllSubtasksByEpicId()");

        taskManager.removeTaskById(subtask4.getId());

        assertSame(TaskStatus.DONE, epic.getStatus(), "Эпик не поменял стутус при удалении подзадачи");
        assertEquals(3, epic.getSubtasksIds().size(), "Не удалилась подзадача из списока эпика");

        taskManager.removeTaskById(subtask1.getId());
        taskManager.removeTaskById(subtask2.getId());
        taskManager.removeTaskById(subtask3.getId());

        assertTrue(epic.getSubtasksIds().isEmpty(), "Не удалилить id подзадач");
        assertSame(TaskStatus.NEW, epic.getStatus(), "Не обновился стутус эпика на NEW");
    }

    @Test
    void addPrioritizedTasks() {
        Task task = new Task("Task2", DESCRIPTION);
        taskManager.addTask(task);
        Set<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
        assertEquals(0, prioritizedTasks.size(), "задача ошибочно добавилась в prioritizedTasks");
        taskManager.updateStartTime(LocalDateTime.of(2000, 1, 1, 0, 0), 1);
        taskManager.updateDuration(Duration.ofMinutes(30), 1);

        assertEquals(1, prioritizedTasks.size(), "Задача не добавилась в prioritizedTasks");
        assertTrue(prioritizedTasks.contains(task), "Задача в prioritizedTasks не равна Task");

        Task task2 = new Task("Task2", DESCRIPTION);
        task2.setStartTime(LocalDateTime.of(2000, 1, 1, 0, 29));
        task2.setDuration(Duration.ofMinutes(30));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> taskManager.addTask(task2));
        assertEquals("Task id=2 saved in manager, but its startTime/duration are set to null/zero"
                , exception.getMessage(), "Сообщение исключания не совпадает с ожидаемым");
        assertEquals(1, prioritizedTasks.size(), "Задача ошибочно добавилась в приоритетные");
        assertSame(task2, taskManager.getTaskById(2), "Задача не добавилась в taskManager");

        taskManager.updateDuration(Duration.ofMinutes(30), 2);
        taskManager.updateStartTime(LocalDateTime.of(2000, 1, 1, 0, 30), 2);
        assertEquals(2, prioritizedTasks.size(), "Задача не добавилась в приоритетные");
        assertTrue(prioritizedTasks.contains(task2), "prioritizedTasks не содержит task2");

        Epic epic = new Epic("Epic", DESCRIPTION);
        int epicID = taskManager.addEpic(epic);

        RuntimeException exception1 = assertThrows(RuntimeException.class,
                () -> taskManager.updateStartTime(LocalDateTime.now(), epicID), "Обновилось startTime у эпика");
        assertEquals("can't set epic's start time", exception1.getMessage(),
                "Неверное сообщение об ошибке");
        RuntimeException exception2 = assertThrows(RuntimeException.class,
                () -> taskManager.updateDuration(Duration.ZERO, epicID), "Обновилось duration у эпика");
        assertEquals("can't set epic's duration", exception2.getMessage(),
                "Неверное сообщение об ошибке");

        Subtask subtask1 = new Subtask("Sub1", DESCRIPTION);
        Subtask subtask2 = new Subtask("Sub2", DESCRIPTION);

        subtask1.setEpicId(epicID);
        subtask1.setStartTime(LocalDateTime.of(1999, 12, 31, 23, 0));
        subtask1.setDuration(Duration.ofMinutes(60));
        int sub1Id = taskManager.addSubtask(subtask1);

        subtask2.setEpicId(epicID);
        subtask2.setStartTime(LocalDateTime.of(2000, 1, 1, 1, 0));
        subtask2.setDuration(Duration.ofMinutes(60));
        int sub2Id = taskManager.addSubtask(subtask2);
        RuntimeException exception3 = assertThrows(RuntimeException.class, () ->
                        taskManager.updateDuration(Duration.ofMinutes(120), sub1Id),
                "Удалось обновить задачу, которая перескат другие");
        assertEquals("task id=" + sub1Id + " is crossing existing ones because of new duration",
                exception3.getMessage(), "Неверное сообщение об ошибке");
        assertEquals(Duration.ofMinutes(60), subtask1.getDuration(), "Длительность не откатилась к предыдущей");

        RuntimeException exception4 = assertThrows(RuntimeException.class,
                () -> taskManager.updateStartTime(LocalDateTime.of(2000, 1, 1, 0, 0), sub2Id),
                "Удалось обновить задачу, которая перескат другие");
        assertEquals("task id=" + sub2Id + " is crossing existing ones because of new start time",
                exception4.getMessage(), "Неверное сообщение об ошибке");
        assertEquals(LocalDateTime.of(2000, 1, 1, 1, 0),
                subtask2.getStartTime(), "startTime не откатилась к предыдущей");
        assertEquals(LocalDateTime.of(2000, 1, 1, 0, 0),
                taskManager.getEndTime(sub1Id), "Менеджер вернул не то время");
        assertEquals(LocalDateTime.of(2000, 1, 1, 2, 0),
                taskManager.getEndTime(epicID), "Менеджер вернул не то время");
        assertEquals(LocalDateTime.of(1999, 12, 31, 23, 0), epic.getStartTime(),
                "Эпику установилось неверное время его начала");
        assertEquals(Duration.ofMinutes(120), epic.getDuration(), "Эпику установилось неверная длительность");

    }


}