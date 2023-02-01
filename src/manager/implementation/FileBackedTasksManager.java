package manager.implementation;

import manager.Managers;
import manager.exception.ManagerSaveException;
import manager.interfaces.HistoryManager;
import manager.interfaces.TaskManager;
import model.*;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class FileBackedTasksManager extends InMemoryTaskManager {
    private final Path path;                // Путь для сохранения файла бэкапа
    private final String delimiter = ";";   // Разделитель для CSV. Пока не решил, как разделять поля,
                                            // поэтому разделитель в переменной, чтоб не переписывать его в методах.

    public FileBackedTasksManager(@NotNull File file) {
        this.path = file.toPath();
    }

    public static void main(String[] args) {
        FileBackedTasksManager manager1 = Managers.getDefaultFBTM();

        Task task1 = new Task("Выгулять собаку", "Бегать, дурачиться и валяться в снегу");
        manager1.addTaskWithID(task1);
        int task1ID = task1.getId();
        System.out.println("Таск №1 добавлен");

        Task task2 = new Task("Вынести мусор", "Надо не забыть собрать всякую мелочь по всей квартире");
        manager1.addTaskWithID(task2);
        int task2ID = task2.getId();
        System.out.println("Таск №2 добавлен");
        System.out.println("-----------------------------------------------------------------------------------------");

        Epic epic1 = new Epic("Съездить в супермаркет", "Закупиться всяким на месяц");
        manager1.addEpicWithID(epic1);
        int epic1ID = epic1.getId();
        System.out.println("Пустой эпик №1 добавлен");

        Epic epic2 = new Epic("Убрать лёд с коврика в машине", "А то надоело, что когда жмешь на педали, ноги скользят");
        manager1.addEpicWithID(epic2);
        int epic2ID = epic2.getId();
        System.out.println("Пустой эпик №2 добавлен");
        System.out.println("-----------------------------------------------------------------------------------------");

        SubTask subTask1 = new SubTask("Купить продукты", "Молоко, сыр, спагетти, яйца", epic1ID);
        manager1.addSubTaskWithID(subTask1);
        int subTask1ID = subTask1.getId();
        System.out.println("Сабтаск №1 к эпику №1 добавлен");

        SubTask subTask2 = new SubTask("Купить бытовую химию", "Таблетки для посудомойки", epic1ID);
        manager1.addSubTaskWithID(subTask2);
        int subTask2ID = subTask2.getId();
        System.out.println("Сабтаск №2 к эпику №1 добавлен");

        SubTask subTask3 = new SubTask("Купить корм для собаки", "Только хороший, а не Педигри", epic1ID);
        manager1.addSubTaskWithID(subTask3);
        int subTask3ID = subTask3.getId();
        System.out.println("Сабтаск №3 к эпику №1 добавлен");
        System.out.println("-----------------------------------------------------------------------------------------");

        for (Integer key : manager1.getTasks().keySet()) {           // Устанавливаем всем таскам статус ЗАВЕРШЕНО
            manager1.getTask(key).setTaskStatus(Status.DONE);
        }
        System.out.println("Установили статус DONE всем таскам.");

        for (Integer key : manager1.getEpic(epic1ID).getSubTasksID()) {
            manager1.updateStatusSubTask(key, Status.DONE);
        }
        System.out.println("Установили статус DONE всем сабтаскам эпика про супермаркет.");
        System.out.println("Статус самого эпика тоже должен быть DONE.");
        System.out.println("-----------------------------------------------------------------------------------------");

        System.out.println("Запрашиваем Таск №1");
        manager1.getTask(task1ID);
        System.out.println("Запрашиваем Таск №2");
        manager1.getTask(task2ID);
        System.out.println("Запрашиваем Эпик №2 (ПУСТОЙ)");
        manager1.getEpic(epic2ID);
        System.out.println("-----------------------------------------------------------------------------------------");

        System.out.println("Запрашиваем историю");
        System.out.println("Должно быть: Таск №1, Таск №2, Эпик №2");
        System.out.println(manager1.getHistory().getHistory());
        System.out.println("-----------------------------------------------------------------------------------------");

        System.out.println("Запрашиваем Эпик №1");
        manager1.getEpic(epic1ID);
        System.out.println("Запрашиваем Таск №2");
        manager1.getTask(task2ID);
        System.out.println("-----------------------------------------------------------------------------------------");

        System.out.println("Запрашиваем историю");
        System.out.println("Должно быть: Таск №1, Эпик №2, Эпик №1, Таск №2");
        System.out.println(manager1.getHistory().getHistory());
        System.out.println("-----------------------------------------------------------------------------------------");

        System.out.println("Запрашиваем Сабтаск №3");
        manager1.getSubTask(subTask3ID);
        System.out.println("Запрашиваем Сабтаск №2");
        manager1.getSubTask(subTask2ID);
        System.out.println("Запрашиваем Сабтаск №1");
        manager1.getSubTask(subTask1ID);
        System.out.println("-----------------------------------------------------------------------------------------");

        System.out.println("Запрашиваем историю");
        System.out.println("Должно быть: Таск №1, Эпик №2, Эпик №1, Таск №2, Сабтаск №3, Сабтаск №2, Сабтаск №1");
        System.out.println(manager1.getHistory().getHistory());
        System.out.println("-----------------------------------------------------------------------------------------");

        System.out.println("Создаем новый экземпляр менеджера из файла");
        TaskManager manager2 = loadFromFile(new File("src/test/Testing.csv"));
        System.out.println("Новый экземпляр менеджера создан");

        System.out.println("-----------------------------------------------------------------------------------------");

        System.out.println("Запросим его историю");
        System.out.println("Должно быть: Таск №1, Эпик №2, Эпик №1, Таск №2, Сабтаск №3, Сабтаск №2, Сабтаск №1");
        System.out.println(manager2.getHistory().getHistory());
    }

    public void save() {
        try (Writer writer = new FileWriter(path.toString(), StandardCharsets.UTF_8)) {

            writer.write(String.join(delimiter, new String[]{                       // Пишем хэдер
                    "id",
                    "TaskType",
                    "title",
                    "extraInfo",
                    "TaskStatus",
                    "epicID",
                    "\n"}));

            for (Map.Entry<Integer, Task> entry : getTasks().entrySet()) {          // Пишем в файл таски
                writer.append(getTaskString(entry.getValue()))
                        .write("\n");
            }
            for (Map.Entry<Integer, Epic> entry : getEpics().entrySet()) {          // Пишем в файл эпики
                writer.append(getTaskString(entry.getValue()))
                        .write("\n");
            }
            for (Map.Entry<Integer, SubTask> entry : getSubTasks().entrySet()) {    // Пишем в файл сабтаски
                writer.append(getTaskString(entry.getValue()))
                        .write("\n");
            }
            writer.append("\n").                                                    // Пустая строка, разделитель
                    write(historyToString(getHistory()));                       // Пишем в файл историю
        } catch (IOException e) {
            e.printStackTrace();
            throw new ManagerSaveException(e.getMessage());
        }
    }


    // Так как айди задач нам не нужно присваивать заново, а нужно взять их из файла,
    // то нужно переопределить методы добавления задач без участия счетчика айдишников.
    @Override
    public void addTask(Task task) {
        getTasks().put(task.getId(), task);
        save();
    }

    public void addTaskWithID(Task task) {
        super.addTask(task);
        save();
    }

    @Override
    public void addEpic(Epic epic) {
        getEpics().put(epic.getId(), epic);
        save();
    }

    public void addEpicWithID(Epic epic) {
        super.addEpic(epic);
        save();
    }

    @Override
    public void addSubTask(@NotNull SubTask subTask) {
        if (!getEpics().containsKey(subTask.getEpicID())) {
            throw new RuntimeException("Ошибка: эпик отсутствует!");
        }
        super.getSubTasks().put(subTask.getId(), subTask);
        getSubTaskList(subTask.getEpicID()).add(subTask.getId());   // Добавить айди сабтаска в список эпика-родителя
        updateStatusEpic(subTask.getEpicID());                      // Обновить статус эпика
        save();
    }

    public void addSubTaskWithID(SubTask subTask) {
        super.addSubTask(subTask);
        save();
    }

    // Переопределяем методы, модифицирующие хешмапы с задачами и историю
    @Override
    public void removeTask(int id) {
        super.removeTask(id);
        save();
    }

    @Override
    public void removeSubTask(int id) {
        super.removeSubTask(id);
        save();
    }

    @Override
    public void removeEpic(int id) {
        super.removeEpic(id);
        save();
    }

    @Override
    public void clearTasks() {
        super.clearTasks();
        save();
    }

    @Override
    public void clearEpics() {
        super.clearEpics();
        save();
    }

    @Override
    public void clearSubTasks() {
        super.clearSubTasks();
        save();
    }

    @Override
    public Task getTask(int id) {
        Task task = super.getTask(id);
        save();
        return task;
    }

    @Override
    public Epic getEpic(int id) {
        Epic epic = super.getEpic(id);
        save();
        return epic;
    }

    @Override
    public SubTask getSubTask(int id) {
        SubTask subTask = super.getSubTask(id);
        save();
        return subTask;
    }

    @Override
    public void updateStatusEpic(int id) {
        super.updateStatusEpic(id);
        save();
    }

    @Override
    public void updateStatusSubTask(int id, Status subTaskStatus) {
        super.updateStatusSubTask(id, subTaskStatus);
        save();
    }

    public String getTaskString(@NotNull Task task) {           // Таск в строку
        return String.join(delimiter, new String[]{
                Integer.toString(task.getId()),                 // айдишник
                task.getTaskType().name(),                      // Тип задачи
                task.getTitle(),                                // Заголовок
                task.getExtraInfo(),                            // Подробное описание
                task.getTaskStatus().name(),                    // Статус
                " "                                             // Родительский эпик (неактуально для таска)
        });
    }

    public String getTaskString(@NotNull Epic epic) {           // Эпик в строку
        return String.join(delimiter, new String[]{Integer.toString(epic.getId()),                 // айдишник
                epic.getTaskType().name(),                      // Тип задачи
                epic.getTitle(),                                // Заголовок
                epic.getExtraInfo(),                            // Подробное описание
                " ",                                            // Статус (неактуально для эпика, сам рассчитает)
                " "                                             // Родительский эпик (неактуально для эпика)
        });
    }

    public String getTaskString(@NotNull SubTask subTask) {     // Сабтаск в строку
        return String.join(delimiter, new String[]{Integer.toString(subTask.getId()),              // айдишник
                subTask.getTaskType().name(),                   // Тип задачи
                subTask.getTitle(),                             // Заголовок
                subTask.getExtraInfo(),                         // Подробное описание
                subTask.getTaskStatus().name(),                 // Статус
                Integer.toString(subTask.getEpicID())           // Родительский эпик
        });
    }

    public Task getTaskFromString(String id, String title, String extraInfo, String status) {
        return new Task(
                Integer.parseInt(id),
                title,
                extraInfo,
                Status.valueOf(status));
    }

    public Epic getEpicFromString(String id, String title, String extraInfo) {
        return new Epic(
                Integer.parseInt(id),
                title,
                extraInfo);
    }

    public SubTask getSubTaskFromString(String id, String title, String extraInfo, String status, String epicID) {
        return new SubTask(
                Integer.parseInt(id),
                title,
                extraInfo,
                Status.valueOf(status),
                Integer.parseInt(epicID));
    }

    public String historyToString(@NotNull HistoryManager manager) {     // История в строку
        if (manager.getHistory().size() != 0) {
            List<Task> tasks = manager.getHistory();
            StringBuilder stringBuilder = new StringBuilder(Integer.toString(tasks.get(0).getId()));
            for (int i = 1; i < tasks.size(); i++) {
                stringBuilder.append(",").append(tasks.get(i).getId());
            }
            return stringBuilder.toString();
        } else {
            return "\n";
        }

    }

    public static Optional<List<Integer>> historyListFromString(String value) { // История из строки в ArrayList
        List<Integer> historyList = new ArrayList<>();
        if (value != null) {
            String[] historyArray = value.split(",");
            for (String task : historyArray) {
                historyList.add(Integer.parseInt(task));
            }
            return Optional.of(historyList);
        } else {
            return Optional.of(Collections.emptyList());
        }
    }

    public void addTaskByType(@NotNull String str) {
        String[] taskArray = str.split(delimiter);
        TaskType taskType = TaskType.valueOf(taskArray[1]);
        switch (taskType) {
            case TASK -> addTask(getTaskFromString(
                    taskArray[0],
                    taskArray[2],
                    taskArray[3],
                    taskArray[4]));
            case EPIC -> addEpic(getEpicFromString(
                    taskArray[0],
                    taskArray[2],
                    taskArray[3]));
            case SUBTASK -> addSubTask(getSubTaskFromString(
                    taskArray[0],
                    taskArray[2],
                    taskArray[3],
                    taskArray[4],
                    taskArray[5]));
        }
    }

    public static @NotNull FileBackedTasksManager loadFromFile(File file) {
        FileBackedTasksManager fileBackedTasksManager = new FileBackedTasksManager(file);
        if (Files.exists(file.toPath())) {
            try (Reader fileReader = new FileReader(file.toPath().toString(), StandardCharsets.UTF_8);
                 BufferedReader bufferedReader = new BufferedReader(fileReader)) {
                bufferedReader.readLine();                                                 // Пропускаем хэдер
                while (bufferedReader.ready()) {
                    String str = bufferedReader.readLine();
                    if (str != null && !str.isEmpty()) {                                    // Если строка не пустая
                        fileBackedTasksManager.addTaskByType(str);                          // Добавляем таски по типу
                    } else if (str != null && str.isEmpty()) {                              // Если строка-разделитель
                        String historyLine = bufferedReader.readLine();                     // Читаем следующую строку
                        List<Integer> historyList = new ArrayList<>();
                        if (historyListFromString(historyLine).isPresent()) {
                            historyList = historyListFromString(historyLine).get();
                        }
                        Map<Integer, Task> taskMap = fileBackedTasksManager.getTasks();     // Получаем ссылки на мапы
                        Map<Integer, Epic> epicMap = fileBackedTasksManager.getEpics();
                        Map<Integer, SubTask> subTaskMap = fileBackedTasksManager.getSubTasks();
                        if (historyList != null) {
                            for (Integer id : historyList) {
                                if (taskMap.containsKey(id)) {
                                    fileBackedTasksManager.getTask(id);
                                } else if (epicMap.containsKey(id)) {
                                    fileBackedTasksManager.getEpic(id);
                                } else if (subTaskMap.containsKey(id)) {
                                    fileBackedTasksManager.getSubTask(id);
                                }

                            }
                        }

                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            try {
                Files.createFile(file.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return fileBackedTasksManager;
    }


}
