package util;

import com.yandex.kanban.model.Epic;
import com.yandex.kanban.model.Subtask;
import com.yandex.kanban.model.Task;
import com.yandex.kanban.model.TaskStatus;
import com.yandex.kanban.util.Converter;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ConverterTest {
    public static final String DESCRIPTION = "description";

    @Test
    public void taskToStringTest() {
        Task task = new Task("Task", DESCRIPTION);
        task.setStatus(TaskStatus.DONE);
        task.setId(2);
        String toString = Converter.taskToString(task);
        Task restored = Converter.stringToTask(toString);

        assertEquals(task.toString(), restored.toString());
    }

    @Test
    public void subToStringTest() {
        Subtask subtask = new Subtask("Subtask", DESCRIPTION);
        subtask.setStatus(TaskStatus.IN_PROGRESS);
        subtask.setId(2);
        subtask.setEpicId(3);
        String toString = Converter.taskToString(subtask);
        Task restored = Converter.stringToTask(toString);

        assertEquals(subtask.toString(), restored.toString());
    }

    @Test
    public void epicToStringTest() {
        Epic epic = new Epic("Subtask", DESCRIPTION);
        epic.setStatus(TaskStatus.DONE);
        epic.setId(4);
        epic.setSubtasksIds(List.of(1, 2, 3));
        String toString = Converter.taskToString(epic);
        Task restored = Converter.stringToTask(toString);

        assertEquals(epic.toString(), restored.toString());
    }
}
