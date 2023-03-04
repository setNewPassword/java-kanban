package main.java.manager.implementation;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import main.java.manager.Managers;
import main.java.manager.servers.KVTaskClient;
import main.java.model.Epic;
import main.java.model.SubTask;
import main.java.model.Task;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;

public class HttpTaskManager extends FileBackedTasksManager {

    private final KVTaskClient kvTaskClient;


    private final Gson gson = Managers.getGson();

    public HttpTaskManager(URI uri) throws IOException, InterruptedException {
        super(uri);
        kvTaskClient = new KVTaskClient(uri);

        try {
            loadByKey();
        } catch (IOException | InterruptedException exception) {
            exception.printStackTrace();
        }
    }

    @Override
    public void save() {
        try {
            kvTaskClient.put("Task", gson.toJson(getTasks()));
            kvTaskClient.put("Epic", gson.toJson(getEpics()));
            kvTaskClient.put("SubTask", gson.toJson(getSubTasks()));
            kvTaskClient.put("History", gson.toJson(getHistory().getHistory()));
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException();
        }
    }

    public void loadByKey() throws IOException, InterruptedException {
        Map<Integer, Task> loadedTasks = gson.fromJson(
                kvTaskClient.load("Task"), new TypeToken<Map<Integer, Task>>() {}.getType());
        Map<Integer, Epic> loadedEpics = gson.fromJson(
                kvTaskClient.load("Epic"), new TypeToken<Map<Integer, Epic>>() {}.getType());
        Map<Integer, SubTask> loadedSubTasks = gson.fromJson(
                kvTaskClient.load("SubTask"), new TypeToken<Map<Integer, SubTask>>() {}.getType());
        List<Task> loadedHistory = gson.fromJson(
                kvTaskClient.load("History"), new TypeToken<List<Task>>() {}.getType());
        if(loadedTasks != null) {
            for (Map.Entry<Integer, Task> task : loadedTasks.entrySet()) {
                getTasks().put(task.getValue().getId(), task.getValue());
                try {
                    compareTasksByTimeAndAddToTreeSet(task.getValue());
                } catch (RuntimeException exception) {
                    System.out.println(exception.getMessage());
                }
            }
        }
        if(loadedEpics != null) {
            for (Map.Entry<Integer, Epic> epic : loadedEpics.entrySet()) {
                getEpics().put(epic.getValue().getId(), epic.getValue());
            }
        }
        if (loadedSubTasks != null) {
            for (Map.Entry<Integer, SubTask> subTask : loadedSubTasks.entrySet()) {
                getSubTasks().put(subTask.getValue().getId(), subTask.getValue());
                try {
                    compareTasksByTimeAndAddToTreeSet(subTask.getValue());
                } catch (RuntimeException exception) {
                    System.out.println(exception.getMessage());
                }
            }
        }
        if (loadedHistory != null) {
            for (Task task : loadedHistory) {
                int id = task.getId();
                assert loadedTasks != null;
                if (loadedTasks.containsKey(id)) {
                    getTask(id);
                } else {
                    assert loadedSubTasks != null;
                    if (loadedSubTasks.containsKey(id)) {
                        getSubTask(id);
                    } else {
                        getEpic(id);
                    }
                }
            }
        }
    }

    public KVTaskClient getKvTaskClient() {
        return kvTaskClient;
    }
}
