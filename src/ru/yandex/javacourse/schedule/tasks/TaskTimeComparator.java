package ru.yandex.javacourse.schedule.tasks;

import java.util.Comparator;

public class TaskTimeComparator implements Comparator<Task> {

    @Override
    public int compare(Task o1, Task o2) {
        return o1.getStartTime().get().compareTo(o2.getStartTime().get());
    }
}
