package ru.yandex.javacourse.schedule.manager;

import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.StringTokenizer;

public class InFileTaskManager extends InMemoryTaskManager {
    Path file;

    public InFileTaskManager(String filename) {
//        file = Paths.get(filename);
    }

    public InFileTaskManager(String filename, String restoreFilename) {
//        file = Paths.get(filename);
//        restoreTasksFromFile(restoreFilename);
    }

//    public void restoreTasksFromFile(String filename) {
//        Path restorationFile = Paths.get(filename);
//
//        save();
//    }
//
//    private void save(){
//
//    }

    private void restoreTaskFromString(String taskString) {
        StringTokenizer tokenizer = new StringTokenizer(taskString, ",");
        int id = Integer.parseInt(tokenizer.nextToken());
        String type = tokenizer.nextToken();
        String name = tokenizer.nextToken();
        TaskStatus status = TaskStatus.valueOf(tokenizer.nextToken());
        String description = tokenizer.nextToken();
        int epicId = tokenizer.hasMoreTokens() ? Integer.parseInt(tokenizer.nextToken()) : 0;
        switch (type) {
            case "TASK":
                Task task = new Task(id, name, description, status);
                addNewTask(task);
                break;
            case "EPIC":
                Epic epic = new Epic(id, name, description);
                addNewEpic(epic);
                break;
            case "SUBTASK":
                Subtask subtask = new Subtask(id, name, description, status, epicId);
                addNewSubtask(subtask);
                break;
        }
    }
}
