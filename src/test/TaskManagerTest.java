package test;

import manager.interfaces.HistoryManager;
import manager.interfaces.TaskManager;
import model.Epic;
import model.SubTask;
import model.Task;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

abstract class TaskManagerTest<T extends TaskManager> {
    public T taskManager;

    abstract T createTaskManager();

    protected LocalDateTime dateTime1 = LocalDateTime.of(2023, 2, 12, 11, 30);
    protected LocalDateTime dateTime2 = LocalDateTime.of(2023, 2, 12, 11, 45);
    protected LocalDateTime dateTime3 = LocalDateTime.of(2023, 2, 12, 12, 30);
    protected LocalDateTime dateTime4 = LocalDateTime.of(2023, 2, 12, 13, 45);
    protected Duration duration10 = Duration.ofMinutes(10);
    protected Duration duration20 = Duration.ofMinutes(20);
    protected Duration duration30 = Duration.ofMinutes(30);
    protected Task task1 = new Task("Title for Task-1", "Description for Task-1", dateTime1, duration10);
    protected Task task1Long = new Task("Title for long Task",
            "Description for long Task", dateTime1, duration30);
    protected Task task2 = new Task("Title for Task-2", "Description for Task-2", dateTime2, duration20);
    protected Epic epic1 = new Epic("Title of Epic-1", "Description for Epic-1");

    @BeforeEach
    public void updateTaskManager() {
        taskManager = createTaskManager();
    }


    @Test
    void addTask() {
        final int id = taskManager.addTask(task1);

        final Task actualTask = taskManager.getTask(id);

        assertNotNull(actualTask, "Задача не найдена.");
        assertEquals(task1, actualTask, "Задачи не совпадают.");

        final Map<Integer, Task> tasks = taskManager.getTasks();

        assertNotNull(tasks, "Задачи на возвращаются.");
        assertEquals(1, tasks.size(), "Неверное количество задач.");
        assertEquals(task1, tasks.get(id), "Задачи не совпадают.");
        assertEquals("Title for Task-1", tasks.get(id).getTitle(), "Заголовки не совпадают.");
        assertEquals("Description for Task-1", tasks.get(id).getExtraInfo(), "Описания не совпадают.");
        assertEquals(dateTime1, tasks.get(id).getStartTime(), "Время старта не совпадает.");
        assertEquals(duration10, tasks.get(id).getDuration(), "Продолжительность не совпадает.");
        assertEquals(dateTime1.plus(duration10), tasks.get(id).getEndTime().
                        orElse(LocalDateTime.of(0, 1, 1, 0, 0)),
                "Время окончания рассчитывается не верно.");
    }

    @Test
    void addEpic() {
        final int id = taskManager.addEpic(epic1);
        final Epic actualEpic = taskManager.getEpic(id);

        assertNotNull(actualEpic, "Эпик не найден.");
        assertEquals(epic1, actualEpic, "Эпики не совпадают.");

        final Map<Integer, Epic> epics = taskManager.getEpics();

        assertNotNull(epics, "Эпики на возвращаются.");
        assertEquals(1, epics.size(), "Неверное количество эпиков.");
        assertEquals(epic1, epics.get(id), "Эпики не совпадают.");
    }

    @Test
    void addSubTask() {
        final int epicID = taskManager.addEpic(epic1);
        SubTask subTask1 = new SubTask("Title for SubTask-1", "Description for SubTask-1",
                epicID, dateTime1, duration10);
        SubTask subTask2 = new SubTask("Title for SubTask-2", "Description for SubTask-2",
                epicID, dateTime2, duration20);
        final int subTask1id = taskManager.addSubTask(subTask1);
        final int subTask2id = taskManager.addSubTask(subTask2);
        Epic actualEpic = taskManager.getEpic(epicID);
        SubTask actualSubTask1 = taskManager.getSubTask(subTask1id);
        SubTask actualSubTask2 = taskManager.getSubTask(subTask2id);

        assertNotNull(actualEpic, "Эпик не найден.");
        assertNotNull(actualSubTask1, "СабТаск-1 не найден.");
        assertNotNull(actualSubTask2, "СабТаск-2 не найден.");
        assertEquals(actualEpic, taskManager.getEpic(epicID), "Эпики не совпадают.");
        assertEquals(actualSubTask1, taskManager.getSubTask(subTask1id), "СабТаски-1 не совпадают.");
        assertEquals(actualSubTask2, taskManager.getSubTask(subTask2id), "СабТаски-2 не совпадают.");

        final Map<Integer, Epic> actualEpics = taskManager.getEpics();
        final Map<Integer, SubTask> actualSubTasks = taskManager.getSubTasks();

        assertNotNull(actualEpics, "Эпики на возвращаются.");
        assertNotNull(actualSubTasks, "Сабтаски на возвращаются.");
        assertEquals(1, actualEpics.size(), "Неверное количество эпиков.");
        assertEquals(actualEpic, actualEpics.get(epicID), "Эпики не совпадают.");
        assertEquals(2, actualSubTasks.size(), "Неверное количество сабТасков.");
        assertEquals(subTask2, actualSubTasks.get(subTask2id), "СабТаски не совпадают.");

        assertEquals(dateTime1, actualSubTasks.get(subTask1id).getStartTime(), "Ошибка в startTime сабТаска-1.");
        assertEquals(dateTime2.plus(duration20),
                actualSubTasks.get(subTask2id).getEndTime()
                        .orElse(LocalDateTime.of(0, 1, 1, 0, 0)),
                "Ошибка в endTime сабТаска-2.");
        assertEquals(dateTime1, actualEpics.get(epicID).getStartTime(), "Ошибка в startTime эпика.");
        assertEquals(dateTime2.plus(duration20),
                actualEpics.get(epicID).getEndTime()
                        .orElse(LocalDateTime.of(0, 1, 1, 0, 0)),
                "Ошибка в endTime эпика.");
    }

    @Test
    public void shouldThrowExceptionWhenAddSubTaskWithoutEpic() {
        RuntimeException ex = Assertions.assertThrows(
                RuntimeException.class, () -> {
                    final int epicID = taskManager.addEpic(epic1);
                    SubTask subTask1 = new SubTask("Title for SubTask-1", "Description for SubTask-1",
                            (epicID + 5), dateTime1, duration10);
                    taskManager.addSubTask(subTask1);
                }
        );

        assertEquals("Ошибка: эпик отсутствует!", ex.getMessage());
    }

    @Test
    void ShouldReturnTaskAndWriteItToHistory() {
        final int task1ID = taskManager.addTask(task1);
        Task actualTask = taskManager.getTask(task1ID);
        assertNotNull(actualTask, "Ошибка возвращения таска.");
        assertEquals(task1ID, taskManager.getHistory().getHistory().get(0).getId(), "Ошибка менеджера истории.");
    }

    @Test
    void ShouldThrowExceptionWhenWrongTaskId() {
        RuntimeException ex = Assertions.assertThrows(
                RuntimeException.class, () -> {
                    final int task1ID = taskManager.addTask(task1);
                    Task actualTask = taskManager.getTask(task1ID + 1);
                }
        );

        assertEquals("Ошибка: нет таска с таким id!", ex.getMessage());
    }

    @Test
    void ShouldReturnEpicAndWriteItToHistory() {
        final int epic1ID = taskManager.addEpic(epic1);
        Epic actualEpic = taskManager.getEpic(epic1ID);
        assertNotNull(actualEpic, "Ошибка возвращения таска.");
        assertEquals(epic1ID, taskManager.getHistory().getHistory().get(0).getId(), "Ошибка менеджера истории.");
    }

    @Test
    void ShouldThrowExceptionWhenWrongEpicId() {
        RuntimeException ex = Assertions.assertThrows(
                RuntimeException.class, () -> {
                    final int epic1ID = taskManager.addTask(epic1);
                    Epic actualEpic = taskManager.getEpic(epic1ID + 1);
                }
        );

        assertEquals("Ошибка: нет эпика с таким id!", ex.getMessage());
    }

    @Test
    void ShouldReturnSubTaskAndWriteItToHistory() {
        final int epic1ID = taskManager.addEpic(epic1);
        SubTask subTask1 = new SubTask("Title for SubTask-1", "Description for SubTask-1",
                epic1ID, dateTime1, duration10);
        final int subTask1ID = taskManager.addSubTask(subTask1);
        SubTask actualSubTask = taskManager.getSubTask(subTask1ID);
        assertNotNull(actualSubTask, "Ошибка возвращения таска.");
        assertEquals(subTask1ID, taskManager.getHistory().getHistory().get(0).getId(), "Ошибка менеджера истории.");
    }

    @Test
    void ShouldThrowExceptionWhenWrongSubTaskId() {
        RuntimeException ex = Assertions.assertThrows(
                RuntimeException.class, () -> {
                    final int epic1ID = taskManager.addEpic(epic1);
                    SubTask subTask1 = new SubTask("Title for SubTask-1", "Description for SubTask-1",
                            epic1ID, dateTime1, duration10);
                    final int subTask1ID = taskManager.addSubTask(subTask1);
                    SubTask actualSubTask = taskManager.getSubTask(subTask1ID + 1);
                }
        );

        assertEquals("Ошибка: нет сабтаска с таким id!", ex.getMessage());
    }

    @Test
    void ShouldClearTaskListAndRemoveThemFromHistory() {
        final int task1ID = taskManager.addTask(task1);
        Task actualTask = taskManager.getTask(task1ID);
        taskManager.clearTasks();
        assertEquals(0, taskManager.getTasks().size(), "Ошибка очистки списка тасков.");
        assertFalse(taskManager.getHistory().getHistory().contains(actualTask), "Ошибка удаления таска из истории.");
    }

    @Test
    void ShouldClearEpicsAndSubtaskListAndRemoveThemFromHistory() {
        final int epic1ID = taskManager.addEpic(epic1);
        SubTask subTask1 = new SubTask("Title for SubTask-1", "Description for SubTask-1",
                epic1ID, dateTime1, duration10);
        final int subTask1ID = taskManager.addSubTask(subTask1);
        Epic actualEpic = taskManager.getEpic(epic1ID);
        SubTask actualSubTask = taskManager.getSubTask(subTask1ID);
        taskManager.clearEpics();
        assertEquals(0, taskManager.getEpics().size(), "Ошибка очистки списка Эпиков.");
        assertEquals(0, taskManager.getSubTasks().size(), "Ошибка очистки списка сабТасков.");
        assertFalse(taskManager.getHistory().getHistory().contains(actualEpic),
                "Ошибка удаления эпика из истории.");
        assertFalse(taskManager.getHistory().getHistory().contains(actualSubTask),
                "Ошибка удаления сабТаска из истории.");
    }

    @Test
    void ShouldClearSubTaskListAndRemoveThemFromHistory() {
        final int epic1ID = taskManager.addEpic(epic1);
        SubTask subTask1 = new SubTask("Title for SubTask-1", "Description for SubTask-1",
                epic1ID, dateTime1, duration10);
        final int subTask1ID = taskManager.addSubTask(subTask1);
        SubTask actualSubTask = taskManager.getSubTask(subTask1ID);
        taskManager.clearSubTasks();
        assertEquals(0, taskManager.getSubTasks().size(), "Ошибка очистки списка сабТасков");
        assertFalse(taskManager.getHistory().getHistory().contains(actualSubTask),
                "Ошибка удаления сабТаска из истории.");
    }

    @Test
    void ShouldReturnListSubtasksOfEpic() {
        final int epicID = taskManager.addEpic(epic1);
        SubTask subTask1 = new SubTask("Title for SubTask-1", "Description for SubTask-1",
                epicID, dateTime1, duration10);
        SubTask subTask2 = new SubTask("Title for SubTask-2", "Description for SubTask-2",
                epicID, dateTime2, duration20);
        final int subTask1id = taskManager.addSubTask(subTask1);
        final int subTask2id = taskManager.addSubTask(subTask2);
        List<Integer> actualSubTasksOfEpic = taskManager.getEpic(epicID).getSubTasksID();
        assertNotNull(actualSubTasksOfEpic, "Ошибка возвращения списка сабТасков эпика.");
        assertEquals(2, actualSubTasksOfEpic.size(), "Неверный размер списка сабТасков эпика.");
        assertTrue(actualSubTasksOfEpic.contains(subTask1id), "Ошибка содержимого списка сабТасков эпика.");
    }

    @Test
    void ShouldRemoveTaskAndRemoveItFromHistory() {
        final int task1ID = taskManager.addTask(task1);
        Task actualTask = taskManager.getTask(task1ID);
        taskManager.removeTask(task1ID);
        assertThrows(RuntimeException.class, () -> { taskManager.getTask(task1ID); },
                "Ошибка удаления таска по айди.");
        assertFalse(taskManager.getHistory().getHistory().contains(actualTask), "Ошибка удаления таска из истории.");
    }

    @Test
    void ShouldRemoveEpicAndAllItsSubtasksAndRemoveThemFromHistory() {
        final int epicID = taskManager.addEpic(epic1);
        SubTask subTask1 = new SubTask("Title for SubTask-1", "Description for SubTask-1",
                epicID, dateTime1, duration10);
        final int subTask1id = taskManager.addSubTask(subTask1);
        Epic actualEpic = taskManager.getEpic(epicID);
        SubTask actualSubTask1 = taskManager.getSubTask(subTask1id);
        taskManager.removeEpic(epicID);
        assertThrows(RuntimeException.class, () -> { taskManager.getEpic(epicID); },
                "Ошибка удаления эпика по айди.");
        assertFalse(taskManager.getHistory().getHistory().contains(actualEpic),
                "Ошибка удаления таска из истории.");
        assertThrows(RuntimeException.class, () -> { taskManager.getSubTask(subTask1id); },
                "Ошибка удаления сабТаска при удалении родительского эпика.");
        assertFalse(taskManager.getHistory().getHistory().contains(actualSubTask1),
                "Ошибка удаления сабТаска из истории при удалении родительского эпика.");
    }

    @Test
    void ShouldRemoveSubtaskAndRemoveItFromHistory() {
        final int epicID = taskManager.addEpic(epic1);
        SubTask subTask1 = new SubTask("Title for SubTask-1", "Description for SubTask-1",
                epicID, dateTime1, duration10);
        final int subTask1id = taskManager.addSubTask(subTask1);
        Epic actualEpic = taskManager.getEpic(epicID);
        SubTask actualSubTask1 = taskManager.getSubTask(subTask1id);
        taskManager.removeSubTask(subTask1id);
        assertThrows(RuntimeException.class, () -> { taskManager.getSubTask(subTask1id); },
                "Ошибка удаления сабТаска по айди.");
        assertFalse(taskManager.getHistory().getHistory().contains(actualSubTask1),
                "Ошибка удаления сабТаска из истории.");
    }

    @Test
    void ShouldReturnHistoryManager() {
        final int task1ID = taskManager.addTask(task1);
        Task actualTask = taskManager.getTask(task1ID);
        HistoryManager actualHistoryManager = taskManager.getHistory();
        assertNotNull(actualHistoryManager, "Ошибка возвращения менеджера истории.");
    }

    @Test
    void ShouldAddTasksToTreeSetWhenNoTimeIntersection() {
        final int task2id = taskManager.addTask(task2);
        final int task3id = taskManager.addTask(new Task("Title for task without startTime and duration",
                "Description for task without startTime and duration"));
        final int task1id = taskManager.addTask(task1);
        Task actualTask1 = taskManager.getTask(task1id);
        Task actualTask2 = taskManager.getTask(task2id);
        Task actualTask3 = taskManager.getTask(task3id);
        List<Task> prioritizedList = taskManager.getPrioritizedTasks();
        assertNotNull(prioritizedList, "Ошибка возвращения списка тасков в порядке времени.");
        assertEquals(3, prioritizedList.size(), "Размер списка задач не соответствует ожидаемому.");
        assertEquals(actualTask1, prioritizedList.get(0), "На первом месте не самый ранний по времени таск.");
        assertEquals(actualTask3, prioritizedList.get(2), "На последнем месте не таск без времени старта.");
    }

    @Test
    void ShouldThrowExceptionWhenTimeIntersection() {
        RuntimeException ex = Assertions.assertThrows(
                RuntimeException.class, () -> {
                    final int task1LongId = taskManager.addTask(task1Long);
                    final int task2id = taskManager.addTask(task2);
                }
        );

        assertEquals("Произошло пересечение задач по времени. Добавленная задача будет удалена.",
                ex.getMessage());
    }
}
