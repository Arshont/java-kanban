package ru.yandex.javacourse.schedule.manager;

import static ru.yandex.javacourse.schedule.tasks.TaskStatus.IN_PROGRESS;
import static ru.yandex.javacourse.schedule.tasks.TaskStatus.NEW;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import ru.yandex.javacourse.schedule.tasks.*;

public class InMemoryTaskManager implements TaskManager {

    protected final Map<Integer, Task> tasks = new HashMap<>();
    protected final Map<Integer, Epic> epics = new HashMap<>();
    protected final Map<Integer, Subtask> subtasks = new HashMap<>();
    // Возможно, стоило использовать лямба-функцию вместо компаратора?
    protected final Set<Task> prioritizedTasks = new TreeSet<>(new TaskTimeComparator());
    protected int generatorId = 0;
    private final HistoryManager historyManager = Managers.getDefaultHistory();


    @Override
    public ArrayList<Task> getTasks() {
        return new ArrayList<>(this.tasks.values());
    }

    @Override
    public ArrayList<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public ArrayList<Subtask> getEpicSubtasks(int epicId) {
        ArrayList<Subtask> tasks = new ArrayList<>();
        Epic epic = epics.get(epicId);
        if (epic == null) {
            return null;
        }
        for (int id : epic.getSubtaskIds()) {
            tasks.add(subtasks.get(id));
        }
        return tasks;
    }

    @Override
    public Task getTask(int id) {
        final Task task = tasks.get(id);
        historyManager.add(task);
        return task;
    }

    @Override
    public Subtask getSubtask(int id) {
        final Subtask subtask = subtasks.get(id);
        historyManager.add(subtask);
        return subtask;
    }

    @Override
    public Epic getEpic(int id) {
        final Epic epic = epics.get(id);
        historyManager.add(epic);
        return epic;
    }

    @Override
    public int addNewTask(Task task) {
        if (!hasNewTaskTimeCrossings(task)) {
            final int id = ++generatorId;
            task.setId(id);
            tasks.put(id, task);
            addTaskWithPriority(task);
            return id;
        }
        return 0;
    }

    @Override
    public int addNewEpic(Epic epic) {
        final int id = ++generatorId;
        epic.setId(id);
        epics.put(id, epic);
        updateEpicAttributes(id);
        return id;
    }

    @Override
    public Integer addNewSubtask(Subtask subtask) {
        if (hasNewTaskTimeCrossings(subtask)) {
            final int epicId = subtask.getEpicId();
            Epic epic = epics.get(epicId);
            if (epic == null) {
                return null;
            }
            final int id = ++generatorId;
            subtask.setId(id);
            subtasks.put(id, subtask);
            addTaskWithPriority(subtask);
            epic.addSubtaskId(subtask.getId());
            updateEpicAttributes(epicId);
            return id;
        }
        return  0;
    }

    @Override
    public void updateTask(Task task) {
        final int id = task.getId();
        final Task savedTask = tasks.get(id);
        if (savedTask == null) {
            return;
        }
        prioritizedTasks.remove(task);
        addTaskWithPriority(task);
        tasks.put(id, task);
    }

    @Override
    public void updateEpic(Epic epic) {
        final Epic savedEpic = epics.get(epic.getId());
        savedEpic.setName(epic.getName());
        savedEpic.setDescription(epic.getDescription());
    }

    @Override
    public void updateSubtask(Subtask subtask) {
        final int id = subtask.getId();
        final int epicId = subtask.getEpicId();
        final Subtask savedSubtask = subtasks.get(id);
        if (savedSubtask == null) {
            return;
        }
        final Epic epic = epics.get(epicId);
        if (epic == null) {
            return;
        }
        prioritizedTasks.remove(subtask);
        addTaskWithPriority(subtask);
        subtasks.put(id, subtask);
        updateEpicAttributes(epicId);
    }

    @Override
    public void deleteTask(int id) {
        prioritizedTasks.remove(getTask(id));
        tasks.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void deleteEpic(int id) {
        final Epic epic = epics.remove(id);
        historyManager.remove(id);
        for (Integer subtaskId : epic.getSubtaskIds()) {
            prioritizedTasks.remove(getSubtasks().get(id));
            subtasks.remove(subtaskId);
            historyManager.remove(subtaskId);
        }
    }

    @Override
    public void deleteSubtask(int id) {
        Subtask subtask = subtasks.remove(id);
        if (subtask == null) {
            return;
        }
        Epic epic = epics.get(subtask.getEpicId());
        prioritizedTasks.remove(getSubtasks().get(id));
        epic.removeSubtask(id);
        historyManager.remove(id);
        updateEpicAttributes(epic.getId());
    }

    @Override
    public void deleteTasks() {
        for (Map.Entry<Integer, Task> entry : tasks.entrySet()) {
            historyManager.remove(entry.getValue().getId());
            prioritizedTasks.remove(entry.getValue());
        }
        tasks.clear();
    }

    @Override
    public void deleteSubtasks() {
        for (Epic epic : epics.values()) {
            epic.cleanSubtaskIds();
            updateEpicAttributes(epic.getId());
        }
        for (Map.Entry<Integer, Subtask> entry : subtasks.entrySet()) {
            historyManager.remove(entry.getValue().getId());
            prioritizedTasks.remove(entry.getValue());
        }
        subtasks.clear();
    }

    @Override
    public void deleteEpics() {
        for (Map.Entry<Integer, Epic> entry : epics.entrySet()) {
            historyManager.remove(entry.getValue().getId());
        }
        for (Map.Entry<Integer, Subtask> entry : subtasks.entrySet()) {
            historyManager.remove(entry.getValue().getId());
            prioritizedTasks.remove(entry.getValue());
        }
        epics.clear();
        subtasks.clear();
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    protected void updateEpicAttributes(int epicId) {
        updateEpicStatus(epicId);
        updateEpicTimes(epicId);
    }

    protected void updateEpicStatus(int epicId) {
        final Epic epic = epics.get(epicId);
        List<Integer> subs = epic.getSubtaskIds();
        if (subs.isEmpty()) {
            epic.setStatus(NEW);
            return;
        }
        TaskStatus status = null;
        for (int id : subs) {
            final Subtask subtask = subtasks.get(id);
            if (status == null) {
                status = subtask.getStatus();
                continue;
            }

            if (status == subtask.getStatus()
                    && status != IN_PROGRESS) {
                continue;
            }
            epic.setStatus(IN_PROGRESS);
            return;
        }
        epic.setStatus(status);
    }

    protected void updateEpicTimes(int epicId) {
        final Epic epic = epics.get(epicId);
        LocalDateTime startTime = LocalDateTime.MAX;
        LocalDateTime endTime = LocalDateTime.MIN;
        Duration totalDuration = Duration.ZERO;
        Duration totalDurationWithStartTime = Duration.ZERO;
        if (!epic.getSubtaskIds().isEmpty()) {
            for (int id : epic.getSubtaskIds()) {
                if (subtasks.get(id).getStartTime().isPresent()) {
                    startTime = startTime.isAfter(subtasks.get(id).getStartTime().get()) ? subtasks.get(id).getStartTime().get() : startTime;
                    endTime = endTime.isBefore(subtasks.get(id).getEndTime().get()) ? subtasks.get(id).getEndTime().get() : endTime;
                    totalDurationWithStartTime = totalDurationWithStartTime.plus(subtasks.get(id).getDuration());
                }
                totalDuration = totalDuration.plus(subtasks.get(id).getDuration());
            }
            if (startTime != LocalDateTime.MAX) {
                epic.setStartTime(startTime);
                epic.setEndTime(endTime.plus(totalDuration.minus(totalDurationWithStartTime)));
            }
            epic.setDuration(totalDuration);
        }
    }

    protected boolean hasNewTaskTimeCrossings(Task task) {
        if (!prioritizedTasks.isEmpty()) {
//            Iterator<Task> iterator = prioritizedTasks.iterator();
//            while (iterator.hasNext()){
//                Task task1 = iterator.next();
//                if (task1.isCrossedWith(task)) {
//                    return true;
//                }
//                task = task1;
//            }
            return prioritizedTasks.stream()
                    .anyMatch(pTask -> pTask.isCrossedWith(task));
        }
        return false;
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }

    protected void addTaskWithPriority(Task task) {
        if (task.getStartTime().isPresent()) {
            prioritizedTasks.add(task);
        }
    }
}
