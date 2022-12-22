package manager;

import manager.interfaces.HistoryManager;
import manager.interfaces.TaskManager;

public abstract class Managers implements TaskManager {
    private static TaskManager taskManager;
    private static InMemoryHistoryManager historyManager;

    public TaskManager getDefault(){
        return taskManager;
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

}
