package ru.yandex.javacourse.schedule.http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
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

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerTasksTest {

    // создаём экземпляр InMemoryTaskManager
    TaskManager manager = new InMemoryTaskManager();
    // передаём его в качестве аргумента в конструктор HttpTaskServer
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = new GsonBuilder()
            .serializeNulls()
            .registerTypeAdapter(Duration.class, new DurationTypeAdapter().nullSafe())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter().nullSafe())
            .registerTypeAdapter(Epic.class, new EpicDeserializer())
            .create();

    public HttpTaskManagerTasksTest() throws IOException {
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

    // ---------- POST /tasks (создание задачи) ----------

    @Test
    public void testAddTask() throws IOException, InterruptedException {

        Task task = new Task("Test 2", "Testing task 2",
                TaskStatus.NEW, Duration.ofMinutes(5), LocalDateTime.now());
        String taskJson = gson.toJson(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        assertEquals(1, JsonParser.parseString(response.body()).getAsJsonObject().get("task_id").getAsInt());

        assertEquals(1, manager.getTasks().size(), "Должна быть добавлена одна задача");
        assertEquals("Test 2", manager.getTasks().get(0).getName(), "Имя задачи должно совпадать");
    }

    @Test
    public void testFailedAddTask() throws IOException, InterruptedException {
        Task task = new Task("Test 1", "Testing task 1",
                TaskStatus.NEW, Duration.ofMinutes(30), LocalDateTime.of(2026, 2, 12, 8, 15, 0));
        manager.addNewTask(task);

        Task taskForRequest = new Task("Test 2", "Testing task 2",
                TaskStatus.NEW, Duration.ofMinutes(30), LocalDateTime.of(2026, 2, 12, 8, 0, 0));

        String taskJson = gson.toJson(taskForRequest);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).POST(HttpRequest.BodyPublishers.ofString(taskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode(), "Должен вернуться код, соответсвующий ошибке пересечения задач");
        assertEquals(1, manager.getTasks().size(), "Должна быть добавлена одна задача");
        assertSame(task, manager.getTasks().get(0), "Задача не должна измениться");
    }

    // ---------- GET /tasks ----------

    @Test
    public void testGetTasksReturnsAllTasks() throws IOException, InterruptedException {
        Task task1 = new Task("Task 1", "Desc 1",
                TaskStatus.NEW, Duration.ofMinutes(10), LocalDateTime.of(2026, 2, 12, 9, 0));
        Task task2 = new Task("Task 2", "Desc 2",
                TaskStatus.IN_PROGRESS, Duration.ofMinutes(20), LocalDateTime.of(2026, 2, 12, 10, 0));

        manager.addNewTask(task1);
        manager.addNewTask(task2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Должен вернуться статус 200");

        Task[] tasksArray = gson.fromJson(response.body(), Task[].class);
        List<Task> tasksFromResponse = Arrays.asList(tasksArray);
        assertNotNull(tasksFromResponse, "Список задач не должен быть null");
        assertEquals(2, tasksFromResponse.size(), "Должно вернуться две задачи");
    }

    // ---------- GET /tasks/{id} ----------

    @Test
    public void testGetTaskByIdReturnsTask() throws IOException, InterruptedException {
        Task task = new Task("Single", "Single desc",
                TaskStatus.NEW, Duration.ofMinutes(15), LocalDateTime.of(2026, 2, 12, 11, 0));
        int id = manager.addNewTask(task);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + id);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Должен вернуться статус 200");

        Task taskFromResponse = gson.fromJson(response.body(), Task.class);
        assertNotNull(taskFromResponse, "Задача не должна быть null");
        assertEquals(id, taskFromResponse.getId(), "id задачи должен совпадать");
        assertEquals(task.getName(), taskFromResponse.getName(), "Имя задачи должно совпадать");
    }

    @Test
    public void testGetTaskByIdNotFound() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/999");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Для несуществующей задачи должен вернуться статус 404");
    }

    @Test
    public void testGetTaskByIdWithBadIdFormatReturnsBadRequest() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/abc");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(), "Для некорректного id должен вернуться статус 400");
    }

    // ---------- POST /tasks/{id} (обновление задачи) ----------

    @Test
    public void testUpdateTaskSuccess() throws IOException, InterruptedException {
        Task original = new Task("Old name", "Old desc",
                TaskStatus.NEW, Duration.ofMinutes(15), LocalDateTime.of(2026, 2, 12, 12, 0));
        int id = manager.addNewTask(original);

        Task updated = new Task("New name", "New desc",
                TaskStatus.IN_PROGRESS, Duration.ofMinutes(30), LocalDateTime.of(2026, 2, 12, 13, 0));
        String updatedJson = gson.toJson(updated);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + id);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(updatedJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "При успешном обновлении должен вернуться статус 201");
        assertEquals(id, JsonParser.parseString(response.body()).getAsJsonObject().get("task_id").getAsInt());

        Task taskFromManager = manager.getTask(id).orElse(null);
        assertNotNull(taskFromManager, "Задача должна существовать после обновления");
        assertEquals("New name", taskFromManager.getName(), "Имя задачи должно обновиться");
        assertEquals(TaskStatus.IN_PROGRESS, taskFromManager.getStatus(), "Статус задачи должен обновиться");
    }

    @Test
    public void testUpdateTaskNotFound() throws IOException, InterruptedException {
        Task updated = new Task("Name", "Desc",
                TaskStatus.NEW, Duration.ofMinutes(10), LocalDateTime.of(2026, 2, 12, 14, 0));
        String updatedJson = gson.toJson(updated);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/999");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(updatedJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Для обновления несуществующей задачи должен вернуться статус 404");
    }

    @Test
    public void testUpdateTaskWithBadJsonReturnsBadRequest() throws IOException, InterruptedException {
        Task original = new Task("Old name", "Old desc",
                TaskStatus.NEW, Duration.ofMinutes(15), LocalDateTime.of(2026, 2, 12, 15, 0));
        int id = manager.addNewTask(original);

        String badJson = "{this is not valid json}";

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + id);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(badJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(), "При некорректном JSON должен вернуться статус 400");
    }

    @Test
    public void testUpdateTaskHasInteractionsReturnsNotAcceptable() throws IOException, InterruptedException {
        // первая задача, уже существующая в менеджере
        Task first = new Task("First", "First desc",
                TaskStatus.NEW, Duration.ofMinutes(30), LocalDateTime.of(2026, 2, 12, 8, 15));
        manager.addNewTask(first);

        // вторая задача, изначально без пересечений
        Task second = new Task("Second", "Second desc",
                TaskStatus.NEW, Duration.ofMinutes(30), LocalDateTime.of(2026, 2, 12, 9, 0));
        int secondId = manager.addNewTask(second);

        // обновляем вторую задачу так, чтобы она пересекалась с первой
        Task secondWithIntersection = new Task("Second updated", "Second desc updated",
                TaskStatus.NEW, Duration.ofMinutes(30), LocalDateTime.of(2026, 2, 12, 8, 20));
        String updatedJson = gson.toJson(secondWithIntersection);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + secondId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(updatedJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode(),
                "При обновлении задачи с пересечением должен вернуться статус 406 (HasInteractions)");

        Task taskFromManager = manager.getTask(secondId).orElse(null);
        assertNotNull(taskFromManager, "Задача должна остаться в менеджере");
        assertEquals("Second", taskFromManager.getName(),
                "Имя задачи не должно измениться при неуспешном обновлении");
    }

    // ---------- DELETE /tasks/{id} ----------

    @Test
    public void testDeleteTaskSuccess() throws IOException, InterruptedException {
        Task task = new Task("To delete", "Desc",
                TaskStatus.NEW, Duration.ofMinutes(10), LocalDateTime.of(2026, 2, 12, 16, 0));
        int id = manager.addNewTask(task);

        assertEquals(1, manager.getTasks().size(), "Перед удалением должна быть одна задача");

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/" + id);
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "При успешном удалении должен вернуться статус 200");
        assertEquals(0, manager.getTasks().size(), "После удаления не должно быть задач");
    }

    @Test
    public void testDeleteTaskNotFound() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/999");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Для удаления несуществующей задачи должен вернуться статус 404");
    }

    @Test
    public void testDeleteTaskWithBadIdFormatReturnsBadRequest() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/abc");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(), "Для некорректного id при удалении должен вернуться статус 400");
    }

    // ---------- Случаи, выбрасывающие BadRequestException ----------

    @Test
    public void testDeleteWithoutIdReturnsBadRequest() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(),
                "При DELETE /tasks без id должен вернуться статус 400 (BadRequest)");
    }

    @Test
    public void testUnknownHttpMethodReturnsBadRequest() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .method("PUT", HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(),
                "При неизвестном HTTP-методе должен вернуться статус 400 (BadRequest)");
    }

    @Test
    public void testTooLongPathReturnsBadRequest() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks/1/2");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(),
                "При слишком длинном пути (больше трёх сегментов) должен вернуться статус 400 (BadRequest)");
    }

    @Test
    public void testCreateTaskWithBadJsonReturnsBadRequest() throws IOException, InterruptedException {
        String badJson = "{this is not valid json}";

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/tasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(badJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(),
                "При создании задачи с некорректным JSON должен вернуться статус 400 (BadRequest)");
    }

}