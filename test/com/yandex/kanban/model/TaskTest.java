package com.yandex.kanban.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {

    @Test
    void ifTaskEqualsIfIdEquals() {
        Task task = new Task("Task1", "1");
        task.setId(1);
        Task task2 = new Task("Task2", "2");
        task2.setId(1);
        assertEquals(task, task2, "задачи не равны при одинаковом id");
    }

    @Test
    void ifSubtaskEqualsIfIdEquals() {
        Subtask subtask1 = new Subtask("Task1", "1", 1);
        subtask1.setId(1);
        Subtask subtask2 = new Subtask("Task2", "2", 2);
        subtask2.setId(1);
        assertEquals(subtask1, subtask2, "подзадачи не равны при одинаковом id");
    }

    @Test
    void ifEpicEqualsIfIdEquals() {
        Epic epic1 = new Epic("Task1", "1");
        epic1.setId(1);
        Epic epic2 = new Epic("Task2", "2");
        epic2.setId(1);
        assertEquals(epic1, epic2, "подзадачи не равны при одинаковом id");
    }
}