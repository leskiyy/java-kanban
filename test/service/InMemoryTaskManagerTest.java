package service;

import com.yandex.kanban.model.Epic;
import com.yandex.kanban.model.Subtask;
import com.yandex.kanban.model.Task;
import com.yandex.kanban.model.TaskStatus;
import com.yandex.kanban.service.InMemoryTaskManager;
import com.yandex.kanban.service.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {

    public static TaskManager taskManager;


    @BeforeEach
    void initInMemoryTaskManager() {
        taskManager = new InMemoryTaskManager();
    }

    @Test
    void addTask() {
        Task task = new Task("Test addNewTask", "Test addNewTask description");
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
        Epic epic = new Epic("epic", "description");
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
        Epic epic = taskManager.createEpic("TestSubs", "description");
        Subtask subtask = new Subtask("Sub", "description");
        final int subId = taskManager.addSubtask(subtask, epic.getId());
        assertEquals(2, subId, "Неправильно задан id");

        Task savedTask = taskManager.getTaskById(subId);
        assertInstanceOf(Subtask.class, savedTask, "Возврашена не подзадача");
        final Subtask savedSub = (Subtask) savedTask;

        assertSame(subtask, savedSub, "Задачи не совпадают.");
        assertEquals(2, epic.getSubtasksIds().getFirst(), "Подзадача не добавилась в епик");
    }

    @Test
    void addSubtaskWithoutEpicId() {
        Epic epic = taskManager.createEpic("TestSubs", "description");
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
        Epic epic = taskManager.createEpic("TestStatus", "description");

        assertTrue(epic.getSubtasksIds().isEmpty(), "Вновь созданный эпик имеет подзадачи");
        assertSame(TaskStatus.NEW, epic.getStatus(), "Вновь созданный эпик имеет статус не новой");

        Subtask subtask1 = taskManager.createSubtask("sub1", "description", 1);
        Subtask subtask2 = taskManager.createSubtask("sub2", "description", 1);

        assertSame(TaskStatus.NEW, epic.getStatus()
                , "Эпик при добавлении подзадачи со статусом новая поменял статус");

        Subtask subtask3 = new Subtask("sub3", "description", 1);
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

        Subtask subtask4 = taskManager.createSubtask("sub4", "description", 1);

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

}