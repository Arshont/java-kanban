package ru.yandex.javacourse.schedule.manager;

import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static ru.yandex.javacourse.schedule.tasks.TaskStatus.NEW;

public class FileBackedTaskManagerTest {

    @Test
    public void checkEmptyFile() throws IOException {
        Path file1 = Files.createTempFile(null, null);
        Path file2 = Files.createTempFile(null, null);

        TaskManager manager1 = Managers.getFileManager(file1.toString());
        TaskManager manager2 = Managers.getAndRestoreFileManager(file2.toString(), file1.toString());

        assertEquals(getAllTasksInString(manager1), getAllTasksInString(manager2), "Задачи обоих менеджеров должны быть идентичны");

        assertTrue(Files.exists(file1), "Файл первого менеджера должен существовать");
        assertTrue(Files.exists(file2), "Файл второго менеджера должен существовать");

        assertEquals(-1, Files.mismatch(file1, file2), "Файлы менеджеров должны быть идентичны");
        assertEquals(0, manager1.getTasks().size() + manager1.getSubtasks().size() + manager1.getEpics().size(), "Первый менеджер не должен содержать задач");
        assertEquals(0, manager2.getTasks().size() + manager2.getSubtasks().size() + manager2.getEpics().size(), "Второй менеджер не должен содержать задач");
    }

    @Test
    public void checkAddingTasksWithPredefinedId() throws IOException {
        Path file = Files.createTempFile(null, null);
        TaskManager manager = Managers.getFileManager(file.toString());

        Task task = new Task(42, "Task", "Task description", TaskStatus.NEW);
        Epic epic = new Epic(43, "Epic", "Epic description");
        Subtask subtask = new Subtask(44, "Subtask", "Subtask description", TaskStatus.NEW, 43);

        manager.addNewTask(task);
        manager.addNewEpic(epic);
        manager.addNewSubtask(subtask);

        assertEquals(1, manager.getTasks().size(), "В менеджер должна быть добавлена одна задача");
        assertEquals(1, manager.getEpics().size(), "В менеджер должна быть добавлена один эпик");
        assertEquals(1, manager.getSubtasks().size(), "В менеджер должна быть добавлена одна подзадача");

        assertEquals(42, manager.getTask(42).getId(), "ID задачи не должен быть измененён");
        assertEquals(43, manager.getEpic(43).getId(), "ID эпика не должен быть измененён");
        assertEquals(44, manager.getSubtask(44).getId(), "ID подзадачи не должен быть измененён");
    }

    @Test
    public void checkSaveAndRestore() throws IOException {
        Path file1 = Files.createTempFile(null, null);
        Path file2 = Files.createTempFile(null, null);

        TaskManager manager1 = Managers.getFileManager(file1.toString());


        Task task1 = new Task("Task #1", "Task1 description", NEW);
        Task task2 = new Task("Task #2", "Task2 description", NEW);
        manager1.addNewTask(task1);
        manager1.addNewTask(task2);

        Epic epic1 = new Epic("Epic #1", "Epic1 description");
        Epic epic2 = new Epic("Epic #2", "Epic2 description");
        manager1.addNewEpic(epic1);
        manager1.addNewEpic(epic2);

        Subtask subtask1 = new Subtask("Subtask #1-1", "Subtask1 description", NEW, epic1.getId());
        Subtask subtask2 = new Subtask("Subtask #2-1", "Subtask1 description", NEW, epic1.getId());
        Subtask subtask3 = new Subtask("Subtask #3-1", "Subtask1 description", NEW, epic2.getId());
        manager1.addNewSubtask(subtask1);
        manager1.addNewSubtask(subtask2);
        manager1.addNewSubtask(subtask3);

        TaskManager manager2 = Managers.getAndRestoreFileManager(file2.toString(), file1.toString());

        assertEquals(getAllTasksInString(manager1), getAllTasksInString(manager2), "Задачи обоих менеджеров должны быть идентичны");

        assertTrue(Files.exists(file1), "Файл первого менеджера должен существовать");
        assertTrue(Files.exists(file2), "Файл второго менеджера должен существовать");

        assertEquals(-1, Files.mismatch(file1, file2), "Файлы менеджеров должны быть идентичны");
        assertEquals(7, manager1.getTasks().size() + manager1.getSubtasks().size() + manager1.getEpics().size(), "Первый менеджер должен содержать 7 задач");
        assertEquals(7, manager2.getTasks().size() + manager2.getSubtasks().size() + manager2.getEpics().size(), "Второй менеджер должен содержать 7 задач");
    }

    private String getAllTasksInString(TaskManager manager) {
        StringBuilder builder = new StringBuilder();
        for (Task task : manager.getTasks()) {
            builder.append(task.toStringForCSV());
        }
        for (Epic epic : manager.getEpics()) {
            builder.append(epic.toStringForCSV());
        }
        for (Subtask subtask : manager.getSubtasks()) {
            builder.append(subtask.toStringForCSV());
        }
        return builder.toString();
    }
}
