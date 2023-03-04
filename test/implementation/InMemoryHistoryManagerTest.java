package implementation;

import main.java.manager.implementation.InMemoryHistoryManager;
import main.java.manager.interfaces.HistoryManager;
import main.java.model.Epic;
import main.java.model.SubTask;
import main.java.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class InMemoryHistoryManagerTest {

    public HistoryManager historyManager;
    protected LocalDateTime dateTime1 = LocalDateTime.of(2023, 2, 12, 11, 30);
    protected LocalDateTime dateTime2 = LocalDateTime.of(2023, 2, 12, 11, 45);
    protected LocalDateTime dateTime3 = LocalDateTime.of(2023, 2, 12, 12, 30);
    protected Duration duration10 = Duration.ofMinutes(10);
    protected Duration duration20 = Duration.ofMinutes(20);
    protected Duration duration30 = Duration.ofMinutes(30);
    protected Task task1 = new Task(0, "Title for Task-1", "Description for Task-1", dateTime1, duration10);
    protected Task task2 = new Task(1, "Title for Task-2", "Description for Task-2", dateTime2, duration20);
    protected Epic epic1 = new Epic(2, "Title of Epic-1", "Description for Epic-1");
    protected SubTask subTask1 = new SubTask(3, "Title of SubTask-1",
            "Description for SubTask-1", 2, dateTime3, duration30);

    @BeforeEach
    public void updateHistoryManager() {
        historyManager = new InMemoryHistoryManager();
    }


    @Test
    void shouldAddAllTypesOfTasks() {
        historyManager.add(task1);
        historyManager.add(epic1);
        historyManager.add(subTask1);

        assertEquals(3, historyManager.getHistory().size(), "Ошибка добавления задач.");
    }

    @Test
    void shouldRemoveAllTypesOfTasks() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(epic1);
        historyManager.add(subTask1);

        historyManager.remove(0);
        historyManager.remove(2);
        historyManager.remove(3);

        assertEquals(1, historyManager.getHistory().size(), "Ошибка удаления задач.");
    }

    @Test
    void shouldReturnListOfTasks() {
        historyManager.add(task1);
        historyManager.add(task2);
        historyManager.add(epic1);
        historyManager.add(subTask1);

        List<Task> actualHistory = historyManager.getHistory();

        assertEquals(4, actualHistory.size(), "Ошибка возвращения списка задач.");
    }

}
