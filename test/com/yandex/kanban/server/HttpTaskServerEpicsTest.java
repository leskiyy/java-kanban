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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskServerEpicsTest {
    private static String address = "http://localhost:8080/epics";
    private static HttpTaskServer server;
    private static TaskManager manager;
    private static Gson gson = GsonHolder.getGson();
    private static final HttpClient client = HttpClient.newHttpClient();
    private static HttpRequest request;
    private static HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();
    private static Epic epic1;
    private static Epic epic2;
    private static Subtask subtask1;
    private static Subtask subtask2;
    private static Subtask subtask3;
    private static Subtask subtask4;

    @BeforeEach
    void initEpic() {
        epic1 = new Epic("Epic1", "Desc");
        epic2 = new Epic("Epic2", "Desc");

        subtask1 = new Subtask("Sub1", "Desc");
        subtask1.setEpicId(1);
        subtask1.setStartTime(LocalDateTime.of(2000, 1, 1, 0, 0));
        subtask1.setDuration(Duration.ofMinutes(30));

        subtask2 = new Subtask("Sub2", "Desc");
        subtask2.setStartTime(LocalDateTime.of(2000, 1, 1, 0, 30));
        subtask2.setDuration(Duration.ofMinutes(40));
        subtask2.setEpicId(1);

        subtask3 = new Subtask("Sub3", "Desc");
        subtask3.setEpicId(1);

        subtask4 = new Subtask("Sub4", "Desc");
        subtask4.setEpicId(5);
        subtask4.setStartTime(LocalDateTime.of(2000, 1, 1, 2, 30));
        subtask4.setDuration(Duration.ofMinutes(60));

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
    void getEpics() throws IOException, InterruptedException {
        manager.addEpic(epic1);
        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);
        manager.addSubtask(subtask3);
        manager.addEpic(epic2);
        manager.addSubtask(subtask4);

        request = HttpRequest.newBuilder()
                .uri(URI.create(address))
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, handler);

        assertEquals(200, response.statusCode(), "Неверный код ответа");

        List<Epic> receivedEpics = gson.fromJson(response.body(), new TypeToken<List<Epic>>(){}.getType());
        List<Epic> epics = manager.getAllEpics();

        assertEquals(epics.size(), receivedEpics.size(), "Неверное количество епиков");
        for (int i = 0; i < epics.size(); i++) {
            assertEquals(epics.get(i).toString(), receivedEpics.get(i).toString(), "Не все поля равны");
        }
    }

    @Test
    void addEpic() throws IOException, InterruptedException {
        Epic epicToCreate = new Epic("EpicToCreate", "Desc");
        epicToCreate.setDuration(Duration.ofMinutes(123));
        epicToCreate.setStartTime(LocalDateTime.now());
        epicToCreate.setSubtasksIds(List.of(1,2,5));
        String json = gson.toJson(epicToCreate);
        request = HttpRequest.newBuilder()
                .uri(URI.create(address))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = client.send(request, handler);

        assertEquals(201, response.statusCode(), "Неверный код ответа");
        assertEquals("TASK ADDED SUCCESSFULLY", response.body(), "Неверное тело ответа");

        List<Epic> epics = manager.getAllEpics();

        assertEquals(1, epics.size(), "Епик не добавился");
        assertEquals(1, epics.getFirst().getId(), "Неверный id");
        assertEquals("EpicToCreate", epics.getFirst().getTitle(), "Невернвое название");
        assertEquals("Desc", epics.getFirst().getDescription(), "Невернвое описание");
        assertEquals(Collections.emptyList(), epics.getFirst().getSubtasksIds(),
                "Неверное количество сабтасок в епике");
        assertEquals(Duration.ZERO, epics.getFirst().getDuration(), "Неверная длительность");
        assertNull(epics.getFirst().getStartTime(), "Неверное время начала");
    }
    @Test
    void addWrongEpic() throws IOException, InterruptedException {
        Task epicToCreate = new Task("EpicToCreate", "Desc");
        epicToCreate.setDuration(Duration.ofMinutes(123));
        epicToCreate.setStartTime(LocalDateTime.now());
        String json = gson.toJson(epicToCreate);
        request = HttpRequest.newBuilder()
                .uri(URI.create(address))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
        HttpResponse<String> response = client.send(request, handler);

        assertEquals(400, response.statusCode(), "Неверный код ответа");
        assertEquals("WRONG TYPE", response.body(), "Неверное тело ответа");
    }

    @Test
    void deleteEpic() throws IOException, InterruptedException {
        manager.addTask(epic1);
        manager.addSubtask(subtask1);

        URI uri = URI.create(address + "/1");
        request = HttpRequest.newBuilder()
                .uri(uri)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, handler);

        assertEquals(200, response.statusCode());

        List<Epic> epics = manager.getAllEpics();
        List<Subtask> subtasks = manager.getAllSubtasks();
        assertEquals(0, epics.size(), "Задача не удалилась");
        assertEquals(0, subtasks.size(), "Задача не удалилась");
    }

    @Test
    void deleteNotEpic() throws IOException, InterruptedException {
        manager.addTask(epic1);
        manager.addSubtask(subtask1);
        URI uri = URI.create(address + "/2");
        request = HttpRequest.newBuilder()
                .uri(uri)
                .DELETE()
                .build();

        HttpResponse<String> response = client.send(request, handler);

        assertEquals(404, response.statusCode());

        List<Epic> epics = manager.getAllEpics();
        List<Subtask> subtasks = manager.getAllSubtasksByEpicId(1);
        assertEquals(1, epics.size(), "Задача удалилась");
        assertEquals(1, subtasks.size(), "Задача удалилась");
    }

    @Test
    void getSubtasksByEpicId() throws IOException, InterruptedException {
        manager.addEpic(epic1);
        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);
        manager.addSubtask(subtask3);

        URI uri = URI.create(address + "/1/subtasks");
        request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, handler);

        assertEquals(200, response.statusCode());
        List<Subtask> subtasks = manager.getAllSubtasksByEpicId(1);
        List<Subtask> receivedSubtasks = gson.fromJson(response.body(), new TypeToken<List<Task>>(){}.getType());

        assertEquals(subtasks.size(), receivedSubtasks.size(), "Неверное количество подзадач");
        for (int i = 0; i < subtasks.size(); i++) {
            assertEquals(subtasks.get(i).toString(), receivedSubtasks.get(i).toString(), "Не все поля равны");
        }
    }

    @Test
    void getEpic() throws IOException, InterruptedException {
        manager.addEpic(epic1);
        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);
        manager.addSubtask(subtask3);

        URI uri = URI.create(address + "/1");
        request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, handler);

        assertEquals(200, response.statusCode());

        Task epic = gson.fromJson(response.body(), Task.class);

        assertInstanceOf(Epic.class, epic, "Возвращен таск неверного типа");

        assertEquals(epic1.toString(), epic.toString(), "Поля не совпадают");
    }

    @Test
    void getWrongEpic() throws IOException, InterruptedException {
        manager.addEpic(epic1);
        manager.addSubtask(subtask1);
        manager.addSubtask(subtask2);
        manager.addSubtask(subtask3);

        URI uri = URI.create(address + "/2");
        request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, handler);

        assertEquals(404, response.statusCode());

    }
}
