package service;

import com.yandex.kanban.model.Task;
import com.yandex.kanban.service.*;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    public static HistoryManager historyManager;
    public static final String DESCRIPTION = "description";


    @Test
    void add() {
        historyManager = new InMemoryHistoryManager();
        historyManager.add(new Task("TestTask", DESCRIPTION));
        final List<Task> history = historyManager.getHistory();
        assertNotNull(history, "История не пустая.");
        assertEquals(1, history.size(), "История не пустая.");
    }

    @Test void addAndRemoveOneTask(){
        TaskManager taskManager = initTaskManagersWith10Tasks();
        historyManager = taskManager.getHistoryManager();

        Task task1 = taskManager.getTaskById(5);
        assertEquals(1, historyManager.getHistory().size(), "Таск не добавился в историю");
        assertSame(task1, historyManager.getHistory().getFirst(), "В историю добавился не тот такс");

        task1 = taskManager.getTaskById(5);
        assertEquals(1, historyManager.getHistory().size(), "Таск не добавился в историю" +
                " или добавилось два одинаковых");
        assertSame(task1, historyManager.getHistory().getFirst(), "В историю добавился не тот такс");

        taskManager.removeTaskById(task1.getId());
        assertEquals(0, historyManager.getHistory().size(), "Таск не удалился из истории");
    }

    @Test void addAndRemoveThreeTasksDifferentOrder(){
        TaskManager taskManager = initTaskManagersWith10Tasks();
        historyManager = taskManager.getHistoryManager();

        Task task1 = taskManager.getTaskById(5);
        Task task2 = taskManager.getTaskById(6);
        assertEquals(2, historyManager.getHistory().size(),"Таски не добавился в историю");
        assertSame(task1, historyManager.getHistory().get(0), "В историю добавился не тот такс");
        assertSame(task2, historyManager.getHistory().get(1), "В историю добавился не тот такс");

        task1 = taskManager.getTaskById(5);
        assertEquals(2, historyManager.getHistory().size(),"Таски не добавился в историю");
        assertSame(task1, historyManager.getHistory().get(1), "В историю добавился не тот такс");
        assertSame(task2, historyManager.getHistory().get(0), "В историю добавился не тот такс");

        Task task3 = taskManager.getTaskById(7);
        assertEquals(3, historyManager.getHistory().size(),"Таски не добавился в историю");
        assertSame(task3, historyManager.getHistory().get(2), "В историю добавился не тот такс");

        task3 = taskManager.getTaskById(7);
        assertEquals(3, historyManager.getHistory().size(),"Появился лишний таск в истории");
        assertSame(task2, historyManager.getHistory().get(0), "В историю добавился не тот такс");
        assertSame(task1, historyManager.getHistory().get(1), "В историю добавился не тот такс");
        assertSame(task3, historyManager.getHistory().get(2), "В историю добавился не тот такс");

        taskManager.removeTaskById(6);
        assertEquals(2, historyManager.getHistory().size(),"Таск не удалился из истории");
        assertSame(task1, historyManager.getHistory().get(0), "В историю добавился не тот такс");
        assertSame(task3, historyManager.getHistory().get(1), "Удалился не тот такс");

        taskManager.removeTaskById(7);
        assertEquals(1, historyManager.getHistory().size(),"Таск не удалился из истории");
        assertSame(task1, historyManager.getHistory().getFirst(), "Удалился не тот такс");

        taskManager.removeTaskById(5);
        assertEquals(0, historyManager.getHistory().size(),"История не отчистилась");
    }

    @Test
    void AddTenTaskChangingOrder() {
        TaskManager taskManager = initTaskManagersWith10Tasks();
        historyManager = taskManager.getHistoryManager();
        for (int i = 1; i <= 10; i++) {
            taskManager.getTaskById(i);
        }
        assertEquals(10, historyManager.getHistory().size(), "Не все таски добавились в историю");

        List<Task> allKindOfTasks = taskManager.getAllKindOfTasks();
        List<Task> history = historyManager.getHistory();
        for (int i = 0; i < 10; i++) {
            assertSame(allKindOfTasks.get(i), history.get(i), "История добавилась не в том порядке");
        }
        for (int i = 1; i <= 10; i++) {
            taskManager.getTaskById(11 - i);
        }
        history = historyManager.getHistory();
        for (int i = 0; i < 10; i++) {
            assertSame(allKindOfTasks.get(i), history.get(9-i), "История добавилась не в том порядке");
        }

        taskManager.removeTaskById(1);
        history = historyManager.getHistory();
        assertEquals(6, history.size(), "Не удлались подзадачи из истории");

        taskManager.removeTaskById(7);
        history = historyManager.getHistory();
        assertEquals(5, history.size(), "Не удлались задача из истории");

        taskManager.removeAllTasks();
        assertEquals(0, historyManager.getHistory().size(),"История не отчистилась");
    }

    TaskManager initTaskManagersWith10Tasks() {
        TaskManager taskManager = new InMemoryTaskManager();
        taskManager.createEpic("Epic 1", DESCRIPTION);
        taskManager.createSubtask("Subtask 2", DESCRIPTION, 1);
        taskManager.createSubtask("Subtask 3", DESCRIPTION, 1);
        taskManager.createSubtask("Subtask 4", DESCRIPTION, 1);
        taskManager.createTask("Task 5", DESCRIPTION);
        taskManager.createTask("Task 6", DESCRIPTION);
        taskManager.createTask("Task 7", DESCRIPTION);
        taskManager.createEpic("Epic 8", DESCRIPTION);
        taskManager.createSubtask("Subtask 9", DESCRIPTION, 8);
        taskManager.createSubtask("Subtask 10", DESCRIPTION, 8);
        return taskManager;
    }

}