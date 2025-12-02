package ru.yandex.javacourse.schedule.tasks;

import java.time.Duration;
import java.time.LocalDateTime;

public class Subtask extends Task {
    protected int epicId;

    public Subtask(int id, String name, String description, TaskStatus status, int epicId, Duration duration, LocalDateTime startTime) {
        super(id, name, description, status, duration, startTime);
        this.epicId = epicId;
    }

    public Subtask(int id, String name, String description, TaskStatus status, int epicId, Duration duration) {
        super(id, name, description, status, duration);
        this.epicId = epicId;
    }

    public Subtask(String name, String description, TaskStatus status, int epicId, Duration duration, LocalDateTime startTime) {
        super(name, description, status, duration, startTime);
        this.epicId = epicId;
    }

    public Subtask(String name, String description, TaskStatus status, int epicId, Duration duration) {
        super(name, description, status, duration);
        this.epicId = epicId;
    }


    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "id=" + id +
                ", epicId=" + epicId +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", description='" + description + '\'' +
                ", startTime='" + startTime +'\'' +
                ", duration=" + duration.toMinutes() +
                '}';
    }

    @Override
    public String toStringForCSV() {
        return String.join(",",
                String.valueOf(id),
                "SUBTASK",
                name,
                status.toString(),
                description,
                startTime != null ? startTime.toString() : " ",
                String.valueOf(duration.toMinutes()),
                String.valueOf(epicId));
    }
}
