package test;

import manager.implementation.FileBackedTasksManager;
import model.Epic;
import model.SubTask;
import model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

import static manager.Managers.getDefaultFBTM;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class FileBackedTasksManagerTest extends TaskManagerTest<FileBackedTasksManager>{

    @Override
    FileBackedTasksManager createTaskManager() {
        return getDefaultFBTM();
    }

    @AfterEach
    public void deleteCSV() {
        File tmp = new File("src/manager/implementation/Testing.csv");
        try (RandomAccessFile raf = new RandomAccessFile(tmp, "rw")) {
            if (tmp.exists()) {
                raf.setLength(0);
            }
        } catch (IOException exception) {
            System.out.println("Ошибка при очистке файла между тестами.");
        }
    }

    @Test
    void shouldSaveAndRestoreManager() {
        int task1id = taskManager.addTask(task1);
        int task2id = taskManager.addTask(task2);
        int epic1id = taskManager.addEpic(epic1);
        SubTask subTask1 = new SubTask("Title for SubTask-1", "Description for SubTask-1",
                epic1id, dateTime3, duration10);
        SubTask subTask2 = new SubTask("Title for SubTask-2", "Description for SubTask-2",
                epic1id, dateTime4, duration20);
        final int subTask1id = taskManager.addSubTask(subTask1);
        final int subTask2id = taskManager.addSubTask(subTask2);
        SubTask actualSubTask2 = taskManager.getSubTask(subTask2id);
        SubTask actualSubTask1 = taskManager.getSubTask(subTask1id);
        Epic actualEpic1 = taskManager.getEpic(epic1id);
        Task actualTask2 = taskManager.getTask(task2id);
        Task actualTask1 = taskManager.getTask(task1id);
        FileBackedTasksManager restoredManager = FileBackedTasksManager
                .loadFromFile(new File("src/manager/implementation/Testing.csv"));

        assertEquals(2, restoredManager.getTasks().size(), "Число тасков отличается от исходного.");
        assertEquals(1, restoredManager.getEpics().size(), "Число эпиков отличается от исходного.");
        assertEquals(2, restoredManager.getSubTasks().size(), "Число сабТасков отличается от исходного.");

        assertEquals(subTask2id, restoredManager.getHistory().getHistory().get(0).getId(),
                "Ошибка сохранения истории: нарушен порядок задач.");
        assertEquals(task1id, restoredManager.getHistory().getHistory()
                        .get(restoredManager.getHistory().getHistory().size() - 1).getId(),
                "Ошибка сохранения истории: нарушен порядок задач.");
    }
}
