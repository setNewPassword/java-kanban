package manager;

import manager.interfaces.HistoryManager;
import manager.interfaces.TaskManager;
import model.Epic;
import model.Status;
import model.SubTask;
import model.Task;

import java.util.ArrayList;
import java.util.HashMap;

public class InMemoryTaskManager implements TaskManager {
    private int taskCounter = 0;
    private final HashMap<Integer, Task> tasks;
    private final HashMap<Integer, Epic> epics;
    private final HashMap<Integer, SubTask> subTasks;
    private final HistoryManager history;


    public InMemoryTaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subTasks = new HashMap<>();
        history = Managers.getDefaultHistory();
    }

    @Override
    public void addTask(Task task) {            // Присвоить таску айди и положить в хешмап
        task.setId(taskCounter++);
        tasks.put(task.getId(), task);
    }

    @Override
    public void addEpic(Epic epic) {            // Присвоить эпику айди и положить в хешмап
        epic.setId(taskCounter++);
        epics.put(epic.getId(), epic);
    }

    @Override
    public void addSubTask(SubTask subTask) {   // Присвоить сабтаску айди и положить в хешмап
        if (!epics.containsKey(subTask.getEpicID())) {  // Проверяем, есть ли эпик, который указан как родитель
            throw new RuntimeException("Ошибка: эпик отсутствует!");
        }
        subTask.setId(taskCounter++);
        subTasks.put(subTask.getId(), subTask);
        getSubTaskList(subTask.getEpicID()).add(subTask.getId());   // Добавить айди сабтаска в список эпика-родителя
        updateStatusEpic(subTask.getEpicID());                      // Обновить статус эпика
    }

    @Override
    public Task getTask(int id) {                               // Получение таска
        if (tasks.get(id) != null) {
            history.add(tasks.get(id));
            return tasks.get(id);
        } else {
            throw new RuntimeException("Ошибка: нет таска с таким id!");
        }
    }

    @Override
    public Epic getEpic(int id) {                               // Получение эпика
        if (epics.get(id) != null) {
            history.add(epics.get(id));
            return epics.get(id);
        } else {
            throw new RuntimeException("Ошибка: нет эпика с таким id!");
        }
    }

    @Override
    public SubTask getSubTask(int id) {                         // Получение сабтаска
        if (subTasks.get(id) != null) {
            history.add(subTasks.get(id));
            return subTasks.get(id);
        } else {
            throw new RuntimeException("Ошибка: нет сабтаска с таким id!");
        }
    }

    @Override
    public HashMap<Integer, Task> getTasks() {                  // Получение хешмапы всех тасков
        return new HashMap<>(tasks);
    }

    @Override
    public HashMap<Integer, Epic> getEpics() {                  // Получение хешмапы всех эпиков
        return new HashMap<>(epics);
    }

    @Override
    public HashMap<Integer, SubTask> getSubTasks() {            // Получение хешмапы всех сабтасков
        return new HashMap<>(subTasks);
    }

    @Override
    public void clearTasks() {                                  // Очистка хешмапы тасков
        for (Integer id : tasks.keySet()) {                     // Удаление всех тасков из истории
            history.remove(id);
        }
        tasks.clear();
    }

    @Override
    public void clearEpics() {                                  // Очистка хешмапы эпиков (ну и сабтасков, логично же)
        for (Integer id : subTasks.keySet()) {                  // Удаление всех сабтасков из истории
            history.remove(id);
        }
        subTasks.clear();                                       // Очистка хешмапы сабтасков

        for (Integer id : epics.keySet()) {                     // Удаление всех эпиков из истории
            history.remove(id);
        }
        epics.clear();                                          // Очистка хешмапы эпиков
    }

    @Override
    public void clearSubTasks() {                               // Очистка хешмапы сабтасков
        for (Integer id : subTasks.keySet()) {                  // Удаление всех сабтасков из истории
            history.remove(id);
        }
        subTasks.clear();
        for (Integer id : epics.keySet()) {                     // Проходимся по хешмапе эпиков
            epics.get(id).setSubTasksID(null);                  // Очистка списка сабтасков
            updateStatusEpic(id);                               // Обновление статуса эпика
        }
    }


    @Override
    public ArrayList<Integer> getSubTaskList(int epicID) {      // Получить список ID всех сабтасков эпика
        if (epics.get(epicID) != null) {
            return epics.get(epicID).getSubTasksID();
        } else {
            throw new RuntimeException("Ошибка: нет эпика с таким id!");
        }
    }


    @Override
    public void updateStatusEpic(int id) { // Обновить статус эпика
        Epic epic = new Epic(id, epics.get(id).getTitle(),      // Создаем новую копию эпика
                epics.get(id).getExtraInfo());
        epic.setSubTasksID(epics.get(id).getSubTasksID());      // Копируем список сабтасков

        int statusNew = 0; // Объявляем и инициализируем счетчики для статусов сабтасков
        int statusInProgress = 0;
        int statusDone = 0;
        if (epic.getSubTasksID() != null) {
            for (int i = 0; i < epic.getSubTasksID().size(); i++) { // Подсчитываем сколько каких статусов у сабтасков
                if (subTasks.get(epic.getSubTasksID().get(i)) != null) {
                    if (subTasks.get(epic.getSubTasksID().get(i)).getTaskStatus() == Status.NEW) {
                        statusNew++;
                    } else if (subTasks.get(epic.getSubTasksID().get(i)).getTaskStatus() == Status.IN_PROGRESS) {
                        statusInProgress++;
                    } else {
                        statusDone++;
                    }
                }
            }

            if (statusInProgress == 0 && statusDone == 0) {     // Если нет В_ПРОЦЕССЕ и ЗАВЕРШЕНО
                epic.setTaskStatus(Status.NEW);                 // то статус НЬЮ
            } else if (statusDone > 0 || (statusNew < 1 && statusInProgress < 1)) { // Если все ЗАВЕРШЕН
                epic.setTaskStatus(Status.DONE);                // то ЗАВЕРШЕНО
            } else {
                epic.setTaskStatus(Status.IN_PROGRESS);         // Иначе — В_ПРОЦЕССЕ
            }
        } else {
            epic.setTaskStatus(Status.NEW);                     //Если список сабтасков пустой, тогда НЬЮ
        }

        epics.put(epic.getId(), epic);
    }

    @Override
    public void removeTask(int id) {                            // Удалить таск
        if (tasks.get(id) != null) {                            // Проверяем наличие таска
            history.remove(id);                                 // Удаляем из истории
            tasks.remove(id);                                   // Удаляем из хешмапы тасков
        }
    }

    @Override
    public void removeSubTask(int id) {                         // Удалить сабтаск
        if (subTasks.get(id) != null) {                         // Проверяем наличие сабтаска
            if (getSubTaskList(subTasks.get(id).getEpicID()) != null) { // Есть ли этот сабтаск в списке у эпика?
                getSubTaskList(subTasks.get(id).getEpicID()).remove(Integer.valueOf(id)); // Удаляем из списка у эпика
                updateStatusEpic(subTasks.get(id).getEpicID()); // Обновляем статус эпика
                history.remove(id);                             // Удаляем из истории
                subTasks.remove(id);                            // Удаляем сам объект
            }
        }

    }

    @Override
    public void removeEpic(int id) {                            // Удалить эпик и все его сабтаски
        if (epics.get(id) != null) {                            // Проверяем наличие эпика
            if (getSubTaskList(id) != null) {                   // Если список сабтасков не пустой
                for (int subTaskID : getSubTaskList(id)) {      // удаляем все сабтаски
                    subTasks.remove(subTaskID);
                }
            }
            for (Integer subTaskId : epics.get(id).getSubTasksID()) {
                history.remove(subTaskId);                      // Удаляем из истории все сабтаски этого эпика
            }
            history.remove(id);                                 // Удаляем из истории сам эпик
            epics.remove(id);                                   // Удаляем эпик
        }
    }

    @Override
    public void updateStatusSubTask(int id, Status subTaskStatus) {
        SubTask subTask = new SubTask(id, subTasks.get(id).getTitle(),
                subTasks.get(id).getExtraInfo(),
                subTasks.get(id).getEpicID());
        subTask.setTaskStatus(subTaskStatus);

        subTasks.put(subTask.getId(), subTask);
        updateStatusEpic(subTask.getEpicID());                  // Обновить статус эпика
    }

    @Override
    public HistoryManager getHistory() {
        return history;
    }

}