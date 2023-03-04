package implementation;

import main.java.manager.Managers;
import main.java.manager.implementation.HttpTaskManager;
import main.java.manager.servers.KVServer;
import main.java.model.SubTask;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpTaskManagerTest extends TaskManagerTest<HttpTaskManager> {

    KVServer kvServer;

    @Override
    HttpTaskManager createTaskManager() throws IOException, InterruptedException {
        try {
            kvServer = new KVServer();
            kvServer.start();
        } catch (IOException e) {
            System.out.println("Ошибка запуска KVServer.");
        }
        return (HttpTaskManager) Managers.getDefault();
    }

    @AfterEach
    public void stopServer() {
        kvServer.stop();
    }

    @Test
    void shouldSaveAndRestoreManagerWithEmptyHistory() throws IOException, InterruptedException {
        taskManager.addTask(task1);
        taskManager.addTask(task2);
        int epic1id = taskManager.addEpic(epic1);
        SubTask subTask1 = new SubTask("Title for SubTask-1", "Description for SubTask-1",
                epic1id, dateTime3, duration10);
        SubTask subTask2 = new SubTask("Title for SubTask-2", "Description for SubTask-2",
                epic1id, dateTime4, duration20);
        taskManager.addSubTask(subTask1);
        taskManager.addSubTask(subTask2);

        HttpTaskManager restoredManager = new HttpTaskManager(URI.create("http://localhost:8078/"));
        restoredManager.getKvTaskClient().setApiToken(taskManager.getKvTaskClient().getApiToken());

        assertEquals(2, restoredManager.getTasks().size(), "Число тасков отличается от исходного.");
        assertEquals(1, restoredManager.getEpics().size(), "Число эпиков отличается от исходного.");
        assertEquals(2, restoredManager.getSubTasks().size(), "Число сабТасков отличается от исходного.");
    }

    @Test
    void shouldReturnEmptyHistory() throws IOException, InterruptedException {
        taskManager.addTask(task1);
        taskManager.addTask(task2);
        int epic1id = taskManager.addEpic(epic1);
        SubTask subTask1 = new SubTask("Title for SubTask-1", "Description for SubTask-1",
                epic1id, dateTime3, duration10);
        SubTask subTask2 = new SubTask("Title for SubTask-2", "Description for SubTask-2",
                epic1id, dateTime4, duration20);
        taskManager.addSubTask(subTask1);
        taskManager.addSubTask(subTask2);

        HttpTaskManager restoredManager = new HttpTaskManager(URI.create("http://localhost:8078/"));
        restoredManager.getKvTaskClient().setApiToken(taskManager.getKvTaskClient().getApiToken());

        assertEquals(0, taskManager.getHistory().getHistory().size(),
                "Размер списка истории у исходного менеджера отличается от нуля.");
        assertEquals(0, restoredManager.getHistory().getHistory().size(),
                "Размер списка истории у восстановленного менеджера отличается от нуля.");
    }

    @Test
    void shouldSaveAndRestoreManagerWithExistingHistory() throws IOException, InterruptedException {
        int task1id = taskManager.addTask(task1);
        int task2id = taskManager.addTask(task2);
        int epic1id = taskManager.addEpic(epic1);
        SubTask subTask1 = new SubTask("Title for SubTask-1", "Description for SubTask-1",
                epic1id, dateTime3, duration10);
        SubTask subTask2 = new SubTask("Title for SubTask-2", "Description for SubTask-2",
                epic1id, dateTime4, duration20);
        final int subTask1id = taskManager.addSubTask(subTask1);
        final int subTask2id = taskManager.addSubTask(subTask2);
        taskManager.getSubTask(subTask2id);
        taskManager.getSubTask(subTask1id);
        taskManager.getEpic(epic1id);
        taskManager.getTask(task2id);
        taskManager.getTask(task1id);
        HttpTaskManager restoredManager = new HttpTaskManager(URI.create("http://localhost:8078/"));
        restoredManager.getKvTaskClient().setApiToken(taskManager.getKvTaskClient().getApiToken());


        assertEquals(5, restoredManager.getHistory().getHistory().size(),
                "Размер списка истории у восстановленного менеджера отличается от ожидаемого.");
        assertEquals(subTask2id, restoredManager.getHistory().getHistory().get(0).getId(),
                "Ошибка сохранения истории: нарушен порядок задач.");
        assertEquals(task1id, restoredManager.getHistory().getHistory()
                        .get(restoredManager.getHistory().getHistory().size() - 1).getId(),
                "Ошибка сохранения истории: нарушен порядок задач.");
    }
}
