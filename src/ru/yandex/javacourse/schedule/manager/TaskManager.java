package ru.yandex.javacourse.schedule.manager;

import ru.yandex.javacourse.schedule.http.exceptions.EpicNotFoundException;
import ru.yandex.javacourse.schedule.http.exceptions.HasInteractionsException;
import ru.yandex.javacourse.schedule.http.exceptions.NotFoundException;
import ru.yandex.javacourse.schedule.http.exceptions.SubtasksNotFoundException;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;

import java.util.List;
import java.util.Optional;

/**
 * Task manager.
 *
 * @author Vladimir Ivanov (ivanov.vladimir.l@gmail.com)
 */
public interface TaskManager {
    List<Task> getTasks();

    List<Subtask> getSubtasks();

    List<Epic> getEpics();

    List<Subtask> getEpicSubtasks(int epicId) throws NotFoundException;

    Optional<Task> getTask(int id);

    Optional<Subtask> getSubtask(int id);

    Optional<Epic> getEpic(int id);

    int addNewTask(Task task);

    int addNewEpic(Epic epic);

    int addNewSubtask(Subtask subtask);

    void updateTask(Task task) throws NotFoundException, HasInteractionsException;

    void updateEpic(Epic epic) throws NotFoundException, HasInteractionsException, SubtasksNotFoundException;

    void updateSubtask(Subtask subtask) throws NotFoundException, HasInteractionsException, EpicNotFoundException;

    void deleteTask(int id) throws NotFoundException;

    void deleteEpic(int id) throws NotFoundException;

    void deleteSubtask(int id) throws NotFoundException;

    void deleteTasks();

    void deleteSubtasks();

    void deleteEpics();

    List<Task> getHistory();

    List<Task> getPrioritizedTasks();

//    boolean isTaskAlreadyExists(Task task);
//
//    boolean isTaskAlreadyExists(Task task);
//
//    boolean isTaskAlreadyExists(Task task);
}
