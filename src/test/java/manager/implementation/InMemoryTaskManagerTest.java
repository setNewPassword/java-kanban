package test.java.manager.implementation;

import main.java.manager.interfaces.TaskManager;

import static main.java.manager.Managers.getDefault;

public class InMemoryTaskManagerTest extends TaskManagerTest<TaskManager> {
    @Override
    TaskManager createTaskManager() {
        return getDefault();
    }
}
