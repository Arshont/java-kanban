package ru.yandex.javacourse.schedule.http.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import ru.yandex.javacourse.schedule.http.exceptions.BadRequestException;
import ru.yandex.javacourse.schedule.http.handlers.typeAdapters.DurationTypeAdapter;
import ru.yandex.javacourse.schedule.http.handlers.typeAdapters.LocalDateTimeTypeAdapter;
import ru.yandex.javacourse.schedule.manager.TaskManager;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

public class PrioritizedHttpHandler extends BaseHttpHandler {
    public PrioritizedHttpHandler(TaskManager manager) {
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
                throw new ru.yandex.javacourse.schedule.http.exceptions.BadRequestException();
            }

            String method = exchange.getRequestMethod();
            if (method.equals("GET")) {
                sendText(exchange, gson.toJson(manager.getPrioritizedTasks()));
            } else {
                throw new ru.yandex.javacourse.schedule.http.exceptions.BadRequestException();
            }
        } catch (BadRequestException e) {
            sendBadRequest(exchange);
        }
    }
}
