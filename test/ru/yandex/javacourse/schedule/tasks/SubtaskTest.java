package ru.yandex.javacourse.schedule.tasks;

import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SubtaskTest {

    @Test
    public void testEqualityById() {
        Subtask s0 = new Subtask(1, "Test 1", "Testing task 1", TaskStatus.NEW, Duration.ofMinutes(10), 1);
        Subtask s1 = new Subtask(1, "Test 2", "Testing task 2", TaskStatus.IN_PROGRESS, Duration.ofMinutes(10), 1);
        assertEquals(s0, s1, "task entities should be compared by id");
    }
}
