package com.yandex.kanban.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.yandex.kanban.exeptions.IntersectionException;
import com.yandex.kanban.model.Epic;
import com.yandex.kanban.model.Subtask;
import com.yandex.kanban.model.Task;
import com.yandex.kanban.service.TaskManager;
import com.yandex.kanban.util.GsonHolder;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public abstract class BaseHttpHandler implements HttpHandler {
    private static final String HAS_INTERACTIONS = "INPUT TASK HAS INTERACTIONS";
    private static final String ADDED_SUCCESSFULLY = "TASK ADDED SUCCESSFULLY";
    private static final String UPDATED_SUCCESSFULLY = "TASK UPDATED SUCCESSFULLY";
    protected static final String WRONG_PATH = "WRONG PATH";
    protected static final String WRONG_TYPE = "WRONG TYPE";
    protected static final String WRONG_ID_FORMAT = "WRONG ID FORMAT";
    protected static final String WRONG_JSON_SYNTAX = "WRONG JSON SYNTAX";
    protected static final String UNKNOWN_ERROR = "UNKNOWN ERROR";
    protected static final String GET = "GET";
    protected static final String POST = "POST";
    protected static final String DELETE = "DELETE";
    protected static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    protected TaskManager manager;
    protected final Gson gson = GsonHolder.getGson();

    public BaseHttpHandler(TaskManager manager) {
        this.manager = manager;
    }

    protected void sendText(HttpExchange exchange, String response) throws IOException {
        byte[] responseBytes = response.getBytes(DEFAULT_CHARSET);
        exchange.sendResponseHeaders(200, responseBytes.length);
        exchange.getResponseBody().write(responseBytes);
    }

    protected void sendNoFound(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(404, 0);
    }

    protected void sendBadRequest(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(400, 0);
    }

    protected void sendBadRequest(HttpExchange exchange, String response) throws IOException {
        byte[] responseBytes = response.getBytes(DEFAULT_CHARSET);
        exchange.sendResponseHeaders(400, responseBytes.length);
        exchange.getResponseBody().write(responseBytes);
    }

    protected void sendHasInteractions(HttpExchange exchange, String description) throws IOException {
        byte[] responseBytes = (HAS_INTERACTIONS + '\n' + description).getBytes(DEFAULT_CHARSET);
        exchange.sendResponseHeaders(406, responseBytes.length);
        exchange.getResponseBody().write(responseBytes);
    }

    protected void sendMethodNotAllowed(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(405, 0);
    }

    private void sendAddedSuccessfully(HttpExchange exchange) throws IOException {
        byte[] responseBytes = ADDED_SUCCESSFULLY.getBytes(DEFAULT_CHARSET);
        exchange.sendResponseHeaders(201, responseBytes.length);
        exchange.getResponseBody().write(responseBytes);
    }

    private void sendUpdatedSuccessfully(HttpExchange exchange) throws IOException {
        byte[] responseBytes = UPDATED_SUCCESSFULLY.getBytes(DEFAULT_CHARSET);
        exchange.sendResponseHeaders(201, responseBytes.length);
        exchange.getResponseBody().write(responseBytes);
    }

    protected void sendUpdateResult(HttpExchange exchange, Task income, Task existed, int id) throws IOException {
        try {
            boolean succeed = updateTask(income, existed, id);
            if (succeed) {
                sendUpdatedSuccessfully(exchange);
            } else {
                sendBadRequest(exchange);
            }
        } catch (Exception e) {
            if (e instanceof IntersectionException) {
                sendHasInteractions(exchange, e.getMessage());
            } else {
                sendBadRequest(exchange);
            }
        }
    }

    protected void sendAddResult(HttpExchange exchange, Task task) throws IOException {
        try {
            manager.addTask(task);
        } catch (Exception e) {
            if (e instanceof IntersectionException) {
                sendHasInteractions(exchange, e.getMessage());
            } else {
                sendBadRequest(exchange, e.getMessage());
            }
        }
        sendAddedSuccessfully(exchange);
    }

    private boolean updateTask(Task income, Task existed, int id) {
        if (income instanceof Epic) {
            if (!isValidEpicUpdate(income, existed)) {
                return false;
            }
        }
        if (income instanceof Subtask) {
            if (!isValidSubtaskUpdate(income, existed)) {
                return false;
            }
        }
        if (!income.getTitle().equals(existed.getTitle())) {
            manager.updateTitle(income.getTitle(), id);
        }
        if (!income.getDescription().equals(existed.getDescription())) {
            manager.updateDescription(income.getDescription(), id);
        }
        if (income.getStatus() != existed.getStatus() && !(income instanceof Epic)) {
            manager.updateStatus(income.getStatus(), id);
        }
        if (!income.getDuration().equals(existed.getDuration())) {
            manager.updateDuration(income.getDuration(), id);
        }
        if (!income.getStartTime().equals(existed.getStartTime())) {
            manager.updateStartTime(income.getStartTime(), id);
        }
        return true;
    }

    private boolean isValidEpicUpdate(Task income, Task existed) {
        Epic epicIncome = (Epic) income;
        Epic epicExited = (Epic) existed;
        if (!epicIncome.getSubtasksIds().equals(epicExited.getSubtasksIds())) {
            return false;
        }
        if (!epicIncome.getStatus().equals(epicExited.getStatus())) {
            return false;
        }
        if (!epicIncome.getDuration().equals(epicExited.getDuration())) {
            return false;
        }
        if (!epicIncome.getStartTime().equals(epicExited.getStartTime())) {
            return false;
        }
        return true;
    }

    private boolean isValidSubtaskUpdate(Task income, Task existed) {
        Subtask subIncome = (Subtask) income;
        Subtask subExited = (Subtask) existed;
        if (subExited.getEpicId() != subIncome.getEpicId()) {
            return false;
        }
        return true;
    }
}
