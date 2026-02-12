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
import ru.yandex.javacourse.schedule.tasks.Subtask;
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

public class HttpTaskManagerSubtasksTest {

    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = new GsonBuilder()
            .serializeNulls()
            .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter())
            .registerTypeAdapter(Epic.class, new EpicDeserializer())
            .create();

    public HttpTaskManagerSubtasksTest() throws IOException {
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

    // ---------- POST /subtasks (создание подзадачи) ----------

    @Test
    public void testAddSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Desc");
        int epicId = manager.addNewEpic(epic);

        Subtask subtask = new Subtask("Sub 1", "Sub desc", TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.of(2026, 2, 12, 8, 0), epicId);
        String subtaskJson = gson.toJson(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(subtaskJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertEquals(2, JsonParser.parseString(response.body()).getAsJsonObject().get("task_id").getAsInt());

        assertEquals(1, manager.getSubtasks().size(), "Должна быть добавлена одна подзадача");
        assertEquals("Sub 1", manager.getSubtasks().get(0).getName(), "Имя подзадачи должно совпадать");
    }

    @Test
    public void testFailedAddSubtaskHasInteractions() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Desc");
        int epicId = manager.addNewEpic(epic);

        Subtask sub1 = new Subtask("Sub 1", "D1", TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.of(2026, 2, 12, 8, 15), epicId);
        manager.addNewSubtask(sub1);

        Subtask sub2 = new Subtask("Sub 2", "D2", TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.of(2026, 2, 12, 8, 0), epicId);
        String sub2Json = gson.toJson(sub2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(sub2Json)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode(),
                "При создании подзадачи с пересечением должен вернуться статус 406 (HasInteractions)");
        assertEquals(1, manager.getSubtasks().size(), "Должна остаться одна подзадача");
        assertSame(sub1, manager.getSubtasks().get(0), "Исходная подзадача не должна измениться");
    }

    @Test
    public void testCreateSubtaskWithBadJsonReturnsBadRequest() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Desc");
        manager.addNewEpic(epic);

        String badJson = "{this is not valid json}";

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(badJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(),
                "При создании подзадачи с некорректным JSON должен вернуться статус 400 (BadRequest)");
    }

    // ---------- GET /subtasks ----------

    @Test
    public void testGetSubtasksReturnsAllSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Desc");
        int epicId = manager.addNewEpic(epic);

        Subtask sub1 = new Subtask("Sub 1", "D1", TaskStatus.NEW,
                Duration.ofMinutes(10), LocalDateTime.of(2026, 2, 12, 9, 0), epicId);
        Subtask sub2 = new Subtask("Sub 2", "D2", TaskStatus.NEW,
                Duration.ofMinutes(20), LocalDateTime.of(2026, 2, 12, 10, 0), epicId);
        manager.addNewSubtask(sub1);
        manager.addNewSubtask(sub2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Должен вернуться статус 200");

        Subtask[] subtasksArray = gson.fromJson(response.body(), Subtask[].class);
        List<Subtask> subtasks = Arrays.asList(subtasksArray);
        assertNotNull(subtasks, "Список подзадач не должен быть null");
        assertEquals(2, subtasks.size(), "Должно вернуться две подзадачи");
    }

    // ---------- GET /subtasks/{id} ----------

    @Test
    public void testGetSubtaskByIdReturnsSubtask() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Desc");
        int epicId = manager.addNewEpic(epic);

        Subtask subtask = new Subtask("Sub", "Desc", TaskStatus.NEW,
                Duration.ofMinutes(15), LocalDateTime.of(2026, 2, 12, 11, 0), epicId);
        int id = manager.addNewSubtask(subtask);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/" + id);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Должен вернуться статус 200");

        Subtask subFromResponse = gson.fromJson(response.body(), Subtask.class);
        assertNotNull(subFromResponse, "Подзадача не должна быть null");
        assertEquals(id, subFromResponse.getId(), "id подзадачи должен совпадать");
        assertEquals(subtask.getName(), subFromResponse.getName(), "Имя подзадачи должно совпадать");
    }

    @Test
    public void testGetSubtaskByIdNotFound() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/999");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Для несуществующей подзадачи должен вернуться статус 404");
    }

    @Test
    public void testGetSubtaskByIdWithBadIdFormatReturnsBadRequest() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/abc");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(), "Для некорректного id подзадачи должен вернуться статус 400");
    }

    // ---------- POST /subtasks/{id} (обновление подзадачи) ----------

    @Test
    public void testUpdateSubtaskSuccess() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Desc");
        int epicId = manager.addNewEpic(epic);

        Subtask original = new Subtask("Old", "Old desc", TaskStatus.NEW,
                Duration.ofMinutes(15), LocalDateTime.of(2026, 2, 12, 12, 0), epicId);
        int id = manager.addNewSubtask(original);

        Subtask updated = new Subtask("New", "New desc", TaskStatus.IN_PROGRESS,
                Duration.ofMinutes(30), LocalDateTime.of(2026, 2, 12, 13, 0), epicId);
        String updatedJson = gson.toJson(updated);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/" + id);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(updatedJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "При успешном обновлении подзадачи должен вернуться статус 201");
        assertEquals(id, JsonParser.parseString(response.body()).getAsJsonObject().get("task_id").getAsInt());

        Subtask subFromManager = manager.getSubtask(id).orElse(null);
        assertNotNull(subFromManager, "Подзадача должна существовать после обновления");
        assertEquals("New", subFromManager.getName(), "Имя подзадачи должно обновиться");
        assertEquals(TaskStatus.IN_PROGRESS, subFromManager.getStatus(), "Статус подзадачи должен обновиться");
    }

    @Test
    public void testUpdateSubtaskNotFound() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Desc");
        int epicId = manager.addNewEpic(epic);

        Subtask updated = new Subtask("Name", "Desc", TaskStatus.NEW,
                Duration.ofMinutes(10), LocalDateTime.of(2026, 2, 12, 14, 0), epicId);
        String updatedJson = gson.toJson(updated);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/999");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(updatedJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Для обновления несуществующей подзадачи должен вернуться статус 404");
    }

    @Test
    public void testUpdateSubtaskWithHasInteractionsReturnsNotAcceptable() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Desc");
        int epicId = manager.addNewEpic(epic);

        // первая подзадача, уже существующая в менеджере
        Subtask existing = new Subtask("Existing", "D1", TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.of(2026, 2, 12, 8, 15), epicId);
        manager.addNewSubtask(existing);

        // отдельная подзадача, которую будем обновлять так, чтобы она пересекалась с existing
        Subtask toUpdate = new Subtask("To update", "D2", TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.of(2026, 2, 12, 9, 0), epicId);
        int id = manager.addNewSubtask(toUpdate);

        Subtask updated = new Subtask("To update", "D2", TaskStatus.NEW,
                Duration.ofMinutes(30), LocalDateTime.of(2026, 2, 12, 8, 0), epicId);
        String updatedJson = gson.toJson(updated);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/" + id);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(updatedJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode(),
                "При обновлении подзадачи с пересечением должен вернуться статус 406 (HasInteractions)");
    }

    @Test
    public void testUpdateSubtaskWithEpicNotFoundReturnsNotFound() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Desc");
        int epicId = manager.addNewEpic(epic);

        Subtask original = new Subtask("Old", "Old desc", TaskStatus.NEW,
                Duration.ofMinutes(15), LocalDateTime.of(2026, 2, 12, 15, 0), epicId);
        int id = manager.addNewSubtask(original);

        // Меняем epicId на несуществующий
        Subtask updated = new Subtask("Old", "Old desc", TaskStatus.NEW,
                Duration.ofMinutes(15), LocalDateTime.of(2026, 2, 12, 15, 0), 999);
        updated.setId(id);
        String updatedJson = gson.toJson(updated);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/" + id);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(updatedJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(),
                "При обновлении подзадачи с несуществующим эпиком должен вернуться статус 404 (EpicNotFound)");
    }

    @Test
    public void testUpdateSubtaskWithBadJsonReturnsBadRequest() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Desc");
        int epicId = manager.addNewEpic(epic);

        Subtask original = new Subtask("Old", "Old desc", TaskStatus.NEW,
                Duration.ofMinutes(15), LocalDateTime.of(2026, 2, 12, 15, 0), epicId);
        int id = manager.addNewSubtask(original);

        String badJson = "{this is not valid json}";

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/" + id);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(badJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(), "При некорректном JSON должен вернуться статус 400");
    }

    // ---------- DELETE /subtasks/{id} ----------

    @Test
    public void testDeleteSubtaskSuccess() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Desc");
        int epicId = manager.addNewEpic(epic);

        Subtask subtask = new Subtask("To delete", "Desc", TaskStatus.NEW,
                Duration.ofMinutes(10), LocalDateTime.of(2026, 2, 12, 16, 0), epicId);
        int id = manager.addNewSubtask(subtask);

        assertEquals(1, manager.getSubtasks().size(), "Перед удалением должна быть одна подзадача");

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/" + id);
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "При успешном удалении подзадачи должен вернуться статус 200");
        assertEquals(0, manager.getSubtasks().size(), "После удаления не должно быть подзадач");
    }

    @Test
    public void testDeleteSubtaskNotFound() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/999");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Для удаления несуществующей подзадачи должен вернуться статус 404");
    }

    @Test
    public void testDeleteSubtaskWithBadIdFormatReturnsBadRequest() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/abc");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(),
                "Для некорректного id при удалении подзадачи должен вернуться статус 400");
    }

    // ---------- Случаи, выбрасывающие BadRequestException для /subtasks ----------

    @Test
    public void testDeleteSubtaskWithoutIdReturnsBadRequest() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(),
                "При DELETE /subtasks без id должен вернуться статус 400 (BadRequest)");
    }

    @Test
    public void testUnknownHttpMethodForSubtasksReturnsBadRequest() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .method("PUT", HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(),
                "При неизвестном HTTP-методе для /subtasks должен вернуться статус 400 (BadRequest)");
    }

    @Test
    public void testTooLongPathForSubtasksReturnsBadRequest() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/subtasks/1/2");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(),
                "При слишком длинном пути для /subtasks должен вернуться статус 400 (BadRequest)");
    }
}

