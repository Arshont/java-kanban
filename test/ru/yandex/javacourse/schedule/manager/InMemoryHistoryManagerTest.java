package ru.yandex.javacourse.schedule.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class InMemoryHistoryManagerTest {

    final static Duration basicTestDuration = Duration.ofMinutes(15);
    HistoryManager historyManager;

    @BeforeEach
    public void initHistoryManager() {
        historyManager = Managers.getDefaultHistory();
    }

    @Test
    public void testHistoricVersions() {
        Task task = new Task(1, "Test 1", "Testing task 1", TaskStatus.NEW, basicTestDuration);
        historyManager.add(task);
        assertEquals(1, historyManager.getHistory().size(), "Задача должна быть добавлена в историю");
        task.setStatus(TaskStatus.IN_PROGRESS);
        historyManager.add(task);
        assertEquals(1, historyManager.getHistory().size(), "Не должно быть добавлено новых задач в историю");
        assertEquals(TaskStatus.IN_PROGRESS, historyManager.getHistory().get(0).getStatus(), "Задача в истории должна быть замещена новой");
        historyManager.add(new Task(2, "Test 2", "Testing task 2", TaskStatus.NEW, basicTestDuration));
        assertEquals(2, historyManager.getHistory().size(), "Должна быть добавлена новая задача в историю");
    }

    @Test
    public void testHistoryVersionsRemoval() {
        Task task = new Task(1, "Test 1", "Testing task 1", TaskStatus.NEW, basicTestDuration);
        historyManager.add(task);
        assertEquals(1, historyManager.getHistory().size(), "В историю должна быть добавлена одна задача");
        historyManager.add(new Task(2, "Test 2", "Testing task 2", TaskStatus.NEW, basicTestDuration));
        assertEquals(2, historyManager.getHistory().size(), "В историю должна быть добавлена вторая задача");

        historyManager.remove(2);
        assertEquals(1, historyManager.getHistory().size(), "Должна быть удалена одна задача из истории");
        assertEquals(task, historyManager.getHistory().get(0), "Первая задача не должна быть подвергнута изменениям/удалена");
        historyManager.remove(1);
        assertEquals(0, historyManager.getHistory().size(), "История должна быть опустошена");
    }

    @Test
    public void testRemovalFromHistory() {
        historyManager.add(null);
        assertEquals(0, historyManager.getHistory().size(), "Ни одна задача не должна быть добавлена");
        for (int i = 0; i < 11; i++) {
            Task task = new Task(i + 1, "Test 1", "Testing task 1", TaskStatus.NEW, basicTestDuration);
            historyManager.add(task);
        }
        assertEquals(11, historyManager.getHistory().size(), "В истории должно сожержаться 11 задач");
        assertDoesNotThrow(() -> historyManager.remove(15), "Удаление несуществующей задачи не должно выбрасывать исключений");
        historyManager.remove(1);
        assertEquals(10, historyManager.getHistory().size(), "Удаление из начала истории должно работать корректно");
        historyManager.remove(11);
        assertEquals(9, historyManager.getHistory().size(), "Удаление из конца истории должно работать корректно");
        historyManager.remove(6);
        assertEquals(8, historyManager.getHistory().size(), "Удаление из середины истории должно работать корректно");
    }

}
