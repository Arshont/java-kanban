package ru.yandex.javacourse.schedule.manager;

/**
 * Default managers.
 *
 * @author Vladimir Ivanov (ivanov.vladimir.l@gmail.com)
 */
public class Managers {
    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static TaskManager getFileManager(String filename) {
        return new FileBackedTaskManager(filename);
    }

    public static TaskManager getAndRestoreFileManager(String filename, String filenameForRestore) {
        return new FileBackedTaskManager(filename, filenameForRestore);
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
