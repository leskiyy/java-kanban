package com.yandex.kanban.server;

import com.sun.net.httpserver.HttpServer;
import com.yandex.kanban.handlers.*;
import com.yandex.kanban.model.Epic;
import com.yandex.kanban.model.Subtask;
import com.yandex.kanban.model.Task;
import com.yandex.kanban.service.InMemoryTaskManager;
import com.yandex.kanban.service.Managers;
import com.yandex.kanban.service.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private TaskManager manager;
    private HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
    private static TaskManager testManager = new InMemoryTaskManager();

    static {
        Task task1 = new Task("task1", "desc");
        task1.setStartTime(LocalDateTime.of(2000, 1, 1, 0, 0));
        task1.setDuration(Duration.ofMinutes(30));
        Task task2 = new Task("task2", "desc");
        task2.setStartTime(LocalDateTime.of(2000, 1, 1, 1, 0));
        task2.setDuration(Duration.ofMinutes(30));
        Epic epic = new Epic("epic", "desc");
        Subtask sub1 = new Subtask("sub1", "desc");
        sub1.setStartTime(LocalDateTime.of(2000, 1, 1, 2, 0));
        sub1.setDuration(Duration.ofMinutes(40));
        Subtask sub2 = new Subtask("sub2", "desc");
        sub2.setStartTime(LocalDateTime.of(2000, 1, 1, 3, 0));
        sub2.setDuration(Duration.ofMinutes(140));
        testManager.addTask(task1);
        testManager.addTask(task2);
        int epicId = testManager.addEpic(epic);
        sub1.setEpicId(epicId);
        sub2.setEpicId(epicId);
        testManager.addSubtask(sub1);
        testManager.addSubtask(sub2);
        testManager.getTaskById(2);
        testManager.getTaskById(3);
        testManager.getTaskById(4);
    }

    public HttpTaskServer() throws IOException {
        this.manager = Managers.getDefault();
        createContexts();
    }

    public HttpTaskServer(TaskManager manager) throws IOException {
        this.manager = manager;
        createContexts();
    }

    private void createContexts() {
        server.createContext("/tasks", new TasksHandler(this.manager));
        server.createContext("/subtasks", new SubtasksHandler(this.manager));
        server.createContext("/epics", new EpicsHandler(this.manager));
        server.createContext("/history", new HistoryHandler(this.manager));
        server.createContext("/prioritized", new PrioritizedHandler(this.manager));
    }

    public void start() {
        this.server.start();
    }

    public void stop() {
        this.server.stop(0);
    }

    public static void main(String[] args) throws IOException {
        HttpTaskServer taskServer = new HttpTaskServer(testManager);
        taskServer.start();
    }
}
