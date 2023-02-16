package main.java.manager.implementation;
import main.java.manager.Managers;
import main.java.manager.interfaces.HistoryManager;
import main.java.manager.interfaces.TaskManager;
import main.java.model.Epic;
import main.java.model.Status;
import main.java.model.SubTask;
import main.java.model.Task;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private int taskCounter = 0;
    private final HashMap<Integer, Task> tasks;
    private final HashMap<Integer, Epic> epics;
    private final HashMap<Integer, SubTask> subTasks;
    private final HistoryManager history;
    private final TreeSet<Task> listOfTasksSortedByTime;

    public InMemoryTaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subTasks = new HashMap<>();
        history = Managers.getDefaultHistory();
        this.listOfTasksSortedByTime = new TreeSet<>(taskByStartTimeComparator);
    }

    Comparator<Task> taskByStartTimeComparator = (task1, task2) -> {
        int result = 0;
        if (task1.getStartTime() == null && task2.getStartTime() == null) {
            result = task2.getId() - task1.getId();
        } else if (task1.getStartTime() != null && task2.getStartTime() == null) {
            result = -1;
        } else if (task1.getStartTime() == null && task2.getStartTime() != null) {
            result = 1;
        } else if (task1.getStartTime() != null && task2.getStartTime() != null) {
            if (task1.getStartTime().isAfter(task2.getStartTime())) {
                result = 1;
            } else if (task1.getStartTime().isBefore(task2.getStartTime())) {
                result = -1;
            }
        }
        return result;
    };

    @Override
    public int addTask(Task task) {            // Присвоить таску айди и положить в хешмап
        task.setId(taskCounter++);
        tasks.put(task.getId(), task);
        compareTasksByTimeAndAddToTreeSet(task);
        return task.getId();
    }

    @Override
    public int addEpic(Epic epic) {            // Присвоить эпику айди и положить в хешмап
        epic.setId(taskCounter++);
        epics.put(epic.getId(), epic);
        return epic.getId();
    }

    @Override
    public int addSubTask(SubTask subTask) {           // Присвоить сабтаску айди и положить в хешмап
        if (!epics.containsKey(subTask.getEpicID())) {  // Проверяем, есть ли эпик, который указан как родитель
            throw new RuntimeException("Ошибка: эпик отсутствует!");
        }
        subTask.setId(taskCounter++);
        subTasks.put(subTask.getId(), subTask);
        getSubTaskList(subTask.getEpicID()).add(subTask.getId());   // Добавить айди сабтаска в список эпика-родителя
        updateStatusEpic(subTask.getEpicID());                      // Обновить статус эпика
        setStartTimeForEpic(subTask.getEpicID());                   // Рассчитать время начала для эпика
        setEndTimeForEpic(subTask.getEpicID());                     // Рассчитать время окончания для эпика
        setDurationForEpic(subTask.getEpicID());                    // Рассчитать продолжительность эпика

        compareTasksByTimeAndAddToTreeSet(subTask);
        return subTask.getId();
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
        return tasks;
    }

    @Override
    public HashMap<Integer, Epic> getEpics() {                  // Получение хешмапы всех эпиков
        return epics;
    }

    @Override
    public HashMap<Integer, SubTask> getSubTasks() {            // Получение хешмапы всех сабтасков
        return subTasks;
    }

    @Override
    public void clearTasks() {                                  // Очистка хешмапы тасков
        for (Task task : tasks.values()) {
            listOfTasksSortedByTime.remove(task);
        }
        for (Integer id : tasks.keySet()) {                     // Удаление всех тасков из истории
            history.remove(id);
        }
        tasks.clear();
    }

    @Override
    public void clearEpics() {                                  // Очистка хешмапы эпиков (ну и сабтасков, логично же)
        for (SubTask subTask : subTasks.values()) {
            listOfTasksSortedByTime.remove(subTask);
        }
        for (Integer id : subTasks.keySet()) {                  // Удаление всех сабтасков из истории
            history.remove(id);
        }
        subTasks.clear();                                       // Очистка хешмапы сабтасков

        for (Epic epic : epics.values()) {
            listOfTasksSortedByTime.remove(epic);
        }

        for (Integer id : epics.keySet()) {                     // Удаление всех эпиков из истории
            history.remove(id);
        }
        epics.clear();                                          // Очистка хешмапы эпиков
    }

    @Override
    public void clearSubTasks() {                               // Очистка хешмапы сабтасков

        for (SubTask subTask : subTasks.values()) {
            listOfTasksSortedByTime.remove(subTask);
        }

        for (Integer id : subTasks.keySet()) {                  // Удаление всех сабтасков из истории
            history.remove(id);
        }
        subTasks.clear();

        for (Integer id : epics.keySet()) {                         // Проходимся по хешмапе эпиков
            getEpicWithoutHistorySaving(id).setSubTasksID(new ArrayList<>()); // Очистка списка сабтасков
            getEpicWithoutHistorySaving(id).setStartTime(null);     // Без сабтасков у эпика нет времени начала
            getEpicWithoutHistorySaving(id).setEndTime(null);       // Без сабтасков у эпика нет времени окончания
            getEpicWithoutHistorySaving(id).setDuration(null);      // Без сабтасков у эпика нет продолжительности
            updateStatusEpic(id);                                   // Обновление статуса эпика
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
            } else if (statusDone > 0 && (statusNew < 1 && statusInProgress < 1)) { // Если все ЗАВЕРШЕН
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
            listOfTasksSortedByTime.remove(tasks.get(id));
            history.remove(id);                                 // Удаляем из истории
            tasks.remove(id);                                   // Удаляем из хешмапы тасков
        }
    }

    @Override
    public void removeSubTask(int id) {                         // Удалить сабтаск
        if (subTasks.get(id) != null) {                         // Проверяем наличие сабтаска
            listOfTasksSortedByTime.remove(subTasks.get(id));
            if (getSubTaskList(subTasks.get(id).getEpicID()) != null) { // Есть ли этот сабтаск в списке у эпика?
                getSubTaskList(subTasks.get(id).getEpicID()).remove(Integer.valueOf(id)); // Удаляем из списка у эпика
                updateStatusEpic(subTasks.get(id).getEpicID());         // Обновляем статус эпика
                setStartTimeForEpic(subTasks.get(id).getEpicID());      // Обновить время начала для эпика
                setEndTimeForEpic(subTasks.get(id).getEpicID());        // Обновить время окончания для эпика
                setDurationForEpic(subTasks.get(id).getEpicID());       // Обновить продолжительность эпика
                history.remove(id);                                     // Удаляем из истории
                subTasks.remove(id);                                    // Удаляем сам объект
            }
        }

    }

    @Override
    public void removeEpic(int id) {                            // Удалить эпик и все его сабтаски
        if (epics.get(id) != null) {                            // Проверяем наличие эпика
            listOfTasksSortedByTime.remove(epics.get(id));

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
                subTaskStatus,
                subTasks.get(id).getEpicID(),
                subTasks.get(id).getStartTime(),
                subTasks.get(id).getDuration());

        subTasks.put(subTask.getId(), subTask);
        updateStatusEpic(subTask.getEpicID());                  // Обновить статус эпика
    }

    @Override
    public HistoryManager getHistory() {
        return history;
    }

    private void compareTasksByTimeAndAddToTreeSet(Task task) {
        listOfTasksSortedByTime.add(task);                      // Кладем задачу в сортированный список
        LocalDateTime prev = LocalDateTime.MIN;                 // Минимально возможная дата
        for (Task currentTask : listOfTasksSortedByTime) {
            if (currentTask.getStartTime() != null) {
                if (prev.isAfter(currentTask.getStartTime())) { // Проверяем пересечения по времени
                    listOfTasksSortedByTime.remove(task);
                    throw new RuntimeException("Произошло пересечение задач по времени. Добавленная задача будет удалена.");
                }
                if (currentTask.getEndTime().isPresent()) {
                    prev = (currentTask.getEndTime()).get();
                }
            }
        }
    }

    private void setStartTimeForEpic(int epicId) {                              // Установка времени старта для эпика
        getEpicWithoutHistorySaving(epicId).getSubTasksID()                     // Получаем список сабтасков
                .stream()                                                       // Преобразуем в стрим int
                .map(this::getSubtaskWithoutHistorySaving)                      // Получаем стрим сабтасков
                .map(SubTask::getStartTime)                                     // Получаем стрим startTime
                .filter(Objects::nonNull)                                       // Исключаем из стрима null
                .min(LocalDateTime::compareTo)                                  // Получаем минимальное Optional
                .ifPresent(getEpicWithoutHistorySaving(epicId)::setStartTime);  // Устанавливаем для эпика startTime
    }

    private void setEndTimeForEpic(int epicId) {                                // Установка endTime для эпика
        getEpicWithoutHistorySaving(epicId).getSubTasksID()
                .stream()
                .map(this::getSubtaskWithoutHistorySaving)
                .map(x -> x.getEndTime().orElse(x.getStartTime()))
                .filter(Objects::nonNull)
                .max(LocalDateTime::compareTo)
                .ifPresent(getEpicWithoutHistorySaving(epicId)::setEndTime);
    }

    private void setDurationForEpic(int epicId) {                               // Установка duration для эпика
        getEpicWithoutHistorySaving(epicId)
                .setDuration(getEpicWithoutHistorySaving(epicId).getSubTasksID()
                .stream()
                .map(this::getSubtaskWithoutHistorySaving)
                .map(Task::getDuration)
                .filter(Objects::nonNull)
                .reduce(Duration.ZERO, Duration::plus));
    }

    private SubTask getSubtaskWithoutHistorySaving(int id) {
        return subTasks.get(id);
    }

    private Epic getEpicWithoutHistorySaving(int id) {
        return epics.get(id);
    }

    public List<Task> getPrioritizedTasks() {
        return new ArrayList<>(listOfTasksSortedByTime);
    }
}