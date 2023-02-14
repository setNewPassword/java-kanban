package manager.interfaces;

import model.Epic;
import model.Status;
import model.SubTask;
import model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface TaskManager {

    int addTask(Task task);

    int addEpic(Epic epic);

    int addSubTask(SubTask subTask);

    Task getTask(int id);

    Epic getEpic(int id);

    SubTask getSubTask(int id);

    HashMap<Integer, Task> getTasks();

    HashMap<Integer, Epic> getEpics();

    HashMap<Integer, SubTask> getSubTasks();

    void clearTasks();

    void clearEpics();

     void clearSubTasks();

    ArrayList<Integer> getSubTaskList(int epicID);

    void updateStatusEpic(int id);

    void removeTask(int id);

    void removeSubTask(int id);

    void removeEpic(int id);

    void updateStatusSubTask(int id, Status subTaskStatus);

    HistoryManager getHistory();

    List<Task> getPrioritizedTasks();

}