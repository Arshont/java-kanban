package ru.yandex.javacourse.schedule.http.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.sun.net.httpserver.HttpExchange;
import ru.yandex.javacourse.schedule.http.exceptions.BadRequestException;
import ru.yandex.javacourse.schedule.http.exceptions.HasInteractionsException;
import ru.yandex.javacourse.schedule.http.exceptions.NotFoundException;
import ru.yandex.javacourse.schedule.http.exceptions.SubtasksNotFoundException;
import ru.yandex.javacourse.schedule.http.handlers.serializers_deserializers.EpicDeserializer;
import ru.yandex.javacourse.schedule.http.handlers.typeAdapters.DurationTypeAdapter;
import ru.yandex.javacourse.schedule.http.handlers.typeAdapters.LocalDateTimeTypeAdapter;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Epic;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;

public class EpicsHttpHandler extends TasksHttpHandler {
    public EpicsHttpHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    protected Gson getGson() {
        return new GsonBuilder()
                .serializeNulls()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter().nullSafe())
                .registerTypeAdapter(Duration.class, new DurationTypeAdapter().nullSafe())
                .registerTypeAdapter(Epic.class, new EpicDeserializer())
                .create();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String[] pathElements = exchange.getRequestURI().getPath().split("/");
            if (pathElements.length > 4) {
                throw new ru.yandex.javacourse.schedule.http.exceptions.BadRequestException();
            }

            String method = exchange.getRequestMethod();

            switch (method) {
                case "GET":
                    if (pathElements.length == 4) {
                        if (!pathElements[3].equals("subtasks")) {
                            throw new BadRequestException();
                        }
                        handleGetEpicSubtasks(exchange, Integer.parseInt(pathElements[2]));
                    } else if (pathElements.length == 3) {
                        handleGetEpic(exchange, Integer.parseInt(pathElements[2]));
                    } else {
                        handleGetEpics(exchange);
                    }
                    break;
                case "POST":
                    if (pathElements.length == 3) {
                        handleUpdateEpic(exchange, Integer.parseInt(pathElements[2]));
                    } else if (pathElements.length == 2) {
                        handleCreateEpic(exchange);
                    } else {
                        throw new BadRequestException();
                    }
                    break;
                case "DELETE":
                    if (pathElements.length == 3) {
                        handleDeleteEpic(exchange, Integer.parseInt(pathElements[2]));
                    } else {
                        throw new BadRequestException();
                    }
                    break;
                default:
                    throw new ru.yandex.javacourse.schedule.http.exceptions.BadRequestException();
            }
        } catch (NumberFormatException | BadRequestException | JsonParseException | DateTimeParseException e) {
            sendBadRequest(exchange);
        } catch (HasInteractionsException e) {
            sendHasInteractions(exchange);
        } catch (NotFoundException e) {
            sendNotFound(exchange);
        } catch (SubtasksNotFoundException e) {
            sendSubtasksNotFound(exchange);
        }
    }

    private void handleGetEpic(HttpExchange exchange, int epicId) throws NotFoundException, IOException {
        Optional<Epic> taskOpt = manager.getEpic(epicId);
        if (taskOpt.isPresent()) {
            sendText(exchange, gson.toJson(taskOpt.get()));
        } else {
            throw new NotFoundException();
        }
    }

    private void handleGetEpics(HttpExchange exchange) throws IOException {
        sendText(exchange, gson.toJson(manager.getEpics()));
    }

    private void handleUpdateEpic(HttpExchange exchange, int epicId) throws HasInteractionsException, NotFoundException, IOException, SubtasksNotFoundException {
        Epic epicForUpdate = gson.fromJson(new String(exchange.getRequestBody().readAllBytes()), Epic.class);
        // Исправление несоответствия epicId в теле запроса с epicId в адресе
        epicForUpdate.setId(epicId);
        manager.updateEpic(epicForUpdate);
        sendCreated(exchange, epicId);
    }

    private void handleCreateEpic(HttpExchange exchange) throws IOException, HasInteractionsException {
        int epicId = manager.addNewEpic(gson.fromJson(new String(exchange.getRequestBody().readAllBytes()), Epic.class));
        if (epicId != 0) {
            sendCreated(exchange, epicId);
        } else {
            throw new HasInteractionsException();
        }
    }

    private void handleDeleteEpic(HttpExchange exchange, int epicId) throws NotFoundException, IOException {
        manager.deleteEpic(epicId);
        sendOK(exchange);
    }

    private void handleGetEpicSubtasks(HttpExchange exchange, int epicId) throws NotFoundException, IOException {
        sendText(exchange, gson.toJson(manager.getEpicSubtasks(epicId)));
    }

    private void sendSubtasksNotFound(HttpExchange h) throws IOException {
        byte[] resp = "error_msg:\"не найдены подзадачи эпика\"".getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(404, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }
}
