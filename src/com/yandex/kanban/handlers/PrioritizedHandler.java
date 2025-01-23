package com.yandex.kanban.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.yandex.kanban.model.Task;
import com.yandex.kanban.service.TaskManager;

import java.io.IOException;
import java.util.Set;
import java.util.regex.Pattern;

public class PrioritizedHandler extends BaseHttpHandler {

    public PrioritizedHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try (exchange) {
            String path = exchange.getRequestURI().getPath();
            String requestMethod = exchange.getRequestMethod();
            if (Pattern.matches("^/prioritized$", path)) {
                if (requestMethod.equals(GET)) {
                    Set<Task> prioritizedTasks = manager.getPrioritizedTasks();
                    String response = gson.toJson(prioritizedTasks);
                    sendText(exchange, response);
                } else {
                    sendMethodNotAllowed(exchange);
                }
            } else {
                sendBadRequest(exchange, WRONG_PATH);
            }
        } catch (Exception e) {
            sendBadRequest(exchange, UNKNOWN_ERROR);
        }
    }
}
