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

    @Override
    public int addNewTask(Task task) {
        generatorId++;
        final int id = task.getId() == 0 ? generatorId : task.getId();
        task.setId(id);
        tasks.put(id, task);
        return id;
    }

    @Override
    public int addNewEpic(Epic epic) {
        generatorId++;
        final int id = epic.getId() == 0 ? generatorId : epic.getId();
        epic.setId(id);
        epics.put(id, epic);
        updateEpicStatus(id);
        return id;

    }

    @Override
    public Integer addNewSubtask(Subtask subtask) {
        final int epicId = subtask.getEpicId();
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return null;
        }
        generatorId++;
        final int id = subtask.getId() == 0 ? generatorId : subtask.getId();
        subtask.setId(id);
        subtasks.put(id, subtask);
        epic.addSubtaskId(subtask.getId());
        updateEpicStatus(epicId);
        return id;
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
