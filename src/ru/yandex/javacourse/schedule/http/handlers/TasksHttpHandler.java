package ru.yandex.javacourse.schedule.http.handlers;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import ru.yandex.javacourse.schedule.http.exceptions.BadRequestException;
import ru.yandex.javacourse.schedule.http.exceptions.HasInteractionsException;
import ru.yandex.javacourse.schedule.http.exceptions.NotFoundException;
import ru.yandex.javacourse.schedule.http.handlers.typeAdapters.DurationTypeAdapter;
import ru.yandex.javacourse.schedule.http.handlers.typeAdapters.LocalDateTimeTypeAdapter;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Task;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Optional;

public class TasksHttpHandler extends BaseHttpHandler {

    public TasksHttpHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    protected Gson getGson() {
        return new GsonBuilder()
                .serializeNulls()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeTypeAdapter().nullSafe())
                .registerTypeAdapter(Duration.class, new DurationTypeAdapter())
                .create();
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
                        handleGetTask(exchange, Integer.parseInt(pathElements[2]));
                    } else {
                        handleGetTasks(exchange);
                    }
                    break;
                case "POST":
                    if (pathElements.length == 3) {
                        handleUpdateTask(exchange, Integer.parseInt(pathElements[2]));
                    } else {
                        handleCreateTask(exchange);
                    }
                    break;
                case "DELETE":
                    if (pathElements.length == 3) {
                        handleDeleteTask(exchange, Integer.parseInt(pathElements[2]));
                    } else {
                        throw new BadRequestException();
                    }
                    break;
                default:
                    throw new BadRequestException();
            }
        } catch (NumberFormatException | BadRequestException | JsonParseException | DateTimeParseException e) {
            sendBadRequest(exchange);
        } catch (HasInteractionsException e) {
            sendHasInteractions(exchange);
        } catch (NotFoundException e) {
            sendNotFound(exchange);
        }
    }

    private void handleGetTask(HttpExchange exchange, int taskId) throws IOException, NotFoundException {
        Optional<Task> taskOpt = manager.getTask(taskId);
        if (taskOpt.isPresent()) {
            sendText(exchange, gson.toJson(taskOpt.get()));
        } else {
            throw new NotFoundException();
        }
    }

    private void handleGetTasks(HttpExchange exchange) throws IOException {
        sendText(exchange, gson.toJson(manager.getTasks()));
    }

    private void handleCreateTask(HttpExchange exchange) throws IOException, HasInteractionsException {
        int taskId = manager.addNewTask(gson.fromJson(new String(exchange.getRequestBody().readAllBytes()), Task.class));
        if (taskId != 0) {
            sendCreated(exchange, taskId);
        } else {
            throw new HasInteractionsException();
        }
    }

    private void handleUpdateTask(HttpExchange exchange, int taskId)
            throws IOException, NotFoundException, HasInteractionsException {
            Task taskForUpdate = gson.fromJson(new String(exchange.getRequestBody().readAllBytes()), Task.class);
            // Исправление несоответствия taskId в теле запроса с taskId в адресе
            taskForUpdate.setId(taskId);
            manager.updateTask(taskForUpdate);
            sendCreated(exchange, taskId);
    }

    private void handleDeleteTask(HttpExchange exchange, int taskId) throws IOException, NotFoundException {
        manager.deleteTask(taskId);
        sendOK(exchange);
    }
}

