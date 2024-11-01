package service;

import com.yandex.kanban.model.Epic;
import com.yandex.kanban.model.Subtask;
import com.yandex.kanban.model.Task;
import com.yandex.kanban.model.TaskStatus;
import com.yandex.kanban.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    public static HistoryManager historyManager;

    @BeforeEach
    void initHistoryManager() {
        historyManager = new InMemoryHistoryManager();
    }

    @Test
    void add() {
        historyManager.add(new Task("TestTask", "description"));
        final List<Task> history = historyManager.getHistory();
        assertNotNull(history, "История не пустая.");
        assertEquals(1, history.size(), "История не пустая.");
    }

    @Test
    void testHistory() {
        List<String> history = new ArrayList<>();
        HistoryManager historyManager = new InMemoryHistoryManager();

        Task task = new Task("task1", "v1");
        Subtask subtask = new Subtask("sub1", "v1", 1);
        Epic epic = new Epic("epic1", "v1");
        epic.getSubtasksIds().addAll(List.of(1, 2, 3));

        historyManager.add(task);
        history.add(task.toString());
        historyManager.add(epic);
        history.add(epic.toString());
        historyManager.add(subtask);
        history.add(subtask.toString());

        task.setStatus(TaskStatus.IN_PROGRESS);
        subtask.setDescription("change1");
        epic.getSubtasksIds().removeFirst();

        historyManager.add(task);
        history.add(task.toString());
        historyManager.add(epic);
        history.add(epic.toString());
        historyManager.add(subtask);
        history.add(subtask.toString());

        for (int i = 0; i < history.size(); i++) {
            assertEquals(history.get(i), historyManager.getHistory().get(i).toString(),
                    "Менеджер неправильно записывает состояния тасков");
        }
    }
}