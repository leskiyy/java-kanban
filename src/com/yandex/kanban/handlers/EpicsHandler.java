package com.yandex.kanban.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.yandex.kanban.model.Epic;
import com.yandex.kanban.model.Subtask;
import com.yandex.kanban.model.Task;
import com.yandex.kanban.service.TaskManager;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.regex.Pattern;

public class EpicsHandler extends BaseHttpHandler {
    public EpicsHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try (exchange) {
            String path = exchange.getRequestURI().getPath();
            String requestMethod = exchange.getRequestMethod();
            if (Pattern.matches("^/epics$", path)) {
                handlePath1(exchange, requestMethod);
            } else if (Pattern.matches("^/epics/[1-9]\\d*$", path)) {
                handlePath2(exchange, requestMethod, path);
            } else if (Pattern.matches("^/epics/[1-9]\\d*/subtasks$", path)) {
                handlePath3(exchange, requestMethod, path);
            } else {
                sendBadRequest(exchange, WRONG_PATH);
            }
        } catch (Exception e) {
            sendBadRequest(exchange, UNKNOWN_ERROR);
        }
    }

    private void handlePath1(HttpExchange exchange, String requestMethod) throws IOException {
        switch (requestMethod) {
            case GET -> {
                List<Epic> epics = manager.getAllEpics();
                String response = gson.toJson(epics);
                sendText(exchange, response);
            }
            case POST -> {
                InputStream requestBody = exchange.getRequestBody();
                byte[] bytes = requestBody.readAllBytes();
                String string = new String(bytes, DEFAULT_CHARSET);
                Task epic = null;
                try {
                    epic = gson.fromJson(string, Task.class);
                    if (!(epic instanceof Epic)) {
                        sendBadRequest(exchange, WRONG_TYPE);
                        return;
                    }
                } catch (Exception e) {
                    sendBadRequest(exchange, WRONG_JSON_SYNTAX);
                    return;
                }
                sendAddResult(exchange, epic);
            }
            default -> sendMethodNotAllowed(exchange);
        }
    }

    private void handlePath2(HttpExchange exchange, String requestMethod, String path) throws IOException {
        int id = 0;

        try {
            id = Integer.parseInt(path.split("/")[2]);
        } catch (NumberFormatException e) {
            sendBadRequest(exchange, WRONG_ID_FORMAT);
            return;
        }

        Task task = manager.getTaskById(id);
        if (!(task instanceof Epic)) {
            sendNotFound(exchange);
            return;
        }

        switch (requestMethod) {
            case GET -> {
                String response = gson.toJson(task);
                sendText(exchange, response);
            }
            case DELETE -> {
                manager.removeTaskById(id);
                exchange.sendResponseHeaders(200, 0);
            }
            default -> sendMethodNotAllowed(exchange);
        }
    }

    private void handlePath3(HttpExchange exchange, String requestMethod, String path) throws IOException {
        if (requestMethod.equals(GET)) {
            int id = 0;
            try {
                id = Integer.parseInt(path.split("/")[2]);
            } catch (NumberFormatException e) {
                sendBadRequest(exchange, WRONG_ID_FORMAT);
                return;
            }
            try {
                List<Subtask> subtasks = manager.getAllSubtasksByEpicId(id);
                String response = gson.toJson(subtasks);
                sendText(exchange, response);
            } catch (Exception e) {
                sendBadRequest(exchange, UNKNOWN_ERROR);
            }
        } else {
            sendMethodNotAllowed(exchange);
        }
    }
}