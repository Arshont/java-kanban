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

public class HttpTaskManagerEpicsTest {

    TaskManager manager = new InMemoryTaskManager();
    HttpTaskServer taskServer = new HttpTaskServer(manager);
    Gson gson = new GsonBuilder()
            .serializeNulls()
            .registerTypeAdapter(Duration.class, new DurationTypeAdapter().nullSafe())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter().nullSafe())
            .registerTypeAdapter(Epic.class, new EpicDeserializer())
            .create();

    public HttpTaskManagerEpicsTest() throws IOException {
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

    // ---------- POST /epics (создание эпика) ----------

    @Test
    public void testAddEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "Epic description");
        String epicJson = gson.toJson(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode());
        assertEquals(1, JsonParser.parseString(response.body()).getAsJsonObject().get("task_id").getAsInt());

        assertEquals(1, manager.getEpics().size(), "Должен быть добавлен один эпик");
        assertEquals("Epic 1", manager.getEpics().get(0).getName(), "Имя эпика должно совпадать");
    }

    @Test
    public void testCreateEpicWithBadJsonReturnsBadRequest() throws IOException, InterruptedException {
        String badJson = "{this is not valid json}";

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(badJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(),
                "При создании эпика с некорректным JSON должен вернуться статус 400 (BadRequest)");
    }

    @Test
    public void testFailedAddEpicHasInteractions() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic 1", "Epic description");
        // сначала добавляем эпик в менеджер, чтобы у него появился id
        manager.addNewEpic(epic);

        // затем пытаемся создать такой же эпик с тем же id через HTTP
        String epicJson = gson.toJson(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(epicJson)).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode(),
                "При создании эпика, уже существующего в менеджере, должен вернуться статус 406 (HasInteractions)");
        assertEquals(1, manager.getEpics().size(), "Должен остаться один эпик");
        assertSame(epic, manager.getEpics().get(0), "Исходный эпик не должен измениться");
    }

    // ---------- GET /epics ----------

    @Test
    public void testGetEpicsReturnsAllEpics() throws IOException, InterruptedException {
        Epic epic1 = new Epic("Epic 1", "Desc 1");
        Epic epic2 = new Epic("Epic 2", "Desc 2");

        manager.addNewEpic(epic1);
        manager.addNewEpic(epic2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Должен вернуться статус 200");

        Epic[] epicsArray = gson.fromJson(response.body(), Epic[].class);
        List<Epic> epicsFromResponse = Arrays.asList(epicsArray);
        assertNotNull(epicsFromResponse, "Список эпиков не должен быть null");
        assertEquals(2, epicsFromResponse.size(), "Должно вернуться два эпика");
    }

    // ---------- GET /epics/{id} ----------

    @Test
    public void testGetEpicByIdReturnsEpic() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Desc");
        int id = manager.addNewEpic(epic);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + id);
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Должен вернуться статус 200");

        Epic epicFromResponse = gson.fromJson(response.body(), Epic.class);
        assertNotNull(epicFromResponse, "Эпик не должен быть null");
        assertEquals(id, epicFromResponse.getId(), "id эпика должен совпадать");
        assertEquals(epic.getName(), epicFromResponse.getName(), "Имя эпика должно совпадать");
    }

    @Test
    public void testGetEpicByIdNotFound() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/999");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Для несуществующего эпика должен вернуться статус 404");
    }

    @Test
    public void testGetEpicByIdWithBadIdFormatReturnsBadRequest() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/abc");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(), "Для некорректного id эпика должен вернуться статус 400");
    }

    // ---------- GET /epics/{id}/subtasks ----------

    @Test
    public void testGetEpicSubtasksReturnsSubtasks() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Desc");
        int epicId = manager.addNewEpic(epic);

        Subtask sub1 = new Subtask("Sub1", "D1", TaskStatus.NEW,
                Duration.ofMinutes(10), LocalDateTime.of(2026, 2, 12, 8, 0), epicId);
        Subtask sub2 = new Subtask("Sub2", "D2", TaskStatus.NEW,
                Duration.ofMinutes(20), LocalDateTime.of(2026, 2, 12, 9, 0), epicId);
        manager.addNewSubtask(sub1);
        manager.addNewSubtask(sub2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + epicId + "/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "Должен вернуться статус 200");

        Subtask[] subtasksArray = gson.fromJson(response.body(), Subtask[].class);
        List<Subtask> subtasks = Arrays.asList(subtasksArray);
        assertNotNull(subtasks, "Подзадачи не должны быть null");
        assertEquals(2, subtasks.size(), "Должно вернуться две подзадачи эпика");
    }

    @Test
    public void testGetEpicSubtasksEpicNotFound() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/999/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(),
                "Для несуществующего эпика при запросе его подзадач должен вернуться статус 404");
    }

    @Test
    public void testGetEpicSubtasksWithBadIdFormatReturnsBadRequest() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/abc/subtasks");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(),
                "Для некорректного id эпика при запросе подзадач должен вернуться статус 400");
    }

    @Test
    public void testGetEpicSubtasksWithWrongTailReturnsBadRequest() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/1/wrong");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(),
                "При некорректном окончании пути должен вернуться статус 400 (BadRequest)");
    }

    // ---------- POST /epics/{id} (обновление эпика) ----------

    @Test
    public void testUpdateEpicSuccess() throws IOException, InterruptedException {
        Epic epic = new Epic("Old epic", "Old desc");
        int epicId = manager.addNewEpic(epic);

        Epic updated = new Epic("New epic", "New desc");
        updated.setId(epicId);
        String updatedJson = gson.toJson(updated);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + epicId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(updatedJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, response.statusCode(), "При успешном обновлении эпика должен вернуться статус 201");
        assertEquals(epicId, JsonParser.parseString(response.body()).getAsJsonObject().get("task_id").getAsInt());

        Epic epicFromManager = manager.getEpic(epicId).orElse(null);
        assertNotNull(epicFromManager, "Эпик должен существовать после обновления");
        assertEquals("New epic", epicFromManager.getName(), "Имя эпика должно обновиться");
    }

    @Test
    public void testUpdateEpicNotFound() throws IOException, InterruptedException {
        Epic updated = new Epic("Name", "Desc");
        updated.setId(999);
        String updatedJson = gson.toJson(updated);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/999");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(updatedJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Для обновления несуществующего эпика должен вернуться статус 404");
    }

    @Test
    public void testUpdateEpicWithMissingSubtasksReturnsSubtasksNotFound() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Desc");
        int epicId = manager.addNewEpic(epic);

        Epic updated = new Epic("Epic", "Desc");
        updated.setId(epicId);
        updated.subtaskIds.add(999); // заведомо несуществующая подзадача
        String updatedJson = gson.toJson(updated);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + epicId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(updatedJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(),
                "Если в эпике указаны несуществующие подзадачи, должен вернуться статус 404 (SubtasksNotFound)");
    }

    @Test
    public void testUpdateEpicWithHasInteractionsReturnsNotAcceptable() throws IOException, InterruptedException {
        Epic epic1 = new Epic("Epic 1", "D1");
        Epic epic2 = new Epic("Epic 2", "D2");
        int epic1Id = manager.addNewEpic(epic1);
        int epic2Id = manager.addNewEpic(epic2);

        Subtask subtask = new Subtask("Sub", "D", TaskStatus.NEW,
                Duration.ofMinutes(10), LocalDateTime.of(2026, 2, 12, 8, 0), epic1Id);
        int subId = manager.addNewSubtask(subtask);

        Epic updatedEpic2 = new Epic("Epic 2", "D2");
        updatedEpic2.setId(epic2Id);
        updatedEpic2.subtaskIds.add(subId); // подзадача принадлежит другому эпику
        String updatedJson = gson.toJson(updatedEpic2);

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + epic2Id);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(updatedJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(406, response.statusCode(),
                "Если в эпике указаны подзадачи другого эпика, должен вернуться статус 406 (HasInteractions)");
    }

    @Test
    public void testUpdateEpicWithBadJsonReturnsBadRequest() throws IOException, InterruptedException {
        Epic epic = new Epic("Epic", "Desc");
        int epicId = manager.addNewEpic(epic);

        String badJson = "{this is not valid json}";

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + epicId);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .POST(HttpRequest.BodyPublishers.ofString(badJson))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(), "При некорректном JSON должен вернуться статус 400");
    }

    // ---------- DELETE /epics/{id} ----------

    @Test
    public void testDeleteEpicSuccess() throws IOException, InterruptedException {
        Epic epic = new Epic("To delete", "Desc");
        int epicId = manager.addNewEpic(epic);

        assertEquals(1, manager.getEpics().size(), "Перед удалением должен быть один эпик");

        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/" + epicId);
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode(), "При успешном удалении эпика должен вернуться статус 200");
        assertEquals(0, manager.getEpics().size(), "После удаления не должно быть эпиков");
    }

    @Test
    public void testDeleteEpicNotFound() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/999");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(404, response.statusCode(), "Для удаления несуществующего эпика должен вернуться статус 404");
    }

    @Test
    public void testDeleteEpicWithBadIdFormatReturnsBadRequest() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/abc");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(), "Для некорректного id при удалении эпика должен вернуться статус 400");
    }

    // ---------- Случаи, выбрасывающие BadRequestException для /epics ----------

    @Test
    public void testDeleteEpicWithoutIdReturnsBadRequest() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder().uri(url).DELETE().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(),
                "При DELETE /epics без id должен вернуться статус 400 (BadRequest)");
    }

    @Test
    public void testUnknownHttpMethodForEpicsReturnsBadRequest() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics");
        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .method("PUT", HttpRequest.BodyPublishers.noBody())
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(),
                "При неизвестном HTTP-методе для /epics должен вернуться статус 400 (BadRequest)");
    }

    @Test
    public void testTooLongPathForEpicsReturnsBadRequest() throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        URI url = URI.create("http://localhost:8080/epics/1/subtasks/extra");
        HttpRequest request = HttpRequest.newBuilder().uri(url).GET().build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, response.statusCode(),
                "При слишком длинном пути для /epics должен вернуться статус 400 (BadRequest)");
    }
}

