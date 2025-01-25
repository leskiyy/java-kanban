package com.yandex.kanban.server;

import com.sun.net.httpserver.HttpServer;
import com.yandex.kanban.handlers.*;
import com.yandex.kanban.service.Managers;
import com.yandex.kanban.service.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private static final String LOCALHOST = "localhost";
    private final InetSocketAddress socketAddress = new InetSocketAddress(LOCALHOST, PORT);
    private final InetSocketAddress unresolvedAddress = InetSocketAddress.createUnresolved(LOCALHOST, PORT);
    private final TaskManager manager;
    private HttpServer server = HttpServer.create(socketAddress, 0);

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
        server.setExecutor(null);
    }

    public void start() {
        this.server.start();
    }

    public void stop() {
        this.server.stop(0);
        this.server = null;
    }

    public static void main(String[] args) throws IOException {
        HttpTaskServer taskServer = new HttpTaskServer();
        taskServer.start();
    }
}
