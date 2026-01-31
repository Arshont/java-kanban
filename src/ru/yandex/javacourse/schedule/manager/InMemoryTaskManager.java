package ru.yandex.javacourse.schedule.manager;

import ru.yandex.javacourse.schedule.http.exceptions.EpicNotFoundException;
import ru.yandex.javacourse.schedule.http.exceptions.HasInteractionsException;
import ru.yandex.javacourse.schedule.http.exceptions.NotFoundException;
import ru.yandex.javacourse.schedule.http.exceptions.SubtasksNotFoundException;
import ru.yandex.javacourse.schedule.tasks.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import static ru.yandex.javacourse.schedule.tasks.TaskStatus.IN_PROGRESS;
import static ru.yandex.javacourse.schedule.tasks.TaskStatus.NEW;

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
    public ArrayList<Subtask> getEpicSubtasks(int epicId) throws NotFoundException{
        ArrayList<Subtask> tasks = new ArrayList<>();
        Epic epic = epics.get(epicId);
        if (epic == null) {
            throw new NotFoundException();
        }
        epic.getSubtaskIds().stream().map(subtasks::get).forEach(tasks::add);
        return tasks;
    }

    @Override
    public Optional<Task> getTask(int id) {
        final Task task = tasks.get(id);
        historyManager.add(task);
        return Optional.ofNullable(task);
    }

    @Override
    public Optional<Subtask> getSubtask(int id) {
        final Subtask subtask = subtasks.get(id);
        historyManager.add(subtask);
        return Optional.ofNullable(subtask);
    }

    @Override
    public Optional<Epic> getEpic(int id) {
        final Epic epic = epics.get(id);
        historyManager.add(epic);
        return Optional.ofNullable(epic);
    }

    @Override
    public int addNewTask(Task task){
        if (!hasNewTaskTimeCrossings(task) && !tasks.containsValue(task)) {
            final int id = ++generatorId;
            task.setId(id);
            tasks.put(id, task);
            if (task.getStartTime().isPresent()) {
                prioritizedTasks.add(task);
            }
            return id;
        } else {
            return 0;
        }
    }

    @Override
    public int addNewEpic(Epic epic) {
        if (!epics.containsValue(epic)) {
            final int id = ++generatorId;
            epic.setId(id);
            epics.put(id, epic);
            updateEpicAttributes(id);
            return id;
        }
        return 0;
    }

    @Override
    public int addNewSubtask(Subtask subtask) {
        if (!hasNewTaskTimeCrossings(subtask) && !subtasks.containsValue(subtask)) {
            final int epicId = subtask.getEpicId();
            Epic epic = epics.get(epicId);
            if (epic == null) {
                return 0;
            }
            final int id = ++generatorId;
            subtask.setId(id);
            subtasks.put(id, subtask);
            if (subtask.getStartTime().isPresent()) {
                prioritizedTasks.add(subtask);
            }
            epic.addSubtaskId(subtask.getId());
            updateEpicAttributes(epicId);
            return id;
        }
        return 0;
    }

    @Override
    public void updateTask(Task task) throws HasInteractionsException, NotFoundException {
        final int id = task.getId();
        final Task savedTask = tasks.get(id);
        if (savedTask == null) {
            throw new NotFoundException();
        }
        if (hasNewTaskTimeCrossings(task)) {
            throw new HasInteractionsException();
        }
        if (savedTask.getStartTime().isPresent()) {
            prioritizedTasks.remove(savedTask);
        }
        if (task.getStartTime().isPresent()) {
            prioritizedTasks.add(task);
        }
        tasks.put(id, task);
    }

    @Override
    public void updateEpic(Epic epic) throws NotFoundException, HasInteractionsException, SubtasksNotFoundException {
        final int id = epic.getId();
        final Epic savedEpic = epics.get(id);

        if (savedEpic == null) {
            throw new NotFoundException();
        }

        boolean isAllSubtasksContained = epic.subtaskIds.stream().allMatch(subtasks::containsKey);
        if (!isAllSubtasksContained) {
            throw new SubtasksNotFoundException();
        }

        boolean hasNotInteractions = epic.subtaskIds.stream()
                        .allMatch(subId -> getSubtask(subId).get().getEpicId() == id);
        if (!hasNotInteractions) {
            throw new HasInteractionsException();
        }

        epics.put(id, epic);
        updateEpicAttributes(id);
    }

    @Override
    public void updateSubtask(Subtask subtask) throws EpicNotFoundException, HasInteractionsException, NotFoundException {
        final int id = subtask.getId();
        final int epicId = subtask.getEpicId();
        final Subtask savedSubtask = subtasks.get(id);
        if (savedSubtask == null) {
            throw new NotFoundException();
        }
        if (hasNewTaskTimeCrossings(subtask)) {
            throw new HasInteractionsException();
        }
        final Epic epic = epics.get(epicId);
        if (epic == null) {
            throw new EpicNotFoundException();
        }
        if (savedSubtask.getStartTime().isPresent()) {
            prioritizedTasks.remove(savedSubtask);
        }
        if (subtask.getStartTime().isPresent()) {
            prioritizedTasks.add(subtask);
        }
        subtasks.put(id, subtask);
        updateEpicAttributes(epicId);
    }

    @Override
    public void deleteTask(int id) throws NotFoundException {
        Optional<Task> taskOpt = getTask(id);
        if (taskOpt.isEmpty()) {
            throw new NotFoundException();
        }
        prioritizedTasks.remove(getTask(id).orElse(null));
        tasks.remove(id);
        historyManager.remove(id);
    }

    @Override
    public void deleteEpic(int id) throws NotFoundException {
        final Epic epic = epics.remove(id);
        if (epic == null) {
            throw new NotFoundException();
        }
        historyManager.remove(id);
        epic.getSubtaskIds().forEach(subtaskId -> {
            prioritizedTasks.remove(getSubtask(subtaskId).get());
            subtasks.remove(subtaskId);
            historyManager.remove(subtaskId);
        });
    }

    @Override
    public void deleteSubtask(int id) throws NotFoundException {
        Subtask subtask = subtasks.remove(id);
        if (subtask == null) {
            throw new NotFoundException();
        }
        Epic epic = epics.get(subtask.getEpicId());
        prioritizedTasks.remove(subtask);
        epic.removeSubtask(id);
        historyManager.remove(id);
        updateEpicAttributes(epic.getId());
    }

    @Override
    public void deleteTasks() {
        tasks.entrySet().forEach(entry -> {
            historyManager.remove(entry.getValue().getId());
            prioritizedTasks.remove(entry.getValue());
        });
        tasks.clear();
    }

    @Override
    public void deleteSubtasks() {
        epics.values().forEach(epic -> {
            epic.cleanSubtaskIds();
            updateEpicAttributes(epic.getId());
        });
        subtasks.entrySet().forEach(entry -> {
            historyManager.remove(entry.getValue().getId());
            prioritizedTasks.remove(entry.getValue());
        });
        subtasks.clear();
    }

    @Override
    public void deleteEpics() {
        epics.values().stream().mapToInt(Task::getId).forEach(historyManager::remove);
        subtasks.entrySet().forEach(entry -> {
            historyManager.remove(entry.getValue().getId());
            prioritizedTasks.remove(entry.getValue());
        });
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

            if (status == subtask.getStatus() && status != IN_PROGRESS) {
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
                    startTime = startTime.isAfter(subtasks.get(id).getStartTime().get()) ?
                            subtasks.get(id).getStartTime().get() : startTime;
                    endTime = endTime.isBefore(subtasks.get(id).getEndTime().get()) ?
                            subtasks.get(id).getEndTime().get() : endTime;
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
        if (!prioritizedTasks.isEmpty() && task.getStartTime().isPresent()) {
            return prioritizedTasks.stream().anyMatch(pTask -> pTask.isCrossedWith(task) && (!pTask.equals(task)));
        }
        return false;
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(prioritizedTasks);
    }
}
