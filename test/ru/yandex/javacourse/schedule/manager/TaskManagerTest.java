package ru.yandex.javacourse.schedule.manager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.javacourse.schedule.tasks.Epic;
import ru.yandex.javacourse.schedule.tasks.Subtask;
import ru.yandex.javacourse.schedule.tasks.Task;
import ru.yandex.javacourse.schedule.tasks.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class TaskManagerTest {
    TaskManager manager;
    final static Duration basicTestDuration = Duration.ofMinutes(15);

    @BeforeEach
    public void initManager() {
        manager = Managers.getDefault();
    }

    @Test
    public void testAddTaskWithoutId() {
        Task task = new Task("Test 1", "Testing task 1", TaskStatus.NEW, basicTestDuration);
        
        manager.addNewTask(task);
        assertEquals(1, manager.getTasks().size(), "Одна задача должна быть добавлена");
        assertSame(manager.getTask(1), task, "Задача должна остаться той же самой");
        assertEquals(0, manager.getPrioritizedTasks().size(), "Не должно быть добавлено ни одной задачи с приоритетом");
        
        manager.addNewTask(task);
        assertEquals(1, manager.getTasks().size(), "Задача не должна быть добавлена повторно");
        assertEquals(0, manager.getPrioritizedTasks().size(), "Не должно быть добавлено ни одной задачи с приоритетом");
        Task task1 = new Task("Test 2", "Testing task 2", TaskStatus.NEW, basicTestDuration);
        
        manager.addNewTask(task1);
        assertEquals(2, manager.getTasks().size(), "Должно быть добавлено 2 задачи");
        assertNotNull(manager.getTask(2), "Вторая задача должна быть найдена");
        assertEquals(2, manager.getTask(2).getId(), "Id второй задачи должен быть равен 2");
        assertEquals(0, manager.getPrioritizedTasks().size(), "Не должно быть добавлено ни одной задачи с приоритетом");
    }

    @Test
    public void testAddTaskWithId() {
        Task task = new Task(42, "Test 1", "Testing task 1", TaskStatus.NEW, basicTestDuration);
        
        manager.addNewTask(task);
        assertEquals(1, manager.getTasks().size(), "Одна задача должна быть добавлена");
        assertNotNull(manager.getTask(1), "Задача должна быть добавлена в таблицу с id равным 1");
        assertEquals(1, manager.getTask(1).getId(), "Id добавленной задачи должен быть замещён на 1");
        assertEquals(0, manager.getPrioritizedTasks().size(), "Не должно быть добавлено ни одной задачи с приоритетом");
    }

    @Test
    public void testAddEpicWithoutId() {
        Epic epic = new Epic("Test 1", "Testing epic 1");
        
        manager.addNewEpic(epic);
        assertEquals(1, manager.getEpics().size(), "Один эпик должен быть добавлен");
        assertSame(manager.getEpic(1), epic, "Эпик должен остаться тем же самым");
        
        manager.addNewEpic(epic);
        assertEquals(1, manager.getEpics().size(), "Эпик не должен быть добавлен повторно");
        Epic epic1 = new Epic("Test 2", "Testing epic 2");
        
        manager.addNewEpic(epic1);
        assertEquals(2, manager.getEpics().size(), "Должно быть добавлено 2 эпика");
        assertNotNull(manager.getEpic(2), "Второй эпик должна быть найден");
        assertEquals(2, manager.getEpic(2).getId(), "Id второго эпика должен быть равен 2");
    }

    @Test
    public void testAddEpicWithId() {
        Epic epic = new Epic("Test 1", "Testing epic 1");
        
        manager.addNewEpic(epic);
        assertEquals(1, manager.getEpics().size(), "Один эпик должен быть добавлен");
        assertNotNull(manager.getEpic(1), "Эпик должен быть добавлен в таблицу с id равным 1");
        assertEquals(1, manager.getEpic(1).getId(), "Id добавленного эпика должен быть замещён на 1");
    }

    @Test
    public void testAddSubtaskWithoutId() {
        Epic epic = new Epic("Test 1", "Testing epic 1");
        manager.addNewEpic(epic);

        Subtask subtaskWithoutEpic = new Subtask("Test 0", "Testing subtask 0", TaskStatus.NEW, basicTestDuration, 0);
        assertNull(manager.addNewSubtask(subtaskWithoutEpic), "Добавление задчи должно завершиться неудачно");
        assertEquals(0, manager.getSubtasks().size(), "Ни одна подзадача не должна быть добавлена");
        assertEquals(0, manager.getPrioritizedTasks().size(), "Не должно быть добавлено ни одной задачи с приоритетом");

        Subtask subtask = new Subtask("Test 1", "Testing subtask 1", TaskStatus.NEW, basicTestDuration, epic.getId());
        manager.addNewSubtask(subtask);
        assertEquals(1, manager.getSubtasks().size(), "Одна подзадача должна быть добавлена");
        assertSame(manager.getSubtask(epic.getId() + 1), subtask, "Подзадача должна остаться той же самой");
        assertEquals(0, manager.getPrioritizedTasks().size(), "Не должно быть добавлено ни одной задачи с приоритетом");

        manager.addNewSubtask(subtask);
        assertEquals(1, manager.getSubtasks().size(), "Подзадача не должна быть добавлена повторно");
        assertEquals(0, manager.getPrioritizedTasks().size(), "Не должно быть добавлено ни одной задачи с приоритетом");

        Subtask subtask1 = new Subtask("Test 2", "Testing subtask 2", TaskStatus.NEW, basicTestDuration, epic.getId());
        manager.addNewSubtask(subtask1);
        assertEquals(2, manager.getSubtasks().size(), "Должно быть добавлено 2 подзадачи");
        assertNotNull(manager.getSubtask(2), "Вторая подзадача должна быть найдена");
        assertEquals(2, manager.getSubtask(2).getId(), "Id второй подзадачи должен быть равен 2");
        assertEquals(0, manager.getPrioritizedTasks().size(), "Не должно быть добавлено ни одной задачи с приоритетом");
    }

    @Test
    public void testAddSubtaskWithId() {
        Epic epic = new Epic("Test 1", "Testing epic 1");
        manager.addNewEpic(epic);

        Subtask subtask = new Subtask(42, "Test 1", "Testing subtask 1", TaskStatus.NEW, basicTestDuration, epic.getId());
        manager.addNewSubtask(subtask);
        assertEquals(1, manager.getSubtasks().size(), "Одна подзадача должна быть добавлена");
        assertNotNull(manager.getSubtask(2), "Подадача должна быть добавлена в таблицу с id равным 2");
        assertEquals(2, manager.getSubtask(2).getId(), "Id добавленной подзадачи должен быть замещён на 2");
        assertEquals(0, manager.getPrioritizedTasks().size(), "Не должно быть добавлено ни одной задачи с приоритетом");
    }

    @Test
    public void testUpdateTaskWithoutTime() {
        Task task = new Task("Test 1", "Testing task 1", TaskStatus.NEW, basicTestDuration);
        manager.addNewTask(task);

        Task newTaskWithoutId = new Task("Test 2", "Testing task 2", TaskStatus.NEW, basicTestDuration);
        assertDoesNotThrow(() -> manager.updateTask(newTaskWithoutId), "Метод должен выполниться корректно");
        assertSame(task, manager.getTask(task.getId()), "Задача не должна измениться");
        assertEquals(1, manager.getTasks().size(), "Количество задач не должно измениться");

        Task newTaskWithIncorrectId = new Task(42, "Test 3", "Testing task 3", TaskStatus.NEW, basicTestDuration);
        assertDoesNotThrow(() -> manager.updateTask(newTaskWithIncorrectId), "Метод должен выполниться корректно");
        assertSame(task, manager.getTask(task.getId()), "Задача не должна измениться");
        assertEquals(1, manager.getTasks().size(), "Количество задач не должно измениться");

        Task newTask = new Task(1,"Test 4", "Testing task 4", TaskStatus.NEW, basicTestDuration);
        assertDoesNotThrow(() -> manager.updateTask(newTask), "Метод должен выполниться корректно");
        assertSame(newTask, manager.getTask(task.getId()), "Задача по тому же Id должна быть заменена");
        assertNotSame(task, manager.getTask(task.getId()), "Задача по тому же Id должна быть заменена");
        assertEquals(1, manager.getTasks().size(), "Количество задач не должно измениться");
    }

    @Test
    public void testUpdateEpic() {
        Epic epic = new Epic("Test 1", "Testing epic 1");
        manager.addNewEpic(epic);

        Epic newEpicWithoutId = new Epic("Test 2", "Testing epic 2");
        assertDoesNotThrow(() -> manager.updateEpic(newEpicWithoutId), "Метод должен выполниться корректно");
        assertSame(epic, manager.getEpic(epic.getId()), "Эпик не должен измениться");
        assertEquals(1, manager.getEpics().size(), "Количество эпиков не должно измениться");

        Epic newEpicWithIncorrectId = new Epic(42, "Test 3", "Testing epic 3");
        assertDoesNotThrow(() -> manager.updateEpic(newEpicWithIncorrectId), "Метод должен выполниться корректно");
        assertSame(epic, manager.getEpic(epic.getId()), "Эпик не должен измениться");
        assertEquals(1, manager.getEpics().size(), "Количество эпиков не должно измениться");

        Epic newEpic = new Epic(1,"Test 4", "Testing epic 4");
        assertDoesNotThrow(() -> manager.updateEpic(newEpic), "Метод должен выполниться корректно");
        assertSame(newEpic, manager.getEpic(epic.getId()), "Эпик по тому же Id должен быть заменен");
        assertNotSame(epic, manager.getEpic(epic.getId()), "Эпик по тому же Id должен быть заменен");
        assertEquals(1, manager.getEpics().size(), "Количество эпиков не должно измениться");
    }

    @Test
    public void testUpdateSubtaskWithoutTime() {
        Epic epic = new Epic("Test 1", "Testing epic 1");
        manager.addNewEpic(epic);
        
        Subtask subtask = new Subtask("Test 2", "Testing subtask 2", TaskStatus.NEW, basicTestDuration, epic.getId());
        manager.addNewSubtask(subtask);

        Subtask newSubtaskWithoutId = new Subtask("Test 3", "Testing subtask 3", TaskStatus.NEW, basicTestDuration, epic.getId());
        assertDoesNotThrow(() -> manager.updateSubtask(newSubtaskWithoutId), "Метод должен выполниться корректно");
        assertSame(subtask, manager.getSubtask(subtask.getId()), "Подзадача не должна измениться");
        assertEquals(1, manager.getSubtasks().size(), "Количество подзадач не должно измениться");

        Subtask newSubtaskWithIncorrectId = new Subtask(42, "Test 4", "Testing subtask 4", TaskStatus.NEW, basicTestDuration, epic.getId());
        assertDoesNotThrow(() -> manager.updateSubtask(newSubtaskWithIncorrectId), "Метод должен выполниться корректно");
        assertSame(subtask, manager.getSubtask(subtask.getId()), "Подзадача не должна измениться");
        assertEquals(1, manager.getSubtasks().size(), "Количество подзадач не должно измениться");

        Subtask newSubtaskWithIncorrectEpicId = new Subtask(42, "Test 5", "Testing subtask 5", TaskStatus.NEW, basicTestDuration, 52);
        assertDoesNotThrow(() -> manager.updateSubtask(newSubtaskWithIncorrectEpicId), "Метод должен выполниться корректно");
        assertSame(subtask, manager.getSubtask(subtask.getId()), "Подзадача не должна измениться");
        assertEquals(1, manager.getSubtasks().size(), "Количество подзадач не должно измениться");

        Subtask newSubtask = new Subtask(subtask.getId(),"Test 5", "Testing subtask 5", TaskStatus.NEW, basicTestDuration, epic.getId());
        assertDoesNotThrow(() -> manager.updateSubtask(newSubtask), "Метод должен выполниться корректно");
        assertSame(newSubtask, manager.getSubtask(subtask.getId()), "Подзадача по тому же Id должна быть заменена");
        assertNotSame(subtask, manager.getSubtask(subtask.getId()), "Подзадача по тому же Id должна быть заменена");
        assertEquals(1, manager.getSubtasks().size(), "Количество подзадач не должно измениться");
    }

    @Test
    public void testDeleteTaskWithoutTime() {
        assertEquals(0, manager.getTasks().size(), "Список задач должен быть пуст");
        assertDoesNotThrow(() -> manager.deleteTask(42), "Метод должен выполниться корректно");
        assertEquals(0, manager.getTasks().size(), "Список задач должен быть пуст");

        Task task = new Task("Test 1", "Testing task 1", TaskStatus.NEW, basicTestDuration);
        manager.addNewTask(task);

        assertEquals(1, manager.getTasks().size(), "Одна задача должна быть добавлена");
        assertDoesNotThrow(() -> manager.deleteTask(42), "Метод должен выполниться корректно");
        assertEquals(1, manager.getTasks().size(), "Количество задач не должно измениться");
        assertSame(task, manager.getTask(1), "Задача должна остаться той же");

        assertEquals(1, manager.getTasks().size(), "В списке задач должна быть одна задача");
        assertDoesNotThrow(() -> manager.deleteTask(1), "Метод должен выполниться корректно");
        assertEquals(0, manager.getTasks().size(), "Список задач должен быть пуст");
    }

    @Test
    public void testDeleteEpic() {
        assertEquals(0, manager.getEpics().size(), "Список задач должен быть пуст");
        assertDoesNotThrow(() -> manager.deleteEpic(42), "Метод должен выполниться корректно");
        assertEquals(0, manager.getEpics().size(), "Список задач должен быть пуст");

        Epic epic = new Epic("Test 1", "Testing epic 1");
        manager.addNewEpic(epic);

        assertEquals(1, manager.getEpics().size(), "Одна задача должна быть добавлена");
        assertDoesNotThrow(() -> manager.deleteEpic(42), "Метод должен выполниться корректно");
        assertEquals(1, manager.getEpics().size(), "Количество задач не должно измениться");
        assertSame(epic, manager.getEpic(1), "Задача должна остаться той же");

        assertEquals(1, manager.getEpics().size(), "В списке задач должна быть одна задача");
        assertDoesNotThrow(() -> manager.deleteEpic(1), "Метод должен выполниться корректно");
        assertEquals(0, manager.getEpics().size(), "Список задач должен быть пуст");
    }

    @Test
    public void testDeleteSubtaskWithoutTime() {
        Epic epic = new Epic("Test 1", "Testing epic 1");
        manager.addNewEpic(epic);

        assertEquals(0, manager.getSubtasks().size(), "Список задач должен быть пуст");
        assertDoesNotThrow(() -> manager.deleteSubtask(42), "Метод должен выполниться корректно");
        assertEquals(0, manager.getSubtasks().size(), "Список задач должен быть пуст");

        Subtask subtask = new Subtask("Test 2", "Testing subtask 1", TaskStatus.NEW, basicTestDuration, 1);
        manager.addNewSubtask(subtask);

        assertEquals(1, manager.getEpic(1).getSubtaskIds().size(), "У эпика должна быть добавлена одна подзадача");
        assertEquals(subtask, manager.getSubtask(manager.getEpic(1).getSubtaskIds().get(0)), "Подзадача должна быть добавлена непосредственно к данному эпику");


        assertEquals(1, manager.getSubtasks().size(), "Одна задача должна быть добавлена");
        assertDoesNotThrow(() -> manager.deleteSubtask(42), "Метод должен выполниться корректно");
        assertEquals(1, manager.getSubtasks().size(), "Количество задач не должно измениться");
        assertSame(subtask, manager.getSubtask(2), "Задача должна остаться той же");

        assertEquals(1, manager.getSubtasks().size(), "В списке задач должна быть одна задача");
        assertDoesNotThrow(() -> manager.deleteSubtask(2), "Метод должен выполниться корректно");
        assertEquals(0, manager.getSubtasks().size(), "Список задач должен быть пуст");
        assertEquals(0,manager.getEpic(1).getSubtaskIds().size(), "У эпика не должно остаться подзадач");
    }

    @Test
    public void testAddTaskWithTime() {
        Task task = new Task("Test 1", "Testing task 1", TaskStatus.NEW, basicTestDuration,
                LocalDateTime.of(2025, 12, 20, 8, 0));
        manager.addNewTask(task);
        assertEquals(1, manager.getTasks().size(), "Должна быть добавлена одна задача");
        assertEquals(1, manager.getPrioritizedTasks().size(), "Должна быть добавлена одна задача с приоритетом");

        Task task1 = new Task("Test 2", "Testing task 2", TaskStatus.NEW, basicTestDuration);
        manager.addNewTask(task1);
        assertEquals(2, manager.getTasks().size(), "Должно быть добавлено две задача");
        assertEquals(1, manager.getPrioritizedTasks().size(), "Должна быть добавлена одна задача с приоритетом");

        Task task2 = new Task("Test 3", "Testing task 3", TaskStatus.NEW, basicTestDuration,
                LocalDateTime.of(2025, 12, 20, 8, 0));
        manager.addNewTask(task2);
        assertEquals(2, manager.getTasks().size(), "Должно быть добавлено две задача");
        assertEquals(1, manager.getPrioritizedTasks().size(), "Должна быть добавлена одна задача с приоритетом");

        Task task3 = new Task("Test 4", "Testing task 4", TaskStatus.NEW, basicTestDuration,
                LocalDateTime.of(2025, 12, 20, 8, 10));
        manager.addNewTask(task3);
        assertEquals(2, manager.getTasks().size(), "Должно быть добавлено две задача");
        assertEquals(1, manager.getPrioritizedTasks().size(), "Должна быть добавлена одна задача с приоритетом");

        Task task4 = new Task("Test 5", "Testing task 5", TaskStatus.NEW, basicTestDuration,
                LocalDateTime.of(2025, 12, 20, 8, 15));
        manager.addNewTask(task4);
        assertEquals(2, manager.getTasks().size(), "Должно быть добавлено две задача");
        assertEquals(1, manager.getPrioritizedTasks().size(), "Должна быть добавлена одна задача с приоритетом");

        Task task5 = new Task("Test 6", "Testing task 6", TaskStatus.NEW, basicTestDuration,
                LocalDateTime.of(2025, 12, 20, 8, 20));
        manager.addNewTask(task5);
        assertEquals(3, manager.getTasks().size(), "Должно быть добавлено три задачи");
        assertEquals(2, manager.getPrioritizedTasks().size(), "Должно быть добавлено две задачи с приоритетом");
    }

    @Test
    public void testAddSubtaskWithTime() {
        Epic epic = new Epic("Test 1", "Testing epic 1");
        manager.addNewEpic(epic);

        Subtask subtask = new Subtask("Test 2", "Testing subtask 1", TaskStatus.NEW, basicTestDuration,
                LocalDateTime.of(2025, 12, 20, 8, 0), 1);
        manager.addNewSubtask(subtask);
        assertEquals(1, manager.getSubtasks().size(), "Должна быть добавлена одна задача");
        assertEquals(1, manager.getPrioritizedTasks().size(), "Должна быть добавлена одна задача с приоритетом");
        assertEquals(1, manager.getEpic(1).getSubtaskIds().size(), "У эпика должна быть добавлена одна подзадача");

        Subtask subtask1 = new Subtask("Test 3", "Testing subtask 2", TaskStatus.NEW, basicTestDuration, 1);
        manager.addNewSubtask(subtask1);
        assertEquals(2, manager.getSubtasks().size(), "Должно быть добавлено две задача");
        assertEquals(1, manager.getPrioritizedTasks().size(), "Должна быть добавлена одна задача с приоритетом");
        assertEquals(2, manager.getEpic(1).getSubtaskIds().size(), "У эпика должно быть добавлено две подзадачи");

        Subtask subtask2 = new Subtask("Test 4", "Testing subtask 3", TaskStatus.NEW, basicTestDuration,
                LocalDateTime.of(2025, 12, 20, 8, 0), 1);
        manager.addNewSubtask(subtask2);
        assertEquals(2, manager.getSubtasks().size(), "Должно быть добавлено две задача");
        assertEquals(1, manager.getPrioritizedTasks().size(), "Должна быть добавлена одна задача с приоритетом");
        assertEquals(2, manager.getEpic(1).getSubtaskIds().size(), "У эпика должно быть добавлено две подзадачи");

        Subtask subtask3 = new Subtask("Test 5", "Testing subtask 4", TaskStatus.NEW, basicTestDuration,
                LocalDateTime.of(2025, 12, 20, 8, 10), 1);
        manager.addNewSubtask(subtask3);
        assertEquals(2, manager.getSubtasks().size(), "Должно быть добавлено две задача");
        assertEquals(1, manager.getPrioritizedTasks().size(), "Должна быть добавлена одна задача с приоритетом");
        assertEquals(2, manager.getEpic(1).getSubtaskIds().size(), "У эпика должно быть добавлено две подзадачи");

        Subtask subtask4 = new Subtask("Test 6", "Testing subtask 5", TaskStatus.NEW, basicTestDuration,
                LocalDateTime.of(2025, 12, 20, 8, 15), 1);
        manager.addNewSubtask(subtask4);
        assertEquals(2, manager.getSubtasks().size(), "Должно быть добавлено две задача");
        assertEquals(1, manager.getPrioritizedTasks().size(), "Должна быть добавлена одна задача с приоритетом");
        assertEquals(2, manager.getEpic(1).getSubtaskIds().size(), "У эпика должно быть добавлено две подзадачи");

        Subtask subtask5 = new Subtask("Test 7", "Testing subtask 6", TaskStatus.NEW, basicTestDuration,
                LocalDateTime.of(2025, 12, 20, 8, 20), 1);
        manager.addNewSubtask(subtask5);
        assertEquals(3, manager.getSubtasks().size(), "Должно быть добавлено три задачи");
        assertEquals(2, manager.getPrioritizedTasks().size(), "Должно быть добавлено две задачи с приоритетом");
        assertEquals(3, manager.getEpic(1).getSubtaskIds().size(), "У эпика должно быть добавлено три подзадачи");

    }

    @Test
    public void testUpdateTaskWithTime() {
        Task task = new Task("Test 1", "Testing task 1", TaskStatus.NEW, basicTestDuration,
                LocalDateTime.of(2025, 12, 20, 8, 0));
        manager.addNewTask(task);

        Task task1 = new Task(1,"Test 2", "Testing task 2", TaskStatus.NEW, basicTestDuration);
        manager.updateTask(task1);
        assertEquals(1, manager.getTasks().size(), "Не должно быть добавлено новых задач");
        assertEquals(0, manager.getPrioritizedTasks().size(), "Список задач с приоритетом должен быть пуст");
        assertSame(task1, manager.getTask(1), "Задача должна быть замена новой");
        assertNotSame(task, manager.getTask(1), "Задача должна быть замена новой");

        Task task2 = new Task(1,"Test 3", "Testing task 3", TaskStatus.NEW, basicTestDuration,
                LocalDateTime.of(2025, 12, 20, 8, 0));
        manager.updateTask(task2);
        assertEquals(1, manager.getTasks().size(), "Не должно быть добавлено новых задач");
        assertEquals(1, manager.getPrioritizedTasks().size(), "Должна быть добавлена одна задача с приоритетом");
        assertSame(task2, manager.getTask(1), "Задача должна быть замена новой");

        Task taskForUpdate = new Task("Test 2", "Testing task 2", TaskStatus.NEW, basicTestDuration);
        manager.addNewTask(taskForUpdate);
        assertEquals(2, manager.getTasks().size(), "Должно быть добавлено две задачи");
        assertEquals(1, manager.getPrioritizedTasks().size(), "Не должно быть добавлено новых задач с приоритетом");
        assertSame(taskForUpdate, manager.getTask(2), "Новая задача должна быть добавлена");

        Task task3 = new Task(2, "Test 4", "Testing task 4", TaskStatus.NEW, basicTestDuration,
                LocalDateTime.of(2025, 12, 20, 8, 10));
        manager.updateTask(task3);
        assertEquals(2, manager.getTasks().size(), "Не должно быть добавлено новых задач");
        assertEquals(1, manager.getPrioritizedTasks().size(), "Не должно быть добавлено новых задач с приоритетом");
        assertSame(task3, manager.getTask(2), "Задача должна быть замена новой");
        assertNotSame(taskForUpdate, manager.getTask(2), "Задача должна быть заменена новой");

        Task task4 = new Task(2, "Test 5", "Testing task 5", TaskStatus.NEW, basicTestDuration,
                LocalDateTime.of(2025, 12, 20, 8, 15));
        manager.updateTask(task4);
        assertEquals(2, manager.getTasks().size(), "Не должно быть добавлено новых задач");
        assertEquals(1, manager.getPrioritizedTasks().size(), "Не должно быть добавлено новых задач с приоритетом");
        assertNotSame(task4, manager.getTask(2), "Задача не должна быть замена новой");
        assertSame(task3, manager.getTask(2), "Задача не должна быть заменена новой");

        Task task5 = new Task(2, "Test 6", "Testing task 6", TaskStatus.NEW, basicTestDuration,
                LocalDateTime.of(2025, 12, 20, 8, 20));
        manager.updateTask(task5);
        assertEquals(2, manager.getTasks().size(), "Не должно быть добавлено новых задач");
        assertEquals(2, manager.getPrioritizedTasks().size(), "Должна быть добавлена вторая задача с приоритетом");
        assertNotSame(task5, manager.getTask(2), "Задача должна быть замена новой");
        assertSame(task3, manager.getTask(2), "Задача должна быть заменена новой");
    }

//    @Test
//    public void testAddSubtaskWithTime() {
//        Epic epic = new Epic("Test 1", "Testing epic 1");
//        manager.addNewEpic(epic);
//
//        Subtask subtask = new Subtask("Test 2", "Testing subtask 1", TaskStatus.NEW, basicTestDuration,
//                LocalDateTime.of(2025, 12, 20, 8, 0), 1);
//        manager.addNewSubtask(subtask);
//        assertEquals(1, manager.getSubtasks().size(), "Должна быть добавлена одна задача");
//        assertEquals(1, manager.getPrioritizedTasks().size(), "Должна быть добавлена одна задача с приоритетом");
//        assertEquals(1, manager.getEpic(1).getSubtaskIds().size(), "У эпика должна быть добавлена одна подзадача");
//
//        Subtask subtask1 = new Subtask("Test 3", "Testing subtask 2", TaskStatus.NEW, basicTestDuration, 1);
//        manager.addNewSubtask(subtask1);
//        assertEquals(2, manager.getSubtasks().size(), "Должно быть добавлено две задача");
//        assertEquals(1, manager.getPrioritizedTasks().size(), "Должна быть добавлена одна задача с приоритетом");
//        assertEquals(2, manager.getEpic(1).getSubtaskIds().size(), "У эпика должно быть добавлено две подзадачи");
//
//        Subtask subtask2 = new Subtask("Test 4", "Testing subtask 3", TaskStatus.NEW, basicTestDuration,
//                LocalDateTime.of(2025, 12, 20, 8, 0), 1);
//        manager.addNewSubtask(subtask2);
//        assertEquals(2, manager.getSubtasks().size(), "Должно быть добавлено две задача");
//        assertEquals(1, manager.getPrioritizedTasks().size(), "Должна быть добавлена одна задача с приоритетом");
//        assertEquals(2, manager.getEpic(1).getSubtaskIds().size(), "У эпика должно быть добавлено две подзадачи");
//
//        Subtask subtask3 = new Subtask("Test 5", "Testing subtask 4", TaskStatus.NEW, basicTestDuration,
//                LocalDateTime.of(2025, 12, 20, 8, 10), 1);
//        manager.addNewSubtask(subtask3);
//        assertEquals(2, manager.getSubtasks().size(), "Должно быть добавлено две задача");
//        assertEquals(1, manager.getPrioritizedTasks().size(), "Должна быть добавлена одна задача с приоритетом");
//        assertEquals(2, manager.getEpic(1).getSubtaskIds().size(), "У эпика должно быть добавлено две подзадачи");
//
//        Subtask subtask4 = new Subtask("Test 6", "Testing subtask 5", TaskStatus.NEW, basicTestDuration,
//                LocalDateTime.of(2025, 12, 20, 8, 15), 1);
//        manager.addNewSubtask(subtask4);
//        assertEquals(2, manager.getSubtasks().size(), "Должно быть добавлено две задача");
//        assertEquals(1, manager.getPrioritizedTasks().size(), "Должна быть добавлена одна задача с приоритетом");
//        assertEquals(2, manager.getEpic(1).getSubtaskIds().size(), "У эпика должно быть добавлено две подзадачи");
//
//        Subtask subtask5 = new Subtask("Test 7", "Testing subtask 6", TaskStatus.NEW, basicTestDuration,
//                LocalDateTime.of(2025, 12, 20, 8, 20), 1);
//        manager.addNewSubtask(subtask5);
//        assertEquals(3, manager.getSubtasks().size(), "Должно быть добавлено три задачи");
//        assertEquals(2, manager.getPrioritizedTasks().size(), "Должно быть добавлено две задачи с приоритетом");
//        assertEquals(3, manager.getEpic(1).getSubtaskIds().size(), "У эпика должно быть добавлено три подзадачи");
//
//    }

    @Test
    public void testUpdateEpicStatus() {
        Epic epic = new Epic("Test 1", "Testing epic 1");
        manager.addNewEpic(epic);

        assertEquals(TaskStatus.NEW, epic.getStatus(), "У эпика без подзадач должен быть статус NEW");

        Subtask subtask0 = new Subtask("Test 1", "Testing subtask 1", TaskStatus.NEW, basicTestDuration, epic.getId());
        manager.addNewSubtask(subtask0);
        assertEquals(TaskStatus.NEW, epic.getStatus(), "У эпика с одной подзадачей со статусом NEW должен быть статус NEW");

        Subtask subtask1 = new Subtask("Test 2", "Testing subtask 2", TaskStatus.NEW, basicTestDuration, epic.getId());
        manager.addNewSubtask(subtask1);
        assertEquals(TaskStatus.NEW, epic.getStatus(), "У эпика с двумя подзадачами со статусом NEW должен быть статус NEW");

        subtask1.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateSubtask(subtask1);
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus(), "У эпика с хотя бы одной подзадачей со статусом " +
                "IN_PROGRESS должен быть статус IN_PROGRESS");
        subtask0.setStatus(TaskStatus.IN_PROGRESS);
        manager.updateSubtask(subtask0);
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus(), "У эпика с хотя бы одной подзадачей со статусом " +
                "IN_PROGRESS должен быть статус IN_PROGRESS");
        subtask1.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask1);
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus(), "У эпика с хотя бы одной подзадачей со статусом " +
                "IN_PROGRESS должен быть статус IN_PROGRESS");

        subtask0.setStatus(TaskStatus.NEW);
        manager.updateSubtask(subtask0);
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus(), "У эпика, не все статусы подзадач которого равны" +
                "между собой, и среди статусов подзадач нет статуса IN_PROGRESS, должен быть статус IN_PROGRESS");

        Subtask subtask2 = new Subtask("Test 3", "Testing subtask 3", TaskStatus.DONE, basicTestDuration, epic.getId());
        manager.addNewSubtask(subtask2);
        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus(), "У эпика, не все статусы подзадач которого равны" +
                "между собой, и среди статусов подзадач нет статуса IN_PROGRESS, должен быть статус IN_PROGRESS");

        subtask0.setStatus(TaskStatus.DONE);
        manager.updateSubtask(subtask0);
        assertEquals(TaskStatus.DONE, epic.getStatus(), "У эпика, у которого все подзадачи имеют статус " +
                "DONE, должен быть статус DONE");
    }


//    @Test
//    public void checkTaskAddedInHistory() {
//        Task task = new Task("Test 1", "Testing task 1", TaskStatus.NEW);
//        manager.addNewTask(task);
//        assertEquals(1, manager.getTasks().size(), "task should be added");
//        manager.getTask(1);
//        assertTrue(manager.getHistory().contains(task), "task should be added in history");
//    }
//
//    @Test
//    public void checkTaskRemovedFromHistory() {
//        Task task = new Task("Test 1", "Testing task 1", TaskStatus.NEW);
//        manager.addNewTask(task);
//        assertEquals(1, manager.getTasks().size(), "task should be added");
//        manager.getTask(1);
//        assertTrue(manager.getHistory().contains(task), "task should be added in history");
//        manager.deleteTask(1);
//        assertEquals(0, manager.getTasks().size(), "task should be removed");
//        assertFalse(manager.getHistory().contains(task), "task should be removed from history");
//    }
//
//    @Test
//    public void checkHistoryLimitIsOver10() {
//        for (int i = 0; i < 11; i++) {
//            Task task = new Task("Test 1", "Testing task 1", TaskStatus.NEW);
//            manager.addNewTask(task);
//            assertEquals(i + 1, manager.getTasks().size(), "task should be added");
//            manager.getTask(i + 1);
//            assertEquals(i + 1, manager.getHistory().size(), "task should be added in history");
//        }
//    }

}
