package manager;

import manager.implementation.FileBackedTasksManager;
import manager.implementation.InMemoryHistoryManager;
import manager.implementation.InMemoryTaskManager;
import manager.interfaces.HistoryManager;
import manager.interfaces.TaskManager;

import java.io.File;

public class Managers {

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    public static FileBackedTasksManager getDefaultFBTM() {
        return new FileBackedTasksManager(new File("src/test/Testing.csv"));
    }

}
