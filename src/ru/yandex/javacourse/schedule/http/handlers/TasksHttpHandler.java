package ru.yandex.javacourse.schedule.http.handlers;

import com.sun.net.httpserver.HttpExchange;
import ru.yandex.javacourse.schedule.manager.TaskManager;

import java.io.IOException;

public class TasksHttpHandler extends BaseHttpHandler {

    public TasksHttpHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String method = exchange.getRequestMethod();
        try {
            switch (method) {
                case "GET":
                    String[] pathElements = exchange.getRequestURI().getPath().split("/");
                    if (pathElements.length == 3) {
                        int taskId = Integer.parseInt(exchange.getRequestURI().getPath().split("/")[2]);
                    }
                    // Ветка чистого GET-запроса
                    break;
                case "POST":
                    break;
                case "DELETE":
                    break;
                default:

            }
        } catch (NumberFormatException e) {
            sendNotFound(exchange);
        }

    }
}
