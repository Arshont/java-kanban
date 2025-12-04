package ru.yandex.javacourse.schedule;

import static ru.yandex.javacourse.schedule.tasks.TaskStatus.NEW;

import ru.yandex.javacourse.schedule.manager.Managers;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Random;

public class Main {
    public static void main(String[] args) {

        TaskManager manager = Managers.getFileManager("Test.txt");

        // Создание
        Task task1 = new Task("Task #1", "Task1 description", NEW, Duration.ofMinutes(23), LocalDateTime.of(2025, 12, 4, 12,33));
        Task task2 = new Task("Task #2", "Task2 description", NEW, Duration.ofMinutes(65), LocalDateTime.of(2025, 12, 15, 12,33));
        manager.addNewTask(task1);
        manager.addNewTask(task2);

        Epic epic1 = new Epic("Epic #1", "Epic1 description");
        Epic epic2 = new Epic("Epic #2", "Epic2 description");
        final int epicId1 = manager.addNewEpic(epic1);
        manager.addNewEpic(epic2);

        Subtask subtask1 = new Subtask("Subtask #1-1", "Subtask1 description", NEW, epicId1, Duration.ofMinutes(15), LocalDateTime.of(2025, 12, 6, 12,33));
        Subtask subtask2 = new Subtask("Subtask #2-1", "Subtask1 description", NEW, epicId1, Duration.ofMinutes(24), LocalDateTime.of(2025, 12, 7, 12,33));
        Subtask subtask3 = new Subtask("Subtask #3-1", "Subtask1 description", NEW, epicId1, Duration.ofMinutes(45));
        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);
        manager.addNewSubtask(subtask3);

        printAllTasks(manager);
        printHistory(manager);

        TaskManager manager1 = Managers.getAndRestoreFileManager("Test2.txt", "Test.txt");

        System.out.println(manager.getPrioritizedTasks());
        System.out.println(epic1);
        System.out.println(epic2);

//        printHistory(manager);
//        for (Task task : manager.getTasks()) {
//            manager.getTask(task.getId());
//        }
//
//        for (Epic epic : manager.getEpics()) {
//            manager.getEpic(epic.getId());
//        }
//
//        for (Subtask subtask : manager.getSubtasks()) {
//            manager.getSubtask(subtask.getId());
//        }
//        printHistory(manager);
//        manager.getTask(1);
//        printHistory(manager);
//
//        Random rand = new Random();
//        for (int i = 0; i < 5; i++) {
//            int id = rand.nextInt(2) + 1;
//            manager.getTask(id);
//            System.out.println("Запрошена задача с id = " + id);
//            printHistory(manager);
//        }
//        for (int i = 0; i < 3; i++) {
//            int id = rand.nextInt(2) + 3;
//            manager.getEpic(id);
//            System.out.println("Запрошен эпик с id = " + id);
//            printHistory(manager);
//        }
//        for (int i = 0; i < 5; i++) {
//            int id = rand.nextInt(3) + 5;
//            manager.getSubtask(id);
//            System.out.println("Запрошена подзадача с id = " + id);
//            printHistory(manager);
//        }
//        manager.deleteTask(2);
//        printHistory(manager);
//        manager.deleteEpic(3);
//        printHistory(manager);
    }

    private static void printHistory(TaskManager manager) {
        System.out.println("История просмотров:");
        for (Task task : manager.getHistory()) {
            System.out.println(task);
        }
    }

    private static void printAllTasks(TaskManager manager) {
        System.out.println("Задачи:");
        for (Task task : manager.getTasks()) {
            System.out.println(task);
        }
        System.out.println("Эпики:");
        for (Task epic : manager.getEpics()) {
            System.out.println(epic);
//			System.out.println("--> Подзадачи эпика:");
            for (Task task : manager.getEpicSubtasks(epic.getId())) {
                System.out.println("--> " + task);
            }
        }
        System.out.println("Подзадачи:");
        for (Task subtask : manager.getSubtasks()) {
            System.out.println(subtask);
        }

        System.out.println("История:");
        for (Task task : manager.getHistory()) {
            System.out.println(task);
        }
    }
}
