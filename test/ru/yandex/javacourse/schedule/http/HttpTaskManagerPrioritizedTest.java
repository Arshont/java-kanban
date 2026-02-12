package ru.yandex.javacourse.schedule.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.http.handlers.typeAdapters.DurationTypeAdapter;
import ru.yandex.javacourse.schedule.http.handlers.typeAdapters.LocalDateTimeTypeAdapter;
import ru.yandex.javacourse.schedule.manager.InMemoryTaskManager;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HttpTaskManagerPrioritizedTest {

    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = new GsonBuilder()
            .serializeNulls()
            .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
            .create();

    public HttpTaskManagerPrioritizedTest() throws IOException {
    }

    @BeforeEach
    public void setUp() {
        manager.deleteTasks();
        manager.deleteSubtasks();
        manager.deleteEpics();
        taskServer.startServer();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stopServer();
    }

    // ---------- GET /prioritized ----------

    @Test
    public void testGetPrioritizedTasksReturnsTasksInOrder() throws IOException, InterruptedException {
        Task task1 = new Task("Task 1", "D1", TaskStatus.NEW,
                Duration.ofMinutes(10), LocalDateTime.of(2026, 2, 12, 9, 0));
        Task task2 = new Task("Task 2", "D2", TaskStatus.NEW,
                Duration.ofMinutes(10), LocalDateTime.of(2026, 2, 12, 8, 0));

        int id1 = manager.addNewTask(task1);
        int id2 = manager.addNewTask(task2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Должен вернуться статус 200");

        Task[] prioritizedArray = gson.fromJson(response.body(), Task[].class);
        List<Task> prioritized = Arrays.asList(prioritizedArray);
        assertNotNull(prioritized, "Список приоритетных задач не должен быть null");
        assertEquals(2, prioritized.size(), "Должно вернуться две задачи");

        // Задача с более ранним стартом должна быть первой
        assertEquals(id2, prioritized.get(0).getId(), "Первая задача должна быть с более ранним временем старта");
        assertEquals(id1, prioritized.get(1).getId(), "Вторая задача должна быть с более поздним временем старта");
    }

    @Test
    public void testGetPrioritizedTasksEmpty() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Для пустого списка приоритетных задач должен вернуться статус 200");

        Task[] prioritizedArray = gson.fromJson(response.body(), Task[].class);
        List<Task> prioritized = Arrays.asList(prioritizedArray);
        assertNotNull(prioritized, "Список приоритетных задач не должен быть null");
        assertEquals(0, prioritized.size(), "Пустой список приоритетных задач должен возвращать пустой список");
    }

    @Test
    public void testUnknownHttpMethodForPrioritizedReturnsBadRequest() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritized");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .method("POST", HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(),
                "При отличном от GET методе для /prioritized должен вернуться статус 400 (BadRequest)");
    }

    @Test
    public void testTooLongPathForPrioritizedReturnsBadRequest() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/prioritized/extra/path");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(),
                "При слишком длинном пути для /prioritized должен вернуться статус 400 (BadRequest)");
    }
}

