package ru.yandex.javacourse.schedule.manager;

import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import java.io.*;

import java.util.StringTokenizer;

public class FileBackedTaskManager extends InMemoryTaskManager {
    String filename;

    public FileBackedTaskManager(String filename) {
        this.filename = filename;
        save();
    }

    public FileBackedTaskManager(String filename, String restoreFilename) {
        this.filename = filename;
        restoreTasksFromFile(restoreFilename);
        save();
    }

    @Override
    public int addNewTask(Task task) {
        generatorId++;
        final int id = task.getId() == 0 ? generatorId : task.getId();
        task.setId(id);
        tasks.put(id, task);
        save();
        return id;
    }

    @Override
    public int addNewEpic(Epic epic) {
        generatorId++;
        final int id = epic.getId() == 0 ? generatorId : epic.getId();
        epic.setId(id);
        epics.put(id, epic);
        updateEpicStatus(id);
        save();
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
        save();
        return id;
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void deleteSubtask(int id) {
        super.deleteSubtask(id);
        save();
    }

    @Override
    public void deleteTasks() {
        super.deleteTasks();
        save();
    }

    @Override
    public void deleteEpics() {
        super.deleteEpics();
        save();
    }

    @Override
    public void deleteSubtasks() {
        super.deleteSubtasks();
        save();
    }

    public void restoreTasksFromFile(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line = reader.readLine();
            while (reader.ready()) {
                line = reader.readLine();
                restoreTaskFromString(line);
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Что-то пошло не так при восстановлении состояния менеджера из файла");
        }
    }

    private void save() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write("id,type,name,status,description,epic\n");
            for (Task task : getTasks()) {
                writer.write(task.toStringForCSV() + "\n");
            }
            for (Epic epic : getEpics()) {
                writer.write(epic.toStringForCSV() + "\n");
            }
            for (Subtask subtask : getSubtasks()) {
                writer.write(subtask.toStringForCSV() + "\n");
            }
        } catch (IOException e) {
            throw new ManagerSaveException("Что-то пошло не так при сохранении задач в файл");
        }
    }

    private void restoreTaskFromString(String taskString) {
        StringTokenizer tokenizer = new StringTokenizer(taskString, ",");
        int id = Integer.parseInt(tokenizer.nextToken());
        String type = tokenizer.nextToken();
        String name = tokenizer.nextToken();
        TaskStatus status = TaskStatus.valueOf(tokenizer.nextToken());
        String description = tokenizer.nextToken();
        int epicId = tokenizer.hasMoreTokens() ? Integer.parseInt(tokenizer.nextToken()) : 0;
        switch (type) {
            case "TASK" -> addNewTask(new Task(id, name, description, status));
            case "EPIC" -> addNewEpic(new Epic(id, name, description));
            case "SUBTASK" -> addNewSubtask(new Subtask(id, name, description, status, epicId));
        }
    }
}
