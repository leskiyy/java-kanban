package com.yandex.kanban.server;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yandex.kanban.model.Epic;
import com.yandex.kanban.model.Subtask;
import com.yandex.kanban.model.Task;
import com.yandex.kanban.service.InMemoryTaskManager;
import com.yandex.kanban.service.TaskManager;
import com.yandex.kanban.util.GsonHolder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskServerHistoryAndPrioritizedTest {
    private static URI uriPrioritized = URI.create("http://localhost:8080/prioritized");
    private static URI uriHistory = URI.create("http://localhost:8080/history");
    private static HttpTaskServer server;
    private static TaskManager manager;
    private static Gson gson = GsonHolder.getGson();
    private static final HttpClient client = HttpClient.newHttpClient();
    private static HttpRequest request;
    private static HttpResponse.BodyHandler<String> handler = HttpResponse.BodyHandlers.ofString();

    @BeforeAll
    static void initManager() throws IOException {
        manager = new InMemoryTaskManager();
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
        manager.addTask(task1);
        manager.addTask(task2);
        int epicId = manager.addEpic(epic);
        sub1.setEpicId(epicId);
        sub2.setEpicId(epicId);
        manager.addSubtask(sub1);
        manager.addSubtask(sub2);
        manager.getTaskById(2);
        manager.getTaskById(3);
        manager.getTaskById(4);
        server = new HttpTaskServer(manager);
        server.start();
    }

    @Test
    void getHistory() throws IOException, InterruptedException {
        request = HttpRequest.newBuilder()
                .uri(uriHistory)
                .GET()
                .build();
        HttpResponse<String> response = client.send(request, handler);

        assertEquals(200, response.statusCode());

        List<Task> returnedHistory = gson.fromJson(response.body(), new TypeToken<List<Task>>(){}.getType());
        List<Task> history = manager.getHistoryManager().getHistory();

        assertEquals(history.size(), returnedHistory.size(), "Не совпадабт размеры истории");
        for (int i = 0; i < history.size(); i++) {
            assertEquals(history.get(i).toString(), returnedHistory.get(i).toString(), "Не все поля равны");
        }
    }

    @Test
    void getPrioritized() throws IOException, InterruptedException {
        request = HttpRequest.newBuilder()
            .uri(uriPrioritized)
            .GET()
            .build();
        HttpResponse<String> response = client.send(request, handler);
        List<Task> returnedPrioritizedTasks = gson.fromJson(response.body(), new TypeToken<List<Task>>(){}.getType());
        Set<Task> prioritizedTasks = manager.getPrioritizedTasks();
        assertEquals(returnedPrioritizedTasks.size(), prioritizedTasks.size(), "Не совпадабт размеры истории");
        Iterator<Task> iterator = prioritizedTasks.iterator();
        int count = 0;
        while (iterator.hasNext()) {
            Task orig = iterator.next();
            Task returned = returnedPrioritizedTasks.get(count);
            assertEquals(orig.toString(), returned.toString(), "Не все поля равны");
            count++;
        }
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

}
