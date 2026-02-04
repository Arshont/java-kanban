package ru.yandex.javacourse.schedule.http.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Task;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

public class TasksHttpHandler extends BaseHttpHandler {

    public TasksHttpHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    protected Gson getGson() {
        return new GsonBuilder()
                .serializeNulls()
                .registerTypeAdapter(LocalDateTime.class, new Adapters.LocalDateTimeTypeAdapter())
                .create();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String method = exchange.getRequestMethod();
            String[] pathElements = exchange.getRequestURI().getPath().split("/");
            if (pathElements.length > 3) {
                throw new BadRequestException();
            }
            switch (method) {
                case "GET":
                    if (pathElements.length == 3) {
                        System.out.println("Начата обработка запроса на GET по id");
                        int taskId = Integer.parseInt(pathElements[2]);
                        Optional<Task> taskOpt = manager.getTask(taskId);
                        if (taskOpt.isPresent()) {
                            sendText(exchange, gson.toJson(taskOpt.get()));
                        } else {
                            sendNotFound(exchange);
                        }
                    } else {
                        // Ветка чистого GET-запроса
                        System.out.println("Начата обработка запроса на GET всех задач");
                        sendText(exchange, gson.toJson(manager.getTasks()));
                    }
                    break;
                case "POST":
                    if (pathElements.length == 3) {
                        // Ветка UPDATE задачи
                        System.out.println("Начата обработка запроса на UPDATE задачи");
                        int taskId = Integer.parseInt(pathElements[2]);
                        Optional<Task> taskOpt = manager.getTask(taskId);
                        Task taskForUpdate = gson.fromJson(exchange.getResponseBody().toString(), Task.class);
                        // Протестировать ручками, что произойдёт, если кинуть некорректный JSON
                        if (taskOpt.isPresent()) {
                            manager.updateTask(taskForUpdate);
                            sendCreated(exchange, taskId);
                        } else {
                            sendNotFound(exchange);
                        }
                    } else {
                        // Ветка CREATE задачи
                        System.out.println("Начата обработка запроса на CREATE задачи");
                        int taskId = manager.addNewTask(gson.fromJson(exchange.getRequestBody().toString(), Task.class));
                        // Подумать, что произойдёт, если задача перезаписывается
                        sendCreated(exchange, taskId);
                    }
                    break;
                case "DELETE":
                    System.out.println("Начата обработка запроса на DELETE задачи");
                    if (pathElements.length == 3) {
                        int taskId = Integer.parseInt(exchange.getRequestURI().getPath().split("/")[2]);
                        Optional<Task> taskOpt = manager.getTask(taskId);
                        if (taskOpt.isPresent()) {
                            manager.deleteTask(taskId);
                        }
                        sendOK(exchange);
                    } else {
                        throw new BadRequestException();
                    }
                    break;
                default:
                    throw new BadRequestException();
            }
        } catch (NumberFormatException | BadRequestException e) {
            sendBadRequest(exchange);
        }

    }
}

