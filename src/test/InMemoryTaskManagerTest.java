package test;

import manager.interfaces.TaskManager;

import static manager.Managers.getDefault;

public class InMemoryTaskManagerTest extends TaskManagerTest<TaskManager> {
    @Override
    TaskManager createTaskManager() {
        return getDefault();
    }
}
