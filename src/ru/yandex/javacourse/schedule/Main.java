package ru.yandex.javacourse.schedule;

import static ru.yandex.javacourse.schedule.tasks.TaskStatus.NEW;

import ru.yandex.javacourse.schedule.manager.Managers;
import ru.yandex.javacourse.schedule.manager.TaskManager;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;

import java.util.Random;

public class Main {
    public static void main(String[] args) {

        TaskManager manager = Managers.getDefault();

        // Создание
        Task task1 = new Task("Task #1", "Task1 description", NEW);
        Task task2 = new Task("Task #2", "Task2 description", NEW);
        manager.addNewTask(task1);
        manager.addNewTask(task2);

        Epic epic1 = new Epic("Epic #1", "Epic1 description");
        Epic epic2 = new Epic("Epic #2", "Epic2 description");
        final int epicId1 = manager.addNewEpic(epic1);
        manager.addNewEpic(epic2);

        Subtask subtask1 = new Subtask("Subtask #1-1", "Subtask1 description", NEW, epicId1);
        Subtask subtask2 = new Subtask("Subtask #2-1", "Subtask1 description", NEW, epicId1);
        Subtask subtask3 = new Subtask("Subtask #3-1", "Subtask1 description", NEW, epicId1);
        manager.addNewSubtask(subtask1);
        manager.addNewSubtask(subtask2);
        manager.addNewSubtask(subtask3);

        printHistory(manager);
        for (Task task : manager.getTasks()) {
            manager.getTask(task.getId());
        }

        for (Epic epic : manager.getEpics()) {
            manager.getEpic(epic.getId());
        }

        for (Subtask subtask : manager.getSubtasks()) {
            manager.getSubtask(subtask.getId());
        }
        printHistory(manager);
        manager.getTask(1);
        printHistory(manager);

        Random rand = new Random();
        for (int i = 0; i < 5; i++) {
            int id = rand.nextInt(2) + 1;
            manager.getTask(id);
            System.out.println("Запрошена задача с id = " + id);
            printHistory(manager);
        }
        for (int i = 0; i < 3; i++) {
            int id = rand.nextInt(2) + 3;
            manager.getEpic(id);
            System.out.println("Запрошен эпик с id = " + id);
            printHistory(manager);
        }
        for (int i = 0; i < 5; i++) {
            int id = rand.nextInt(3) + 5;
            manager.getSubtask(id);
            System.out.println("Запрошена подзадача с id = " + id);
            printHistory(manager);
        }
        manager.deleteTask(2);
        printHistory(manager);
        manager.deleteEpic(3);
        printHistory(manager);
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
