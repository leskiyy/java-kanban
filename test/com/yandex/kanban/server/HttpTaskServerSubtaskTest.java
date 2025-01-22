package com.yandex.kanban.server;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yandex.kanban.model.Epic;
import com.yandex.kanban.model.Subtask;
import com.yandex.kanban.model.Task;
import com.yandex.kanban.service.InMemoryTaskManager;
import com.yandex.kanban.service.TaskManager;
import com.yandex.kanban.util.GsonHolder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskServerSubtaskTest {
    private static String address = "http://localhost:8080/subtasks";
    private static HttpTaskServer server;
    private static TaskManager manager;
    private static Gson gson = GsonHolder.getGson();
    private static final HttpClient client = HttpClient.newHttpClient();
    private static HttpRequest request;
    private static HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
    private static Epic epic;
    private static Subtask subtask1;
    private static Subtask subtask2;

    @BeforeEach
    void initEpic() {
        epic = new Epic("Epic", "Desc");
    }

    @BeforeEach
    void intiClearServer() throws IOException {
        manager = new InMemoryTaskManager();
        server = new HttpTaskServer(manager);
        server.start();
    }
    @AfterEach
    void stopServer() {
        server.stop();
    }

    @Test
    void addSubtaskWithWrongEpicId() throws IOException, InterruptedException {
        manager.addEpic(epic);
        subtask1 = new Subtask("sub1", "desc");
        String json = gson.toJson(subtask1);
        request = HttpRequest.newBuilder()
                .uri(URI.create(address))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = client.send(request, handler);

        assertEquals(400, response.statusCode(), "Некорректный код ответа");
        assertEquals("wrong epic id", response.body(), "Некорректное тело ответа");
    }

    @Test
    void addSubtask() throws IOException, InterruptedException {
        manager.addEpic(epic);
        subtask1 = new Subtask("sub1", "desc");
        subtask1.setEpicId(1);
        String json = gson.toJson(subtask1);
        request = HttpRequest.newBuilder()
                .uri(URI.create(address))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = client.send(request, handler);

        assertEquals(201, response.statusCode(), "Некорректный код ответа");
        assertEquals("TASK ADDED SUCCESSFULLY", response.body(), "Некорректное тело ответа");
    }

    @Test
    void addSubtaskWithIntersection() throws IOException, InterruptedException {
        manager.addEpic(epic);
        subtask1 = new Subtask("sub1", "desc");
        subtask1.setStartTime(LocalDateTime.now());
        subtask1.setDuration(Duration.ofMinutes(60));
        subtask1.setEpicId(1);
        subtask2 = new Subtask("sub1", "desc");
        subtask2.setStartTime(LocalDateTime.now());
        subtask2.setDuration(Duration.ofMinutes(60));
        subtask2.setEpicId(1);
        manager.addSubtask(subtask1);
        String json = gson.toJson(subtask2);
        request = HttpRequest.newBuilder()
                .uri(URI.create(address))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = client.send(request, handler);

        assertEquals(406, response.statusCode());
        assertEquals("INPUT TASK HAS INTERACTIONS\n" +
                        "Task id=3 saved in manager, but its startTime/duration are set to null/zero", response.body(),
                "Некорректное тело ответа");
    }

    @Test
    void updateSubtaskWithIntersection() throws IOException, InterruptedException {
        manager.addEpic(epic);
        subtask1 = new Subtask("sub1", "desc");
        subtask1.setStartTime(LocalDateTime.now());
        subtask1.setDuration(Duration.ofMinutes(60));
        subtask1.setEpicId(1);
        subtask1.setId(2);
        String json = gson.toJson(subtask1);
        subtask1.setStartTime(LocalDateTime.now().minus(Duration.ofMinutes(120)));
        subtask2 = new Subtask("sub1", "desc");
        subtask2.setStartTime(LocalDateTime.now());
        subtask2.setDuration(Duration.ofMinutes(60));
        subtask2.setEpicId(1);
        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);
        request = HttpRequest.newBuilder()
                .uri(URI.create(address))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = client.send(request, handler);
        assertEquals(406, response.statusCode());
        assertEquals("INPUT TASK HAS INTERACTIONS\n" +
                        "task id=2 is crossing existing ones because of new start time", response.body(),
                "Некорректное тело ответа");
    }

    @Test
    void updateSubtaskWhichNotExist() throws IOException, InterruptedException {
        manager.addEpic(epic);
        subtask1 = new Subtask("sub1", "desc");
        subtask1.setEpicId(1);
        subtask1.setId(3);
        String json = gson.toJson(subtask1);
        request = HttpRequest.newBuilder()
                .uri(URI.create(address))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = client.send(request, handler);

        assertEquals(404, response.statusCode(), "Некорректный код ответа");
        List<Integer> subtasksIds = epic.getSubtasksIds();
        assertEquals(0, subtasksIds.size(), "Неверное количество задач у епика");
    }

    @Test
    void deleteSubtask() throws IOException, InterruptedException {
        manager.addEpic(epic);
        subtask1 = new Subtask("sub1", "desc");
        subtask1.setEpicId(1);
        manager.addSubtask(subtask1);
        assertEquals(1, epic.getSubtasksIds().size(), "сабтаск не добавился");
        assertEquals(2, subtask1.getId(), "Не установился правильный id сабтаски");
        request = HttpRequest.newBuilder()
                .uri(URI.create(address + "/2"))
                .DELETE()
                .build();
        HttpResponse<String> response = client.send(request, handler);

        assertEquals(200, response.statusCode(), "Неверный код ответа");
        assertEquals(0, epic.getSubtasksIds().size(), "сабтаск не добавился");
        assertEquals(0, manager.getAllSubtasks().size(), "Не удалился сабтаск");
    }

    @Test
    void getSubtasks() throws IOException, InterruptedException {
        manager.addEpic(epic);
        subtask1 = new Subtask("sub1", "desc");
        subtask1.setDuration(Duration.ofMinutes(60));
        subtask1.setEpicId(1);
        subtask1.setId(2);
        subtask1.setStartTime(LocalDateTime.now().minus(Duration.ofMinutes(120)));
        subtask2 = new Subtask("sub1", "desc");
        subtask2.setStartTime(LocalDateTime.now());
        subtask2.setDuration(Duration.ofMinutes(60));
        subtask2.setEpicId(1);
        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);

        request = HttpRequest.newBuilder()
                .uri(URI.create(address))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, handler);

        assertEquals(200, response.statusCode(), "Неверный код ответа");

        List<Subtask> subtasks = manager.getAllSubtasks();
        String body = response.body();
        List<Subtask> receivedSubtasks = gson.fromJson(body, new TypeToken<List<Subtask>>(){}.getType());
        assertEquals(subtasks.size(), receivedSubtasks.size(), "Неверный размер коллекции сабтасков");
        for (int i = 0; i < subtasks.size(); i++) {
            assertEquals(subtasks.get(i).toString(), receivedSubtasks.get(i).toString(), "Не все поля равны");
        }
    }

    @Test
    void getSubtask() throws IOException, InterruptedException {
        manager.addEpic(epic);
        subtask1 = new Subtask("sub1", "desc");
        subtask1.setStartTime(LocalDateTime.now());
        subtask1.setDuration(Duration.ofMinutes(60));
        subtask1.setEpicId(1);
        manager.addSubtask(subtask1);

        request = HttpRequest.newBuilder()
                .uri(URI.create(address + "/2"))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, handler);

        assertEquals(200, response.statusCode(), "Неверный код ответа");

        Task task = gson.fromJson(response.body(), Task.class);
        assertInstanceOf(Subtask.class, task, "Задача не является сабтаской");
        assertEquals(subtask1.toString(), task.toString(),"Не все поля равны");
    }
}
