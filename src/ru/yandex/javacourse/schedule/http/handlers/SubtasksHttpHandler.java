package ru.yandex.javacourse.schedule.http.handlers;

import com.google.gson.JsonParseException;
import com.sun.net.httpserver.HttpExchange;
import ru.yandex.javacourse.schedule.http.exceptions.*;
import ru.yandex.javacourse.schedule.http.exceptions.BadRequestException;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Subtask;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeParseException;
import java.util.Optional;

public class SubtasksHttpHandler extends TasksHttpHandler{
    public SubtasksHttpHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String[] pathElements = exchange.getRequestURI().getPath().split("/");
            if (pathElements.length > 3) {
                throw new BadRequestException();
            }

            String method = exchange.getRequestMethod();

            switch (method) {
                case "GET":
                    if (pathElements.length == 3) {
                        handleGetSubtask(exchange, Integer.parseInt(pathElements[2]));
                    } else {
                        handleGetSubtasks(exchange);
                    }
                    break;
                case "POST":
                    if (pathElements.length == 3) {
                        handleUpdateSubtask(exchange, Integer.parseInt(pathElements[2]));
                    } else {
                        handleCreateSubtask(exchange);
                    }
                    break;
                case "DELETE":
                    if (pathElements.length == 3) {
                        handleDeleteSubtask(exchange, Integer.parseInt(pathElements[2]));
                    } else {
                        throw new ru.yandex.javacourse.schedule.http.exceptions.BadRequestException();
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
        } catch (EpicNotFoundException e) {
            sendEpicNotFound(exchange);
        }
    }

    private void handleGetSubtask(HttpExchange exchange, int subtaskId) throws NotFoundException, IOException {
        Optional<Subtask> taskOpt = manager.getSubtask(subtaskId);
        if (taskOpt.isPresent()) {
            sendText(exchange, gson.toJson(taskOpt.get()));
        } else {
            throw new NotFoundException();
        }
    }

    private void handleGetSubtasks(HttpExchange exchange) throws IOException {
        sendText(exchange, gson.toJson(manager.getSubtasks()));
    }

    private void handleUpdateSubtask(HttpExchange exchange, int subtaskId) throws HasInteractionsException, NotFoundException, IOException, EpicNotFoundException {
        Subtask subtaskForUpdate = gson.fromJson(new String(exchange.getRequestBody().readAllBytes()), Subtask.class);
        // Исправление несоответствия subtaskId в теле запроса с subtaskId в адресе
        subtaskForUpdate.setId(subtaskId);
        manager.updateSubtask(subtaskForUpdate);
        sendCreated(exchange, subtaskId);
    }

    private void handleCreateSubtask(HttpExchange exchange) throws IOException, HasInteractionsException, EpicNotFoundException {
        int subtaskId = manager.addNewSubtask(gson.fromJson(new String(exchange.getRequestBody().readAllBytes()), Subtask.class));
        if (subtaskId != 0) {
            sendCreated(exchange, subtaskId);
        } else {
            throw new HasInteractionsException();
        }
    }

    private void handleDeleteSubtask(HttpExchange exchange, int subtaskId) throws NotFoundException, IOException {
        manager.deleteSubtask(subtaskId);
        sendOK(exchange);
    }

    private void sendEpicNotFound(HttpExchange h) throws IOException {
        byte[] resp = "error_msg:\"не найден эпик данной подзадачи\"".getBytes(StandardCharsets.UTF_8);
        h.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        h.sendResponseHeaders(404, resp.length);
        h.getResponseBody().write(resp);
        h.close();
    }
}
