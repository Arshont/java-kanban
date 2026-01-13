package ru.yandex.javacourse.schedule.tasks;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

public class Task {
    protected int id;
    protected String name;
    protected TaskStatus status;
    protected String description;
    protected Duration duration;
    protected LocalDateTime startTime;

    public Task(int id, String name, String description, TaskStatus status, Duration duration, LocalDateTime startTime) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.duration = duration;
        this.startTime = startTime;
    }

    public Task(int id, String name, String description, TaskStatus status, Duration duration) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
        this.duration = duration;
    }

    protected Task(int id, String name, String description, TaskStatus status) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.status = status;
    }

    public Task(String name, String description, TaskStatus status, Duration duration, LocalDateTime startTime) {
        this.name = name;
        this.description = description;
        this.status = status;
        this.duration = duration;
        this.startTime = startTime;
    }

    public Task(String name, String description, TaskStatus status, Duration duration) {
        this.name = name;
        this.description = description;
        this.status = status;
        this.duration = duration;
    }

    protected Task(String name, String description, TaskStatus status) {
        this.name = name;
        this.description = description;
        this.status = status;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public Duration getDuration() {
        return duration;
    }

    public Optional<LocalDateTime> getStartTime() {
        return Optional.ofNullable(startTime);
    }

    public Optional<LocalDateTime> getEndTime() {
        if (startTime != null) {
            return Optional.of(startTime.plus(duration));
        } else {
            return Optional.empty();
        }
    }

    public void setName(String name) {
        this.name = name;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status='" + status + '\'' +
                ", description='" + description + '\'' +
                ", startTime='" + startTime + '\'' +
                ", duration=" + duration +
                '}';
    }

    public String toStringForCSV() {
        return String.join(",",
                String.valueOf(id),
                "TASK",
                name,
                status.toString(),
                description,
                startTime != null ? startTime.toString() : " ",
                String.valueOf(duration.toMinutes()));
    }

    public boolean isCrossedWith(Task task) {
        if (task.getEndTime().isPresent() && getEndTime().isPresent()) {
            if (getStartTime().get().isBefore(task.getStartTime().get())) {
                // Если первая (объект, вызывающий метод) задача началась раньше
                return !getEndTime().get().isBefore(task.getStartTime().get());
            } else if (getStartTime().get().isAfter(task.getStartTime().get())) {
                // Если вторая (аргумент) задача началась раньше
                return !task.getEndTime().get().isBefore(getStartTime().get());
            } else {
                // Если задачи начинаются одновременно
                return true;
            }
        } else {
            return false;
        }
    }
}
