package ru.yandex.javacourse.schedule.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.http.handlers.serializers_deserializers.EpicDeserializer;
import ru.yandex.javacourse.schedule.http.handlers.typeAdapters.DurationTypeAdapter;
import ru.yandex.javacourse.schedule.http.handlers.typeAdapters.LocalDateTimeTypeAdapter;
import ru.yandex.javacourse.schedule.manager.InMemoryTaskManager;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Epic;
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

public class HttpTaskManagerHistoryTest {

    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = new GsonBuilder()
            .serializeNulls()
            .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
            .registerTypeAdapter(Epic.class, new EpicDeserializer())
            .create();

    public HttpTaskManagerHistoryTest() throws IOException {
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

    // ---------- GET /history ----------

    @Test
    public void testGetHistoryReturnsViewedTasks() throws IOException, InterruptedException {
        Task task1 = new Task("Task 1", "D1", TaskStatus.NEW,
                Duration.ofMinutes(10), LocalDateTime.of(2026, 2, 12, 8, 0));
        Task task2 = new Task("Task 2", "D2", TaskStatus.NEW,
                Duration.ofMinutes(20), LocalDateTime.of(2026, 2, 12, 9, 0));

        int id1 = manager.addNewTask(task1);
        int id2 = manager.addNewTask(task2);

        // Добавляем задачи в историю через getTask
        manager.getTask(id1);
        manager.getTask(id2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Должен вернуться статус 200");

        Task[] historyArray = gson.fromJson(response.body(), Task[].class);
        List<Task> history = Arrays.asList(historyArray);
        assertNotNull(history, "История не должна быть null");
        assertEquals(2, history.size(), "В истории должно быть две задачи");
        assertEquals(id1, history.get(0).getId(), "Первой в истории должна быть первая просмотренная задача");
        assertEquals(id2, history.get(1).getId(), "Второй в истории должна быть вторая просмотренная задача");
    }

    @Test
    public void testGetHistoryEmpty() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Для пустой истории также должен вернуться статус 200");

        Task[] historyArray = gson.fromJson(response.body(), Task[].class);
        List<Task> history = Arrays.asList(historyArray);
        assertNotNull(history, "История не должна быть null");
        assertEquals(0, history.size(), "Пустая история должна возвращать пустой список");
    }

    @Test
    public void testUnknownHttpMethodForHistoryReturnsBadRequest() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/history");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .method("POST", HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(),
                "При отличном от GET методе для /history должен вернуться статус 400 (BadRequest)");
    }

    @Test
    public void testTooLongPathForHistoryReturnsBadRequest() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/history/extra/path");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(),
                "При слишком длинном пути для /history должен вернуться статус 400 (BadRequest)");
    }
}

