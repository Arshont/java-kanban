package ru.yandex.javacourse.schedule.manager;

import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import java.io.*;

import java.time.Duration;
import java.time.LocalDateTime;
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
        if (task.getId() != 0) {
            // Необходимо, чтобы при добавлении задачи из файла не возникало коллизии id в дальнейшем
            generatorId = task.getId() - 1;
        }
        final int id = super.addNewTask(task);
        if (id != 0) {
            save();
        }
        return id;
    }

    @Override
    public int addNewEpic(Epic epic) {
        if (epic.getId() != 0) {
            generatorId = epic.getId() - 1;
        }
        final int id = super.addNewEpic(epic);
        if (id != 0) {
            save();
        }
        return id;
    }

    @Override
    public Integer addNewSubtask(Subtask subtask) {
        if (subtask.getId() != 0) {
            // Необходимо, чтобы при добавлении задачи из файла не возникало коллизии id в дальнейшем
            generatorId = subtask.getId() - 1;
        }
        final int id = super.addNewSubtask(subtask);
        if (id != 0) {
            save();
        }
        return id;
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateEpic(epic);
        save();
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        super.updateSubtask(subtask);
        save();
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
            writer.write("id,type,name,status,description,startTime,duration,epic\n");
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
        if (tokenizer.hasMoreTokens()) {
            String startTimeToken = tokenizer.nextToken();
            LocalDateTime startTime = !startTimeToken.equals(" ") ? LocalDateTime.parse(startTimeToken) : null;
            Duration duration = Duration.ofMinutes(Integer.parseInt(tokenizer.nextToken()));
            int epicId = tokenizer.hasMoreTokens() ? Integer.parseInt(tokenizer.nextToken()) : 0;
            switch (type) {
                case "TASK" -> addNewTask(new Task(id, name, description, status, duration, startTime));
                case "SUBTASK" ->
                        addNewSubtask(new Subtask(id, name, description, status, duration, startTime, epicId));
            }
        } else {
            addNewEpic(new Epic(id, name, description));
        }

    }
}
