package ru.yandex.javacourse.schedule.manager;

public class ManagerSaveException extends RuntimeException {
    ManagerSaveException(final String message) {
        super(message);
    }
}
