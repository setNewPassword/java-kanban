package implementation;

import main.java.manager.interfaces.TaskManager;

import static main.java.manager.Managers.getDefaultInMemoryTaskManager;

public class InMemoryTaskManagerTest extends TaskManagerTest<TaskManager> {
    @Override
    TaskManager createTaskManager() {
        return getDefaultInMemoryTaskManager();
    }
}
