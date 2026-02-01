package ru.yandex.javacourse.schedule.http.handlers;

import com.sun.net.httpserver.HttpExchange;
import ru.yandex.javacourse.schedule.manager.TaskManager;

import java.io.IOException;

public class HistoryHttpHandler extends BaseHttpHandler {
    public HistoryHttpHandler(TaskManager manager) {
        super(manager);
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {

    }
}
