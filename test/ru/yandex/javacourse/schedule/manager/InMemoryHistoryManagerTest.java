//package ru.yandex.javacourse.schedule.manager;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import ru.yandex.javacourse.schedule.tasks.Task;
//import ru.yandex.javacourse.schedule.tasks.TaskStatus;
//
//import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//public class InMemoryHistoryManagerTest {
//
//    HistoryManager historyManager;
//
//    @BeforeEach
//    public void initHistoryManager() {
//        historyManager = Managers.getDefaultHistory();
//    }
//
//    @Test
//    public void testHistoricVersions() {
//        Task task = new Task(1, "Test 1", "Testiong task 1", TaskStatus.NEW);
//        historyManager.add(task);
//        assertEquals(1, historyManager.getHistory().size(), "historic task should be added");
//        task.setStatus(TaskStatus.IN_PROGRESS);
//        historyManager.add(task);
//        assertEquals(1, historyManager.getHistory().size(), "historic task shouldn't be added");
//        assertEquals(TaskStatus.IN_PROGRESS, historyManager.getHistory().getFirst().getStatus(), "historic task should be replaced by new task");
//        historyManager.add(new Task(2, "Test 2", "Testing task 2", TaskStatus.NEW));
//        assertEquals(2, historyManager.getHistory().size(), "historic task should be added");
//    }
//
//    @Test
//    public void testHistoricVersionsByPointer() {
//        Task task = new Task("Test 1", "Testiong task 1", TaskStatus.NEW);
//        historyManager.add(task);
//        assertEquals(task.getStatus(), historyManager.getHistory().getFirst().getStatus(), "historic task should be stored");
//        task.setStatus(TaskStatus.IN_PROGRESS);
//        historyManager.add(task);
//        assertEquals(TaskStatus.IN_PROGRESS, historyManager.getHistory().getFirst().getStatus(), "historic task should not be changed");
//    }
//
//    @Test
//    public void testHistoryVersionsRemoval() {
//        Task task = new Task(1, "Test 1", "Testiong task 1", TaskStatus.NEW);
//        historyManager.add(task);
//        assertEquals(1, historyManager.getHistory().size(), "historic task should be added");
//        historyManager.add(new Task(2, "Test 2", "Testing task 2", TaskStatus.NEW));
//        assertEquals(2, historyManager.getHistory().size(), "historic task should be added");
//        historyManager.remove(2);
//        assertEquals(1, historyManager.getHistory().size(), "historic task should be removed");
//        assertEquals(task, historyManager.getHistory().getFirst(), "historic task shouldn't change and shouldn't be removed");
//        historyManager.remove(1);
//        assertEquals(0, historyManager.getHistory().size(), "historic task should be removed");
//    }
//
//    @Test
//    public void testHistoryNullProcessing() {
//        historyManager.add(null);
//        assertEquals(0, historyManager.getHistory().size(), "historic task shouldn't be added");
//        for (int i = 0; i < 11; i++) {
//            Task task = new Task(i + 1, "Test 1", "Testing task 1", TaskStatus.NEW);
//            historyManager.add(task);
//        }
//        assertEquals(11, historyManager.getHistory().size(), "historic tasks should be added");
//        assertDoesNotThrow(() -> historyManager.remove(15), "removal of null shouldn't throw any exceptions");
//        historyManager.remove(1);
//        assertEquals(10, historyManager.getHistory().size(), "historic task from the head should be removed");
//        historyManager.remove(11);
//        assertEquals(9, historyManager.getHistory().size(), "historic task from the tail should be removed");
//        historyManager.remove(6);
//        assertEquals(8, historyManager.getHistory().size(), "historic task from the middle should be removed");
//    }
//
//}
