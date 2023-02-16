package main.java.manager;

import main.java.manager.implementation.FileBackedTasksManager;
import main.java.manager.implementation.InMemoryHistoryManager;
import main.java.manager.implementation.InMemoryTaskManager;
import main.java.manager.interfaces.HistoryManager;
import main.java.manager.interfaces.TaskManager;

import java.io.File;

public class Managers {

    public static TaskManager getDefault() {
        return new InMemoryTaskManager();
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    public static FileBackedTasksManager getDefaultFBTM() {
        return new FileBackedTasksManager(new File("src/main/java/manager/implementation/Testing.csv"));
    }

}
