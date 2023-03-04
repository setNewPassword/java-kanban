package model;

import main.java.manager.Managers;
import main.java.manager.interfaces.TaskManager;
import main.java.model.Epic;
import main.java.model.Status;
import main.java.model.SubTask;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class EpicTest {

    private final File file = new File("src/main.java.manager/implementation/Testing.csv");
    private TaskManager taskManager;

    @BeforeEach
    public void createNewFileBackedTasksManager() {
        taskManager = Managers.getDefaultFBTM();
    }

    @AfterEach
    public void deleteFile() {
        file.delete();
    }

    @Test
    public void statusShouldBeNewWhenEpicHaveNoSubtasks() {
        Epic epic1 = new Epic("Заголовок эпика", "Текст описания эпика");
        int epic1id = taskManager.addEpic(epic1);

        Status epicsStatus = taskManager.getEpic(epic1id).getTaskStatus();

        assertEquals(epicsStatus, Status.NEW, "Статус эпика не соответствует ожидаемому.");
    }

    @Test
    public void statusShouldBeNewWhenAllSubtasksHaveStatusNew() {
        Epic epic1 = new Epic("Заголовок эпика", "Текст описания эпика");
        int epic1id = taskManager.addEpic(epic1);
        SubTask subTask1 = new SubTask("Заг сабтаска-1", "Текст сабтаска-1", epic1id);
        SubTask subTask2 = new SubTask("Заг сабтаска-2", "Текст сабтаска-2", epic1id);
        taskManager.addSubTask(subTask1);
        taskManager.addSubTask(subTask2);

        Status epicsStatus = taskManager.getEpic(epic1id).getTaskStatus();

        assertEquals(epicsStatus, Status.NEW, "Статус эпика не соответствует ожидаемому.");
    }

    @Test
    public void statusShouldBeDoneWhenAllSubtasksHaveStatusDone() {
        Epic epic1 = new Epic("Заголовок эпика", "Текст описания эпика");
        int epic1id = taskManager.addEpic(epic1);
        SubTask subTask1 = new SubTask("Заг сабтаска-1", "Текст сабтаска-1", epic1id);
        SubTask subTask2 = new SubTask("Заг сабтаска-2", "Текст сабтаска-2", epic1id);
        taskManager.addSubTask(subTask1);
        taskManager.addSubTask(subTask2);
        taskManager.getEpic(epic1id).getSubTasksID()
                .forEach(key -> taskManager.updateStatusSubTask(key, Status.DONE));

        Status epicStatus = taskManager.getEpic(epic1id).getTaskStatus();

        assertEquals(epicStatus, Status.DONE, "Статус эпика не соответствует ожидаемому.");
    }

    @Test
    public void statusShouldBeInProgressWhenSubtasksHaveStatusNewAndDone() {
        Epic epic1 = new Epic("Заголовок эпика", "Текст описания эпика");
        int epic1id = taskManager.addEpic(epic1);
        SubTask subTask1 = new SubTask("Заг сабтаска-1", "Текст сабтаска-1", epic1id);
        SubTask subTask2 = new SubTask("Заг сабтаска-2", "Текст сабтаска-2", epic1id);
        int subTask1id = taskManager.addSubTask(subTask1);
        int subTask2id = taskManager.addSubTask(subTask2);
        taskManager.updateStatusSubTask(subTask2id, Status.DONE);

        Status epicStatus = taskManager.getEpic(epic1id).getTaskStatus();
        Status subTask1Status = taskManager.getSubTask(subTask1id).getTaskStatus();
        Status subTask2Status = taskManager.getSubTask(subTask2id).getTaskStatus();

        assertEquals(subTask1Status, Status.NEW);
        assertEquals(subTask2Status, Status.DONE);
        assertEquals(epicStatus, Status.IN_PROGRESS, "Статус эпика не соответствует ожидаемому.");
    }

    @Test
    public void statusShouldBeInProgressWhenAllSubtasksHaveStatusInProgress() {
        Epic epic1 = new Epic("Заголовок эпика", "Текст описания эпика");
        int epic1id = taskManager.addEpic(epic1);
        SubTask subTask1 = new SubTask("Заг сабтаска-1", "Текст сабтаска-1", epic1id);
        SubTask subTask2 = new SubTask("Заг сабтаска-2", "Текст сабтаска-2", epic1id);
        taskManager.addSubTask(subTask1);
        taskManager.addSubTask(subTask2);
        taskManager.getEpic(epic1id).getSubTasksID()
                .forEach(key -> taskManager.updateStatusSubTask(key, Status.IN_PROGRESS));

        Status epicStatus = taskManager.getEpic(epic1.getId()).getTaskStatus();

        assertEquals(epicStatus, Status.IN_PROGRESS, "Статус эпика не соответствует ожидаемому.");
    }

}