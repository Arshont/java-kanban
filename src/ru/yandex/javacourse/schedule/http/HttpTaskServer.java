package ru.yandex.javacourse.schedule.http;

import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import ru.yandex.javacourse.schedule.http.handlers.*;
import ru.yandex.javacourse.schedule.manager.InMemoryTaskManager;
import ru.yandex.javacourse.schedule.manager.Managers;
import ru.yandex.javacourse.schedule.manager.TaskManager;

import java.io.IOException;
import java.net.InetSocketAddress;

public class HttpTaskServer {
    private static final int STOP_DELAY = 0;
    private static final int PORT = 8080;
    public final HttpServer server;
    private final TaskManager manager;

    public HttpTaskServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(PORT), 0);
        manager = Managers.getDefault();
        server.createContext("/tasks", new TasksHttpHandler(manager));
        server.createContext("/subtasks", new SubtasksHttpHandler(manager));
        server.createContext("/epics", new EpicsHttpHandler(manager));
        server.createContext("/history", new HistoryHttpHandler(manager));
        server.createContext("/prioritized", new PrioritizedHttpHandler(manager));
    }
    public void startServer() {
        server.start();
    }

    public void stopServer() {
        server.stop(STOP_DELAY);
    }

    public static void main(String[] args) {
        HttpTaskServer taskServer = null;
        try {
            taskServer = new HttpTaskServer();
            taskServer.startServer();
        } catch (IOException e) {

            throw new RuntimeException(e);
        } finally {
            assert taskServer != null;
            taskServer.stopServer();
        }
    }
}
