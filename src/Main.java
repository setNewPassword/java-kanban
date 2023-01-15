import manager.InMemoryTaskManager;
import model.Epic;
import model.Status;
import model.SubTask;
import model.Task;


public class Main {

    public static void main(String[] args) {

        System.out.println("Поехали!");
        InMemoryTaskManager inMemoryTaskManager = new InMemoryTaskManager();

        Task task1 = new Task("Выгулять собаку", "Бегать, дурачиться и валяться в снегу");
        inMemoryTaskManager.addTask(task1);
        int task1ID = task1.getId();
        System.out.println("Таск №1 добавлен");

        Task task2 = new Task("Вынести мусор", "Надо не забыть собрать всякую мелочь по всей квартире");
        inMemoryTaskManager.addTask(task2);
        int task2ID = task2.getId();
        System.out.println("Таск №2 добавлен");
        System.out.println("-----------------------------------------------------------------------------------------");

        Epic epic1 = new Epic("Съездить в супермаркет", "Закупиться всяким на месяц");
        inMemoryTaskManager.addEpic(epic1);
        int epic1ID = epic1.getId();
        System.out.println("Пустой эпик №1 добавлен");

        Epic epic2 = new Epic("Убрать лёд с коврика в машине", "А то надоело, что когда жмешь на педали, ноги скользят");
        inMemoryTaskManager.addEpic(epic2);
        int epic2ID = epic2.getId();
        System.out.println("Пустой эпик №2 добавлен");
        System.out.println("-----------------------------------------------------------------------------------------");

        SubTask subTask1 = new SubTask("Купить продукты", "Молоко, сыр, спагетти, яйца", epic1ID);
        inMemoryTaskManager.addSubTask(subTask1);
        int subTask1ID = subTask1.getId();
        System.out.println("Сабтаск №1 к эпику №1 добавлен");

        SubTask subTask2 = new SubTask("Купить бытовую химию", "Таблетки для посудомойки", epic1ID);
        inMemoryTaskManager.addSubTask(subTask2);
        int subTask2ID = subTask2.getId();
        System.out.println("Сабтаск №2 к эпику №1 добавлен");

        SubTask subTask3 = new SubTask("Купить корм для собаки", "Только хороший, а не Педигри", epic1ID);
        inMemoryTaskManager.addSubTask(subTask3);
        int subTask3ID = subTask3.getId();
        System.out.println("Сабтаск №3 к эпику №1 добавлен");
        System.out.println("-----------------------------------------------------------------------------------------");

        for (Integer key : inMemoryTaskManager.getTasks().keySet()) {           // Устанавливаем всем таскам статус ЗАВЕРШЕНО
            inMemoryTaskManager.getTask(key).setTaskStatus(Status.DONE);
        }
        System.out.println("Установили статус DONE всем таскам.");

        for (Integer key : inMemoryTaskManager.getEpic(epic1ID).getSubTasksID()) {
            inMemoryTaskManager.updateStatusSubTask(key, Status.DONE);
        }
        System.out.println("Установили статус DONE всем сабтаскам эпика про супермаркет.");
        System.out.println("Статус самого эпика тоже должен быть DONE.");
        System.out.println("-----------------------------------------------------------------------------------------");

        System.out.println("Запрашиваем Таск №1");
        inMemoryTaskManager.getTask(task1ID);
        System.out.println("Запрашиваем Таск №2");
        inMemoryTaskManager.getTask(task2ID);
        System.out.println("Запрашиваем Эпик №2 (ПУСТОЙ)");
        inMemoryTaskManager.getEpic(epic2ID);
        System.out.println("-----------------------------------------------------------------------------------------");

        System.out.println("Запрашиваем историю");
        System.out.println("Должно быть: Таск №1, Таск №2, Эпик №2");
        System.out.println(inMemoryTaskManager.getHistory().getHistory());
        System.out.println("-----------------------------------------------------------------------------------------");

        System.out.println("Запрашиваем Эпик №1");
        inMemoryTaskManager.getEpic(epic1ID);
        System.out.println("Запрашиваем Таск №2");
        inMemoryTaskManager.getTask(task2ID);
        System.out.println("-----------------------------------------------------------------------------------------");

        System.out.println("Запрашиваем историю");
        System.out.println("Должно быть: Таск №1, Эпик №2, Эпик №1, Таск №2");
        System.out.println(inMemoryTaskManager.getHistory().getHistory());
        System.out.println("-----------------------------------------------------------------------------------------");

        System.out.println("Запрашиваем Сабтаск №3");
        inMemoryTaskManager.getSubTask(subTask3ID);
        System.out.println("Запрашиваем Сабтаск №2");
        inMemoryTaskManager.getSubTask(subTask2ID);
        System.out.println("Запрашиваем Сабтаск №1");
        inMemoryTaskManager.getSubTask(subTask1ID);
        System.out.println("-----------------------------------------------------------------------------------------");

        System.out.println("Запрашиваем историю");
        System.out.println("Должно быть: Таск №1, Эпик №2, Эпик №1, Таск №2, Сабтаск №3, Сабтаск №2, Сабтаск №1");
        System.out.println(inMemoryTaskManager.getHistory().getHistory());
        System.out.println("-----------------------------------------------------------------------------------------");

        System.out.println("Удаляем Таск №1");
        System.out.println("Он также должен пропасть из истории");
        inMemoryTaskManager.removeTask(task1ID);
        System.out.println("Запрашиваем историю");
        System.out.println("Должно быть: Эпик №2, Эпик №1, Таск №2, Сабтаск №3, Сабтаск №2, Сабтаск №1");
        System.out.println(inMemoryTaskManager.getHistory().getHistory());
        System.out.println("-----------------------------------------------------------------------------------------");

        System.out.println("Удаляем Сабтаск №2");
        System.out.println("Он также должен пропасть из истории");
        inMemoryTaskManager.removeSubTask(subTask2ID);
        System.out.println("Запрашиваем историю");
        System.out.println("Должно быть: Эпик №2, Эпик №1, Таск №2, Сабтаск №3, Сабтаск №1");
        System.out.println(inMemoryTaskManager.getHistory().getHistory());
        System.out.println("-----------------------------------------------------------------------------------------");

        System.out.println("Удаляем Эпик №1 (с сабтасками)");
        System.out.println("Он также должен пропасть из истории, и все его сабтаски тоже");
        inMemoryTaskManager.removeEpic(epic1ID);
        System.out.println("Запрашиваем историю");
        System.out.println("Должно быть: Эпик №2, Таск №2");
        System.out.println(inMemoryTaskManager.getHistory().getHistory());

        System.out.println("-----------------------------------------------------------------------------------------");
    }

}
