package com.yandex.kanban.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.yandex.kanban.model.Task;
import com.yandex.kanban.service.TaskManager;

import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

public class HistoryHandler extends BaseHttpHandler {
    public HistoryHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try (exchange) {
            String path = exchange.getRequestURI().getPath();
            String requestMethod = exchange.getRequestMethod();
            if (Pattern.matches("^/history$", path)) {
                if (requestMethod.equals(GET)) {
                    List<Task> history = manager.getHistoryManager().getHistory();
                    String response = gson.toJson(history);
                    sendText(exchange, response);
                } else {
                    sendMethodNotAllowed(exchange);
                }
            } else {
                sendBadRequest(exchange);
            }
        } catch (Exception e) {
            sendBadRequest(exchange);
        }
    }
}
