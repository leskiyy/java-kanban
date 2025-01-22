package com.yandex.kanban.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.yandex.kanban.model.Epic;
import com.yandex.kanban.model.Subtask;
import com.yandex.kanban.model.Task;
import com.yandex.kanban.service.TaskManager;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import java.util.regex.Pattern;

public class TasksHandler extends BaseHttpHandler {

    public TasksHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try (exchange) {
            String path = exchange.getRequestURI().getPath();
            String requestMethod = exchange.getRequestMethod();
            if (Pattern.matches("^/tasks$", path)) {
                switch (requestMethod) {
                    case GET -> {
                        List<Task> tasks = manager.getAllTasks();
                        String response = gson.toJson(tasks);
                        sendText(exchange, response);
                    }
                    case POST -> {
                        InputStream requestBody = exchange.getRequestBody();
                        byte[] bytes = requestBody.readAllBytes();
                        String string = new String(bytes, DEFAULT_CHARSET);
                        Task incomeTask = null;

                        try {
                            incomeTask = gson.fromJson(string, Task.class);
                            if (incomeTask == null || incomeTask instanceof Subtask || incomeTask instanceof Epic) {
                                sendBadRequest(exchange, WRONG_TYPE);
                                return;
                            }
                        } catch (Exception e) {
                            sendBadRequest(exchange, WRONG_JSON_SYNTAX);
                            return;
                        }

                        if (incomeTask.getDuration() == null) {
                            incomeTask.setDuration(Duration.ZERO);
                        }
                        if (incomeTask.getId() > 0) {
                            int id = incomeTask.getId();
                            Task existedTask = manager.getTaskById(id);
                            if (existedTask == null || existedTask instanceof Subtask || existedTask instanceof Epic) {
                                sendNoFound(exchange);
                            } else {
                                sendUpdateResult(exchange, incomeTask, existedTask, id);
                            }
                        } else {
                            sendAddResult(exchange, incomeTask);
                        }
                    }
                    default -> sendMethodNotAllowed(exchange);
                }
            } else if (Pattern.matches("^/tasks/[1-9]\\d*$", path)) {
                int id = 0;

                try {
                    id = Integer.parseInt(path.split("/")[2]);
                } catch (NumberFormatException e) {
                    sendBadRequest(exchange, WRONG_ID_FORMAT);
                    return;
                }

                Task task = manager.getTaskById(id);
                if (task == null || task instanceof Epic || task instanceof Subtask) {
                    sendNoFound(exchange);
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
            } else {
                sendBadRequest(exchange, WRONG_PATH);
            }
        } catch (Throwable e) {
            sendBadRequest(exchange, UNKNOWN_ERROR);
        }
    }
}
