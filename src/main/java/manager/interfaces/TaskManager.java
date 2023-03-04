package main.java.manager.interfaces;

import main.java.model.Epic;
import main.java.model.Status;
import main.java.model.SubTask;
import main.java.model.Task;

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

    int updateTask(Task task);

    int updateEpic(Epic epic);

    int updateSubTask(SubTask subTask);

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