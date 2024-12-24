package com.yandex.kanban.util;

import com.yandex.kanban.model.Epic;
import com.yandex.kanban.model.Subtask;
import com.yandex.kanban.model.Task;
import com.yandex.kanban.model.TaskStatus;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ConverterTest {
    public static final String DESCRIPTION = "description";

    @Test
    public void taskToStringTest() {
        Task task = new Task("Task", DESCRIPTION);
        task.setStatus(TaskStatus.DONE);
        task.setId(2);
        String toString = Converter.taskToString(task, -1, false);
        Task restored = Converter.stringToTask(toString);

        assertEquals(task.toString(), restored.toString());
    }

    @Test
    public void subToStringTest() {
        Subtask subtask = new Subtask("Subtask", DESCRIPTION);
        subtask.setStatus(TaskStatus.IN_PROGRESS);
        subtask.setId(2);
        subtask.setEpicId(3);
        String toString = Converter.taskToString(subtask, -1, false);
        Task restored = Converter.stringToTask(toString);

        assertEquals(subtask.toString(), restored.toString());
    }

    @Test
    public void epicToStringTest() {
        Epic epic = new Epic("Subtask", DESCRIPTION);
        epic.setStatus(TaskStatus.DONE);
        epic.setId(4);
        epic.setSubtasksIds(List.of(1, 2, 3));
        String toString = Converter.taskToString(epic, -1, false);
        Task restored = Converter.stringToTask(toString);

        assertEquals(epic.toString(), restored.toString());
    }

    @Test
    public void taskToStringTestWithDateTime() {
        Task task = new Task("Task", DESCRIPTION);
        task.setStatus(TaskStatus.DONE);
        task.setId(2);
        task.setDuration(Duration.ofMinutes(9000));
        task.setStartTime(LocalDateTime.now());
        String toString = Converter.taskToString(task, -1, false);
        Task restored = Converter.stringToTask(toString);

        assertEquals(task.toString(), restored.toString());
    }

    @Test
    public void subToStringTestWithDateTime() {
        Subtask subtask = new Subtask("Subtask", DESCRIPTION);
        subtask.setStatus(TaskStatus.IN_PROGRESS);
        subtask.setId(2);
        subtask.setEpicId(3);
        subtask.setId(2);
        subtask.setDuration(Duration.ofMinutes(100));
        subtask.setStartTime(LocalDateTime.of(1998, 11, 18, 3, 8));
        String toString = Converter.taskToString(subtask, -1, false);
        Task restored = Converter.stringToTask(toString);

        assertEquals(subtask.toString(), restored.toString());
    }

    @Test
    public void epicToStringTestWithDateTime() {
        Epic epic = new Epic("Epic", DESCRIPTION);
        epic.setStatus(TaskStatus.DONE);
        epic.setId(4);
        epic.setSubtasksIds(List.of(1, 2, 3));
        epic.setDuration(Duration.ofMinutes(30));
        epic.setStartTime(LocalDateTime.of(2022, 1, 28, 12, 0));
        String toString = Converter.taskToString(epic, -1, false);
        Task restored = Converter.stringToTask(toString);

        assertEquals(epic.toString(), restored.toString());
    }

}
