package ru.yandex.javacourse.schedule.tasks;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class TaskTest {

    @Test
    public void testEqualityById() {
        Task t0 = new Task(1, "Test 1", "Testing task 1", TaskStatus.NEW);
        Task t1 = new Task(1, "Test 2", "Testing task 2", TaskStatus.IN_PROGRESS);
        assertEquals(t0, t1, "task entities should be compared by id");
    }

    @Test
    public void testEndTimeCalculation() {
        LocalDateTime startTime = LocalDateTime.now();
        Duration duration = Duration.ofMinutes(45);
        Task t = new Task(1, "Test 1", "Testing task 1", TaskStatus.NEW, duration, startTime);
        assertTrue(t.getStartTime().isPresent(), "Время начала задачи не должно быть null");
        assertTrue(t.getEndTime().isPresent(), "Время завершения задачи не должно быть null");
        assertEquals(t.getEndTime().get(), startTime.plus(duration), "Время завершения задачи рассчитанное с помощью метода должно совпадать с рассчётным тестовым");
    }

    @Test
    public void testOptionalTimes() {
        Task t = new Task(1, "Test 1", "Testing task 1", TaskStatus.NEW, Duration.ofMinutes(30));
        assertTrue(t.getStartTime().isEmpty(), "Время начала задачи должно равняться null");
        assertTrue(t.getEndTime().isEmpty(), "Время завершения задачи должно равняться null");
    }

    @Test
    public void testCrossingTasks() {
        Task t0 = new Task(1, "Test 1", "Testing task 1", TaskStatus.NEW, Duration.ofMinutes(15),
                LocalDateTime.of(2025, 12, 4, 13, 0));
        Task t1 = new Task(2, "Test 2", "Testing task 2", TaskStatus.NEW, Duration.ofMinutes(15),
                LocalDateTime.of(2025, 12, 4, 13, 10));
        Task t2 = new Task(3, "Test 3", "Testing task 3", TaskStatus.NEW, Duration.ofMinutes(15),
                LocalDateTime.of(2025, 12, 4, 13, 15));
        Task t3 = new Task(4, "Test 4", "Testing task 4", TaskStatus.NEW, Duration.ofMinutes(15),
                LocalDateTime.of(2025, 12, 4, 13, 20));
        Task t4 = new Task(5, "Test 5", "Testing task 5", TaskStatus.NEW, Duration.ofMinutes(15));
        assertTrue(t0.isCrossedWith(t0), "Задача должна пересекаться во времени сама с собой");
        assertTrue(t0.isCrossedWith(t1), "Задачи пересекающиеся во времени должны давать True");
        assertTrue(t1.isCrossedWith(t0), "Задачи пересекающиеся во времени при swap-е друг друга (объект-аргумент) всё ещё должны давать True");
        assertTrue(t0.isCrossedWith(t2), "Задачи пересекающиеся на границе времен (закончилась одна и началась " +
                "следующая в тот же момент времени) должны давать True");
        assertFalse(t0.isCrossedWith(t3), "Задачи не пересекающиеся во времени должны давать False");
        assertFalse(t0.isCrossedWith(t4), "Когда у одной из задач время начала не задано, должно быть False");
    }

}
