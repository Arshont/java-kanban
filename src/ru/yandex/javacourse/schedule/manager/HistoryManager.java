package ru.yandex.javacourse.schedule.manager;

import ru.yandex.javacourse.schedule.tasks.Task;

import java.util.List;

/**
 * History manager.
 *
 * @author Vladimir Ivanov (ivanov.vladimir.l@gmail.com)
 */
public interface HistoryManager {
    List<Task> getHistory();

    void add(Task task);

    void remove(int id);
}
