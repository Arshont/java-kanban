package ru.yandex.javacourse.schedule.tasks;

import static ru.yandex.javacourse.schedule.tasks.TaskStatus.NEW;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {

    protected LocalDateTime endTime;

    protected ArrayList<Integer> subtaskIds = new ArrayList<>();

    public Epic(int id, String name, String description) {
        super(id, name, description, NEW);
    }

    public Epic(String name, String description) {
        super(name, description, NEW);
    }

    public void addSubtaskId(int id) {
        if (!subtaskIds.contains(id) && id != this.id) {
            subtaskIds.add(id);
        }
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void cleanSubtaskIds() {
        subtaskIds.clear();
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public void removeSubtask(int id) {
        subtaskIds.remove(Integer.valueOf(id));
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", description='" + description + '\'' +
                ", subtaskIds=" + subtaskIds +
                ", startTime='" + startTime +'\'' +
                ", duration=" + duration.toMinutes() +
                ", endTime=" + endTime + '\'' +
                '}';
    }

    @Override
    public String toStringForCSV() {
        return String.join(",",
                String.valueOf(id),
                "EPIC",
                name,
                status.toString(),
                description);
    }
}
