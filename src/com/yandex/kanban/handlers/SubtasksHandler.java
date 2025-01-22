package com.yandex.kanban.handlers;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.yandex.kanban.model.Subtask;
import com.yandex.kanban.model.Task;
import com.yandex.kanban.service.TaskManager;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import java.util.regex.Pattern;

public class SubtasksHandler extends BaseHttpHandler {
    public SubtasksHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try (exchange) {
            String path = exchange.getRequestURI().getPath();
            String requestMethod = exchange.getRequestMethod();
            if (Pattern.matches("^/subtasks$", path)) {
                switch (requestMethod) {
                    case GET -> {
                        List<Subtask> subs = manager.getAllSubtasks();
                        String response = gson.toJson(subs);
                        sendText(exchange, response);
                    }
                    case POST -> {
                        InputStream requestBody = exchange.getRequestBody();
                        byte[] bytes = requestBody.readAllBytes();
                        String string = new String(bytes, DEFAULT_CHARSET);
                        Task incomeSub = null;

                        try {
                            incomeSub = gson.fromJson(string, Task.class);
                            if (!(incomeSub instanceof Subtask)) {
                                sendBadRequest(exchange, WRONG_TYPE);
                                return;
                            }
                        } catch (JsonSyntaxException e) {
                            sendBadRequest(exchange, WRONG_JSON_SYNTAX);
                            return;
                        }

                        if (incomeSub.getDuration() == null) {
                            incomeSub.setDuration(Duration.ZERO);
                        }

                        if (incomeSub.getId() > 0) {
                            int subId = incomeSub.getId();
                            Task taskById = manager.getTaskById(subId);
                            if (!(taskById instanceof Subtask)) {
                                sendNoFound(exchange);
                            } else {
                                Subtask existedSub = (Subtask) taskById;
                                sendUpdateResult(exchange, incomeSub, existedSub, subId);
                            }
                        } else {
                            sendAddResult(exchange, incomeSub);
                        }
                    }
                    default -> sendMethodNotAllowed(exchange);
                }
            } else if (Pattern.matches("^/subtasks/[1-9]\\d*$", path)) {
                int id = 0;

                try {
                    id = Integer.parseInt(path.split("/")[2]);
                } catch (NumberFormatException e) {
                    sendBadRequest(exchange, WRONG_ID_FORMAT);
                    return;
                }

                Task task = manager.getTaskById(id);
                if (!(task instanceof Subtask)) {
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
        } catch (Exception e) {
            sendBadRequest(exchange, UNKNOWN_ERROR);
        }
    }
}