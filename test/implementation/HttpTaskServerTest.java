package implementation;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import main.java.manager.Managers;
import main.java.manager.interfaces.TaskManager;
import main.java.manager.servers.HttpTaskServer;
import main.java.manager.servers.KVServer;
import main.java.model.Epic;
import main.java.model.SubTask;
import main.java.model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskServerTest {
    URI basicUri = URI.create("http://localhost:8080/tasks/");
    HttpClient client;
    Gson gson = Managers.getGson();
    KVServer kvServer;
    HttpTaskServer httpTaskServer;
    TaskManager taskManager;

    protected LocalDateTime dateTime1 = LocalDateTime.of(2023, 2, 12, 11, 30);
    protected LocalDateTime dateTime2 = LocalDateTime.of(2023, 2, 12, 11, 45);
    protected Duration duration10 = Duration.ofMinutes(10);
    protected Duration duration20 = Duration.ofMinutes(20);
    protected Task task1 = new Task("Title for Task-1", "Description for Task-1", dateTime1, duration10);
    protected Task task2 = new Task("Title for Task-2", "Description for Task-2", dateTime2, duration20);
    protected Epic epic1 = new Epic("Title of Epic-1", "Description for Epic-1");
    Type tasksType = new TypeToken<HashMap<Integer, Task>>(){}.getType();
    Type epicsType = new TypeToken<HashMap<Integer, Epic>>(){}.getType();
    Type subTasksType = new TypeToken<HashMap<Integer, SubTask>>(){}.getType();

    @BeforeEach
    void restartAll() throws IOException, InterruptedException {
        client = HttpClient.newHttpClient();
        kvServer = new KVServer();
        kvServer.start();
        httpTaskServer = new HttpTaskServer();
        httpTaskServer.startServer();
        taskManager = Managers.getDefaultInMemoryTaskManager();
    }

    @AfterEach
    void stopAll() {
        kvServer.stop();
        httpTaskServer.stopServer();
    }

    @Test
    void shouldAddTasksAndReturnTasksMap() {
        int task1Id = taskManager.addTask(task1);
        int task2Id = taskManager.addTask(task2);
        String task1PostJson = gson.toJson(taskManager.getTask(task1Id));
        String task2PostJson = gson.toJson(taskManager.getTask(task2Id));
        HttpRequest requestPostTask1 = HttpRequest.newBuilder()
                .uri(URI.create(basicUri + "task/"))
                .version(HttpClient.Version.HTTP_1_1)
                .POST(HttpRequest.BodyPublishers.ofString(task1PostJson))
                .build();
        HttpRequest requestPostTask2 = HttpRequest.newBuilder()
                .uri(URI.create(basicUri + "task/"))
                .version(HttpClient.Version.HTTP_1_1)
                .POST(HttpRequest.BodyPublishers.ofString(task2PostJson))
                .build();
        try {
            client.send(requestPostTask1, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }

        try {
            client.send(requestPostTask2, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }

        HttpRequest requestGetTasks = HttpRequest.newBuilder()
                .uri(URI.create(basicUri + "task/"))
                .version(HttpClient.Version.HTTP_1_1)
                .GET()
                .build();
        HashMap<Integer, Task> receivedTasks = null;
        try {
            HttpResponse<String> responseGetTasks = client.send(requestGetTasks, HttpResponse.BodyHandlers.ofString());
            if (responseGetTasks.statusCode() == 200) {
                String tasksJson = responseGetTasks.body();
                JsonElement jsonElement = JsonParser.parseString(tasksJson);
                if (!jsonElement.isJsonObject()) {
                    throw new RuntimeException("Получен не JsonObject.");
                }
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                receivedTasks = gson.fromJson(jsonObject, tasksType);
            } else {
                System.out.println("Что-то пошло не так при запросе с сервера.");
                System.out.println("Сервер вернул код состояния: " + responseGetTasks.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }

        assertNotNull(receivedTasks, "Полученная HashMap пуста.");
        assertEquals(2, receivedTasks.size(), "Размер полученной HashMap отличается от ожидаемого.");
        assertEquals(task1.getTitle(), receivedTasks.get(task1Id).getTitle(), "Заголовки не совпадают.");
        assertEquals(task2.getEndTime(), receivedTasks.get(task2Id).getEndTime(), "Время не совпадает.");
    }

    @Test
    void shouldAddTaskAndReturnTaskById() {
        int task1Id = taskManager.addTask(task1);
        String task1PostJson = gson.toJson(taskManager.getTask(task1Id));
        HttpRequest requestPostTask1 = HttpRequest.newBuilder()
                .uri(URI.create(basicUri + "task/"))
                .version(HttpClient.Version.HTTP_1_1)
                .POST(HttpRequest.BodyPublishers.ofString(task1PostJson))
                .build();
        try {
            client.send(requestPostTask1, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        HttpRequest requestGetTaskById = HttpRequest.newBuilder()
                .uri(URI.create(basicUri + "task/?id=" + task1Id))
                .version(HttpClient.Version.HTTP_1_1)
                .GET()
                .build();
        Task receivedTask = null;
        try {
            HttpResponse<String> responseGetTaskById = client.send(requestGetTaskById, HttpResponse.BodyHandlers.ofString());
            if (responseGetTaskById.statusCode() == 200) {
                String task1Json = responseGetTaskById.body();
                JsonElement jsonElement = JsonParser.parseString(task1Json);
                if (!jsonElement.isJsonObject()) {
                    throw new RuntimeException("Получен не JsonObject.");
                }
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                receivedTask = gson.fromJson(jsonObject, Task.class);
            } else {
                System.out.println("Что-то пошло не так при запросе с сервера.");
                System.out.println("Сервер вернул код состояния: " + responseGetTaskById.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }

        assertNotNull(receivedTask, "Полученная задача пуста.");
        assertEquals(taskManager.getTask(task1Id).getTitle(), receivedTask.getTitle(), "Заголовки не совпадают.");
        assertEquals(taskManager.getTask(task1Id).getEndTime(), receivedTask.getEndTime(), "Время не совпадает.");
    }

    @Test
    void shouldClearTasksMap() {
        int task1Id = taskManager.addTask(task1);
        int task2Id = taskManager.addTask(task2);
        String task1PostJson = gson.toJson(taskManager.getTask(task1Id));
        String task2PostJson = gson.toJson(taskManager.getTask(task2Id));
        HttpRequest requestPostTask1 = HttpRequest.newBuilder()
                .uri(URI.create(basicUri + "task/"))
                .version(HttpClient.Version.HTTP_1_1)
                .POST(HttpRequest.BodyPublishers.ofString(task1PostJson))
                .build();
        HttpRequest requestPostTask2 = HttpRequest.newBuilder()
                .uri(URI.create(basicUri + "task/"))
                .version(HttpClient.Version.HTTP_1_1)
                .POST(HttpRequest.BodyPublishers.ofString(task2PostJson))
                .build();
        try {
            client.send(requestPostTask1, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }

        try {
            client.send(requestPostTask2, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        HttpRequest requestDeleteTasks = HttpRequest.newBuilder()
                .uri(URI.create(basicUri + "task/"))
                .version(HttpClient.Version.HTTP_1_1)
                .DELETE()
                .build();
        try {
            client.send(requestDeleteTasks, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }

        HttpRequest requestGetTasks = HttpRequest.newBuilder()
                .uri(URI.create(basicUri + "task/"))
                .version(HttpClient.Version.HTTP_1_1)
                .GET()
                .build();
        HashMap<Integer, Task> receivedTasks = null;
        try {
            HttpResponse<String> responseGetTasks = client.send(requestGetTasks, HttpResponse.BodyHandlers.ofString());
            if (responseGetTasks.statusCode() == 200) {
                String tasksJson = responseGetTasks.body();
                JsonElement jsonElement = JsonParser.parseString(tasksJson);
                if (!jsonElement.isJsonObject()) {
                    throw new RuntimeException("Получен не JsonObject.");
                }
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                receivedTasks = gson.fromJson(jsonObject, tasksType);
            } else {
                System.out.println("Что-то пошло не так при запросе с сервера.");
                System.out.println("Сервер вернул код состояния: " + responseGetTasks.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }

        assertEquals(0, receivedTasks != null ? receivedTasks.size() : 0, "Полученная HashMap не пуста.");
    }

    @Test
    void shouldDeleteTaskById() {
        int task1Id = taskManager.addTask(task1);
        String task1PostJson = gson.toJson(taskManager.getTask(task1Id));
        HttpRequest requestPostTask1 = HttpRequest.newBuilder()
                .uri(URI.create(basicUri + "task/"))
                .version(HttpClient.Version.HTTP_1_1)
                .POST(HttpRequest.BodyPublishers.ofString(task1PostJson))
                .build();
        try {
            client.send(requestPostTask1, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        HttpRequest requestDeleteTaskById = HttpRequest.newBuilder()
                .uri(URI.create(basicUri + "task/?id=" + task1Id))
                .version(HttpClient.Version.HTTP_1_1)
                .DELETE()
                .build();
        try {
            client.send(requestDeleteTaskById, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }

        HttpRequest requestGetTaskById = HttpRequest.newBuilder()
                .uri(URI.create(basicUri + "task/?id=" + task1Id))
                .version(HttpClient.Version.HTTP_1_1)
                .GET()
                .build();
        Task receivedTask = null;
        HttpResponse<String> responseGetTaskById = null;
        try {
            responseGetTaskById = client.send(requestGetTaskById, HttpResponse.BodyHandlers.ofString());
            if (responseGetTaskById.statusCode() == 200) {
                String task1Json = responseGetTaskById.body();
                JsonElement jsonElement = JsonParser.parseString(task1Json);
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                receivedTask = gson.fromJson(jsonObject, Task.class);
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }

        assertNull(receivedTask, "Полученная задача не пуста.");
        assert responseGetTaskById != null;
        assertEquals(400, responseGetTaskById.statusCode(), "Код ответа сервера не совпадает.");
    }

    @Test
    void shouldAddSubTaskAndEpicAndReturnSubTasksMapAndEpicsMap() {
        int epic1Id = taskManager.addEpic(epic1);
        SubTask subTask1 = new SubTask("Title for SubTask-1", "Description for SubTask-1",
                epic1Id, dateTime1, duration10);
        int subTask1Id = taskManager.addSubTask(subTask1);
        String epic1PostJson = gson.toJson(taskManager.getEpic(epic1Id));
        String subTask1PostJson = gson.toJson(taskManager.getSubTask(subTask1Id));
        HttpRequest requestPostEpic1 = HttpRequest.newBuilder()
                .uri(URI.create(basicUri + "epic/"))
                .version(HttpClient.Version.HTTP_1_1)
                .POST(HttpRequest.BodyPublishers.ofString(epic1PostJson))
                .build();
        HttpRequest requestPostSubTask1 = HttpRequest.newBuilder()
                .uri(URI.create(basicUri + "subtask/"))
                .version(HttpClient.Version.HTTP_1_1)
                .POST(HttpRequest.BodyPublishers.ofString(subTask1PostJson))
                .build();
        try {
            client.send(requestPostEpic1, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }

        try {
            client.send(requestPostSubTask1, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }

        HttpRequest requestGetEpics = HttpRequest.newBuilder()
                .uri(URI.create(basicUri + "epic/"))
                .version(HttpClient.Version.HTTP_1_1)
                .GET()
                .build();
        HashMap<Integer, Epic> receivedEpics = null;
        try {
            HttpResponse<String> responseGetEpics = client.send(requestGetEpics, HttpResponse.BodyHandlers.ofString());
            if (responseGetEpics.statusCode() == 200) {
                String epicsJson = responseGetEpics.body();
                JsonElement jsonElement = JsonParser.parseString(epicsJson);
                if (!jsonElement.isJsonObject()) {
                    throw new RuntimeException("Получен не JsonObject.");
                }
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                receivedEpics = gson.fromJson(jsonObject, epicsType);
            } else {
                System.out.println("Что-то пошло не так при запросе с сервера.");
                System.out.println("Сервер вернул код состояния: " + responseGetEpics.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }

        HttpRequest requestGetSubTasks = HttpRequest.newBuilder()
                .uri(URI.create(basicUri + "subtask/"))
                .version(HttpClient.Version.HTTP_1_1)
                .GET()
                .build();
        HashMap<Integer, SubTask> receivedSubTasks = null;
        try {
            HttpResponse<String> responseGetSubTasks = client.send(requestGetSubTasks, HttpResponse.BodyHandlers.ofString());
            if (responseGetSubTasks.statusCode() == 200) {
                String subTasksJson = responseGetSubTasks.body();
                JsonElement jsonElement = JsonParser.parseString(subTasksJson);
                if (!jsonElement.isJsonObject()) {
                    throw new RuntimeException("Получен не JsonObject.");
                }
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                receivedSubTasks = gson.fromJson(jsonObject, subTasksType);
            } else {
                System.out.println("Что-то пошло не так при запросе с сервера.");
                System.out.println("Сервер вернул код состояния: " + responseGetSubTasks.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }

        assertNotNull(receivedEpics, "Полученная EpicsHashMap пуста.");
        assertEquals(1, receivedEpics.size(), "Размер полученной HashMap отличается от ожидаемого.");
        assertEquals(epic1.getTitle(), receivedEpics.get(epic1Id).getTitle(), "Заголовки не совпадают.");

        assertNotNull(receivedSubTasks, "Полученная SubTasksHashMap пуста.");
        assertEquals(1, receivedSubTasks.size(), "Размер полученной HashMap отличается от ожидаемого.");
        assertEquals(subTask1.getTitle(), receivedSubTasks.get(subTask1Id).getTitle(), "Заголовки не совпадают.");
        assertEquals(subTask1.getEndTime(), receivedSubTasks.get(subTask1Id).getEndTime(), "Время не совпадает.");
    }

    @Test
    void shouldAddEpicAndSubTaskAndReturnTheyById() {
        int epic1Id = taskManager.addEpic(epic1);
        SubTask subTask1 = new SubTask("Title for SubTask-1", "Description for SubTask-1",
                epic1Id, dateTime1, duration10);
        int subTask1Id = taskManager.addSubTask(subTask1);
        String epic1PostJson = gson.toJson(taskManager.getEpic(epic1Id));
        String subTask1PostJson = gson.toJson(taskManager.getSubTask(subTask1Id));
        HttpRequest requestPostEpic1 = HttpRequest.newBuilder()
                .uri(URI.create(basicUri + "epic/"))
                .version(HttpClient.Version.HTTP_1_1)
                .POST(HttpRequest.BodyPublishers.ofString(epic1PostJson))
                .build();
        HttpRequest requestPostSubTask1 = HttpRequest.newBuilder()
                .uri(URI.create(basicUri + "subtask/"))
                .version(HttpClient.Version.HTTP_1_1)
                .POST(HttpRequest.BodyPublishers.ofString(subTask1PostJson))
                .build();
        try {
            client.send(requestPostEpic1, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }

        try {
            client.send(requestPostSubTask1, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        HttpRequest requestGetEpicById = HttpRequest.newBuilder()
                .uri(URI.create(basicUri + "epic/?id=" + epic1Id))
                .version(HttpClient.Version.HTTP_1_1)
                .GET()
                .build();
        Epic receivedEpic = null;
        try {
            HttpResponse<String> responseGetEpicById = client.send(requestGetEpicById, HttpResponse.BodyHandlers.ofString());
            if (responseGetEpicById.statusCode() == 200) {
                String epic1Json = responseGetEpicById.body();
                JsonElement jsonElement = JsonParser.parseString(epic1Json);
                if (!jsonElement.isJsonObject()) {
                    throw new RuntimeException("Получен не JsonObject.");
                }
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                receivedEpic = gson.fromJson(jsonObject, Epic.class);
            } else {
                System.out.println("Что-то пошло не так при запросе с сервера.");
                System.out.println("Сервер вернул код состояния: " + responseGetEpicById.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }

        HttpRequest requestGetSubTaskById = HttpRequest.newBuilder()
                .uri(URI.create(basicUri + "subtask/?id=" + subTask1Id))
                .version(HttpClient.Version.HTTP_1_1)
                .GET()
                .build();
        SubTask receivedSubTask = null;
        try {
            HttpResponse<String> responseGetSubTaskById = client.send(requestGetSubTaskById,
                    HttpResponse.BodyHandlers.ofString());
            if (responseGetSubTaskById.statusCode() == 200) {
                String subTask1Json = responseGetSubTaskById.body();
                JsonElement jsonElement = JsonParser.parseString(subTask1Json);
                if (!jsonElement.isJsonObject()) {
                    throw new RuntimeException("Получен не JsonObject.");
                }
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                receivedSubTask = gson.fromJson(jsonObject, SubTask.class);
            } else {
                System.out.println("Что-то пошло не так при запросе с сервера.");
                System.out.println("Сервер вернул код состояния: " + responseGetSubTaskById.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }

        assertNotNull(receivedEpic, "Полученный эпик пуст.");
        assertEquals(taskManager.getEpic(epic1Id).getTitle(), receivedEpic.getTitle(), "Заголовки не совпадают.");

        assertNotNull(receivedSubTask, "Полученный сабТаск пуст.");
        assertEquals(taskManager.getSubTask(subTask1Id).getTitle(), receivedSubTask.getTitle(), "Заголовки не совпадают.");
        assertEquals(taskManager.getSubTask(subTask1Id).getEndTime(), receivedSubTask.getEndTime(), "Время не совпадает.");
    }

    @Test
    void shouldReturnListOfSubTasksId() {
        int epic1Id = taskManager.addEpic(epic1);
        SubTask subTask1 = new SubTask("Title for SubTask-1", "Description for SubTask-1",
                epic1Id, dateTime1, duration10);
        SubTask subTask2 = new SubTask("Title for SubTask-2", "Description for SubTask-2",
                epic1Id, dateTime2, duration20);
        int subTask1Id = taskManager.addSubTask(subTask1);
        int subTask2Id = taskManager.addSubTask(subTask2);
        String epic1PostJson = gson.toJson(taskManager.getEpic(epic1Id));
        String subTask1PostJson = gson.toJson(taskManager.getSubTask(subTask1Id));
        String subTask2PostJson = gson.toJson(taskManager.getSubTask(subTask2Id));
        HttpRequest requestPostEpic1 = HttpRequest.newBuilder()
                .uri(URI.create(basicUri + "epic/"))
                .version(HttpClient.Version.HTTP_1_1)
                .POST(HttpRequest.BodyPublishers.ofString(epic1PostJson))
                .build();
        HttpRequest requestPostSubTask1 = HttpRequest.newBuilder()
                .uri(URI.create(basicUri + "subtask/"))
                .version(HttpClient.Version.HTTP_1_1)
                .POST(HttpRequest.BodyPublishers.ofString(subTask1PostJson))
                .build();
        HttpRequest requestPostSubTask2 = HttpRequest.newBuilder()
                .uri(URI.create(basicUri + "subtask/"))
                .version(HttpClient.Version.HTTP_1_1)
                .POST(HttpRequest.BodyPublishers.ofString(subTask2PostJson))
                .build();
        try {
            client.send(requestPostEpic1, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }

        try {
            client.send(requestPostSubTask1, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        try {
            client.send(requestPostSubTask2, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }

        HttpRequest requestGetListOfSubTasksId = HttpRequest.newBuilder()
                .uri(URI.create(basicUri + "subtask/epic?id=" + epic1Id))
                .version(HttpClient.Version.HTTP_1_1)
                .GET()
                .build();
        Type arrayListIntegerType = new TypeToken<ArrayList<Integer>>(){}.getType();
        ArrayList<Integer> receivedListOfSubTasksId = null;
        try {
            HttpResponse<String> responseGetListOfSubTasksId = client
                    .send(requestGetListOfSubTasksId, HttpResponse.BodyHandlers.ofString());
            if (responseGetListOfSubTasksId.statusCode() == 200) {
                String ListOfSubTasksIdJson = responseGetListOfSubTasksId.body();
                JsonElement jsonElement = JsonParser.parseString(ListOfSubTasksIdJson);
                receivedListOfSubTasksId = gson.fromJson(jsonElement, arrayListIntegerType);
            } else {
                System.out.println("Что-то пошло не так при запросе с сервера.");
                System.out.println("Сервер вернул код состояния: " + responseGetListOfSubTasksId.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }

        assertNotNull(receivedListOfSubTasksId, "Полученный список пуст.");
        assertEquals(2, receivedListOfSubTasksId.size(), "Размер не совпадает.");
        assertEquals(subTask1Id, receivedListOfSubTasksId.get(0), "ID не совпадает.");
    }

    @Test
    void shouldClearSubTasksMap() {
        int epic1Id = taskManager.addEpic(epic1);
        SubTask subTask1 = new SubTask("Title for SubTask-1", "Description for SubTask-1",
                epic1Id, dateTime1, duration10);
        int subTask1Id = taskManager.addSubTask(subTask1);
        String epic1PostJson = gson.toJson(taskManager.getEpic(epic1Id));
        String subTask1PostJson = gson.toJson(taskManager.getSubTask(subTask1Id));
        HttpRequest requestPostEpic1 = HttpRequest.newBuilder()
                .uri(URI.create(basicUri + "epic/"))
                .version(HttpClient.Version.HTTP_1_1)
                .POST(HttpRequest.BodyPublishers.ofString(epic1PostJson))
                .build();
        HttpRequest requestPostSubTask1 = HttpRequest.newBuilder()
                .uri(URI.create(basicUri + "subtask/"))
                .version(HttpClient.Version.HTTP_1_1)
                .POST(HttpRequest.BodyPublishers.ofString(subTask1PostJson))
                .build();
        try {
            client.send(requestPostEpic1, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }

        try {
            client.send(requestPostSubTask1, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }

        HttpRequest requestDeleteSubTasks = HttpRequest.newBuilder()
                .uri(URI.create(basicUri + "subtask/"))
                .version(HttpClient.Version.HTTP_1_1)
                .DELETE()
                .build();
        try {
            client.send(requestDeleteSubTasks, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }

        HttpRequest requestGetSubTasks = HttpRequest.newBuilder()
                .uri(URI.create(basicUri + "subtask/"))
                .version(HttpClient.Version.HTTP_1_1)
                .GET()
                .build();
        HashMap<Integer, SubTask> receivedSubTasks = null;
        try {
            HttpResponse<String> responseGetSubTasks = client.send(requestGetSubTasks, HttpResponse.BodyHandlers.ofString());
            if (responseGetSubTasks.statusCode() == 200) {
                String tasksJson = responseGetSubTasks.body();
                JsonElement jsonElement = JsonParser.parseString(tasksJson);
                if (!jsonElement.isJsonObject()) {
                    throw new RuntimeException("Получен не JsonObject.");
                }
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                receivedSubTasks = gson.fromJson(jsonObject, subTasksType);
            } else {
                System.out.println("Что-то пошло не так при запросе с сервера.");
                System.out.println("Сервер вернул код состояния: " + responseGetSubTasks.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }

        assert receivedSubTasks != null;
        assertEquals(0, receivedSubTasks.size(), "Полученная HashMap не пуста.");
    }

    @Test
    void shouldDeleteSubTaskById() {
        int epic1Id = taskManager.addEpic(epic1);
        SubTask subTask1 = new SubTask("Title for SubTask-1", "Description for SubTask-1",
                epic1Id, dateTime1, duration10);
        int subTask1Id = taskManager.addSubTask(subTask1);
        String epic1PostJson = gson.toJson(taskManager.getEpic(epic1Id));
        String subTask1PostJson = gson.toJson(taskManager.getSubTask(subTask1Id));
        HttpRequest requestPostEpic1 = HttpRequest.newBuilder()
                .uri(URI.create(basicUri + "epic/"))
                .version(HttpClient.Version.HTTP_1_1)
                .POST(HttpRequest.BodyPublishers.ofString(epic1PostJson))
                .build();
        HttpRequest requestPostSubTask1 = HttpRequest.newBuilder()
                .uri(URI.create(basicUri + "subtask/"))
                .version(HttpClient.Version.HTTP_1_1)
                .POST(HttpRequest.BodyPublishers.ofString(subTask1PostJson))
                .build();
        try {
            client.send(requestPostEpic1, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }

        try {
            client.send(requestPostSubTask1, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        HttpRequest requestDeleteSubTaskById = HttpRequest.newBuilder()
                .uri(URI.create(basicUri + "subtask/?id=" + subTask1Id))
                .version(HttpClient.Version.HTTP_1_1)
                .DELETE()
                .build();
        try {
            client.send(requestDeleteSubTaskById, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }

        HttpRequest requestGetSubTaskById = HttpRequest.newBuilder()
                .uri(URI.create(basicUri + "subtask/?id=" + subTask1Id))
                .version(HttpClient.Version.HTTP_1_1)
                .GET()
                .build();
        SubTask receivedSubTask = null;
        HttpResponse<String> responseGetSubTaskById = null;
        try {
            responseGetSubTaskById = client.send(requestGetSubTaskById, HttpResponse.BodyHandlers.ofString());
            if (responseGetSubTaskById.statusCode() == 200) {
                String SubTask1Json = responseGetSubTaskById.body();
                JsonElement jsonElement = JsonParser.parseString(SubTask1Json);
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                receivedSubTask = gson.fromJson(jsonObject, SubTask.class);
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }

        assertNull(receivedSubTask, "Полученная задача не пуста.");
        assert responseGetSubTaskById != null;
        assertEquals(400, responseGetSubTaskById.statusCode(), "Код ответа сервера не совпадает.");
    }

    @Test
    void shouldClearEpicsMapAndSubTasksMap() {
        int epic1Id = taskManager.addEpic(epic1);
        SubTask subTask1 = new SubTask("Title for SubTask-1", "Description for SubTask-1",
                epic1Id, dateTime1, duration10);
        int subTask1Id = taskManager.addSubTask(subTask1);
        String epic1PostJson = gson.toJson(taskManager.getEpic(epic1Id));
        String subTask1PostJson = gson.toJson(taskManager.getSubTask(subTask1Id));
        HttpRequest requestPostEpic1 = HttpRequest.newBuilder()
                .uri(URI.create(basicUri + "epic/"))
                .version(HttpClient.Version.HTTP_1_1)
                .POST(HttpRequest.BodyPublishers.ofString(epic1PostJson))
                .build();
        HttpRequest requestPostSubTask1 = HttpRequest.newBuilder()
                .uri(URI.create(basicUri + "subtask/"))
                .version(HttpClient.Version.HTTP_1_1)
                .POST(HttpRequest.BodyPublishers.ofString(subTask1PostJson))
                .build();
        try {
            client.send(requestPostEpic1, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }

        try {
            client.send(requestPostSubTask1, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }

        HttpRequest requestDeleteEpics = HttpRequest.newBuilder()
                .uri(URI.create(basicUri + "epic/"))
                .version(HttpClient.Version.HTTP_1_1)
                .DELETE()
                .build();
        try {
            client.send(requestDeleteEpics, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }

        HttpRequest requestGetEpics = HttpRequest.newBuilder()
                .uri(URI.create(basicUri + "epic/"))
                .version(HttpClient.Version.HTTP_1_1)
                .GET()
                .build();
        HashMap<Integer, Epic> receivedEpics = null;
        try {
            HttpResponse<String> responseGetEpics = client.send(requestGetEpics, HttpResponse.BodyHandlers.ofString());
            if (responseGetEpics.statusCode() == 200) {
                String epicsJson = responseGetEpics.body();
                JsonElement jsonElement = JsonParser.parseString(epicsJson);
                if (!jsonElement.isJsonObject()) {
                    throw new RuntimeException("Получен не JsonObject.");
                }
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                receivedEpics = gson.fromJson(jsonObject, epicsType);
            } else {
                System.out.println("Что-то пошло не так при запросе с сервера.");
                System.out.println("Сервер вернул код состояния: " + responseGetEpics.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        HttpRequest requestGetSubTasks = HttpRequest.newBuilder()
                .uri(URI.create(basicUri + "subtask/"))
                .version(HttpClient.Version.HTTP_1_1)
                .GET()
                .build();
        HashMap<Integer, SubTask> receivedSubTasks = null;
        try {
            HttpResponse<String> responseGetSubTasks = client.send(requestGetSubTasks, HttpResponse.BodyHandlers.ofString());
            if (responseGetSubTasks.statusCode() == 200) {
                String tasksJson = responseGetSubTasks.body();
                JsonElement jsonElement = JsonParser.parseString(tasksJson);
                if (!jsonElement.isJsonObject()) {
                    throw new RuntimeException("Получен не JsonObject.");
                }
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                receivedSubTasks = gson.fromJson(jsonObject, subTasksType);
            } else {
                System.out.println("Что-то пошло не так при запросе с сервера.");
                System.out.println("Сервер вернул код состояния: " + responseGetSubTasks.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }

        assert receivedEpics != null;
        assertEquals(0, receivedEpics.size(), "Полученная HashMap с эпиками не пуста.");
        assert receivedSubTasks != null;
        assertEquals(0, receivedSubTasks.size(), "Полученная HashMap с сабТасками не пуста.");
    }

    @Test
    void shouldDeleteEpicByIdAndItsSubTasks() {
        int epic1Id = taskManager.addEpic(epic1);
        SubTask subTask1 = new SubTask("Title for SubTask-1", "Description for SubTask-1",
                epic1Id, dateTime1, duration10);
        int subTask1Id = taskManager.addSubTask(subTask1);
        String epic1PostJson = gson.toJson(taskManager.getEpic(epic1Id));
        String subTask1PostJson = gson.toJson(taskManager.getSubTask(subTask1Id));
        HttpRequest requestPostEpic1 = HttpRequest.newBuilder()
                .uri(URI.create(basicUri + "epic/"))
                .version(HttpClient.Version.HTTP_1_1)
                .POST(HttpRequest.BodyPublishers.ofString(epic1PostJson))
                .build();
        HttpRequest requestPostSubTask1 = HttpRequest.newBuilder()
                .uri(URI.create(basicUri + "subtask/"))
                .version(HttpClient.Version.HTTP_1_1)
                .POST(HttpRequest.BodyPublishers.ofString(subTask1PostJson))
                .build();
        try {
            client.send(requestPostEpic1, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }

        try {
            client.send(requestPostSubTask1, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        HttpRequest requestDeleteEpicById = HttpRequest.newBuilder()
                .uri(URI.create(basicUri + "epic/?id=" + epic1Id))
                .version(HttpClient.Version.HTTP_1_1)
                .DELETE()
                .build();
        try {
            client.send(requestDeleteEpicById, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }

        HttpRequest requestGetEpicById = HttpRequest.newBuilder()
                .uri(URI.create(basicUri + "epic/?id=" + subTask1Id))
                .version(HttpClient.Version.HTTP_1_1)
                .GET()
                .build();
        Epic receivedEpic = null;
        HttpResponse<String> responseGetEpicById = null;
        try {
            responseGetEpicById = client.send(requestGetEpicById, HttpResponse.BodyHandlers.ofString());
            if (responseGetEpicById.statusCode() == 200) {
                String SubTask1Json = responseGetEpicById.body();
                JsonElement jsonElement = JsonParser.parseString(SubTask1Json);
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                receivedEpic = gson.fromJson(jsonObject, Epic.class);
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }

        HttpRequest requestGetSubTaskById = HttpRequest.newBuilder()
                .uri(URI.create(basicUri + "subtask/?id=" + subTask1Id))
                .version(HttpClient.Version.HTTP_1_1)
                .GET()
                .build();
        SubTask receivedSubTask = null;
        HttpResponse<String> responseGetSubTaskById = null;
        try {
            responseGetSubTaskById = client.send(requestGetSubTaskById, HttpResponse.BodyHandlers.ofString());
            if (responseGetSubTaskById.statusCode() == 200) {
                String SubTask1Json = responseGetSubTaskById.body();
                JsonElement jsonElement = JsonParser.parseString(SubTask1Json);
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                receivedSubTask = gson.fromJson(jsonObject, SubTask.class);
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }


        assertNull(receivedEpic, "Полученный эпик не пуст.");
        assert responseGetEpicById != null;
        assertEquals(400, responseGetEpicById.statusCode(), "Код ответа сервера не совпадает.");
        assertNull(receivedSubTask, "У удаленного эпика остался сабтаск.");
        assert responseGetSubTaskById != null;
        assertEquals(400, responseGetSubTaskById.statusCode(), "Код ответа сервера не совпадает.");
    }

    @Test
    void shouldReturnListOfTasksByHistory() {
        int task1Id = taskManager.addTask(task1);
        int task2Id = taskManager.addTask(task2);
        int epic1Id = taskManager.addEpic(epic1);
        String task1PostJson = gson.toJson(taskManager.getTask(task1Id));
        String task2PostJson = gson.toJson(taskManager.getTask(task2Id));
        String epic1PostJson = gson.toJson(taskManager.getEpic(epic1Id));
        HttpRequest requestPostTask1 = HttpRequest.newBuilder()
                .uri(URI.create(basicUri + "task/"))
                .version(HttpClient.Version.HTTP_1_1)
                .POST(HttpRequest.BodyPublishers.ofString(task1PostJson))
                .build();
        HttpRequest requestPostTask2 = HttpRequest.newBuilder()
                .uri(URI.create(basicUri + "task/"))
                .version(HttpClient.Version.HTTP_1_1)
                .POST(HttpRequest.BodyPublishers.ofString(task2PostJson))
                .build();
        HttpRequest requestPostEpic1 = HttpRequest.newBuilder()
                .uri(URI.create(basicUri + "epic/"))
                .version(HttpClient.Version.HTTP_1_1)
                .POST(HttpRequest.BodyPublishers.ofString(epic1PostJson))
                .build();
        try {
            client.send(requestPostTask1, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }

        try {
            client.send(requestPostTask2, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        try {
            client.send(requestPostEpic1, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }

        HttpRequest requestGetEpic1 = HttpRequest.newBuilder()
                .uri(URI.create(basicUri + "epic/?id=" + epic1Id))
                .version(HttpClient.Version.HTTP_1_1)
                .GET()
                .build();
        HttpRequest requestGetTask2 = HttpRequest.newBuilder()
                .uri(URI.create(basicUri + "task/?id=" + task2Id))
                .version(HttpClient.Version.HTTP_1_1)
                .GET()
                .build();
        HttpRequest requestGetTask1 = HttpRequest.newBuilder()
                .uri(URI.create(basicUri + "task/?id=" + task1Id))
                .version(HttpClient.Version.HTTP_1_1)
                .GET()
                .build();
        try {
            HttpResponse<String> responseGetEpic1 = client.send(requestGetEpic1, HttpResponse.BodyHandlers.ofString());
            if (responseGetEpic1.statusCode() == 200) {
                System.out.println("Эпик №1 успешно запрошен.");
            } else {
                System.out.println("Что-то пошло не так при запросе с сервера.");
                System.out.println("Сервер вернул код состояния: " + responseGetEpic1.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        try {
            HttpResponse<String> responseGetTask2 = client.send(requestGetTask2, HttpResponse.BodyHandlers.ofString());
            if (responseGetTask2.statusCode() == 200) {
                System.out.println("Таск №2 успешно запрошен.");
            } else {
                System.out.println("Что-то пошло не так при запросе с сервера.");
                System.out.println("Сервер вернул код состояния: " + responseGetTask2.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        try {
            HttpResponse<String> responseGetTask1 = client.send(requestGetTask1, HttpResponse.BodyHandlers.ofString());
            if (responseGetTask1.statusCode() == 200) {
                System.out.println("Таск №1 успешно запрошен.");
            } else {
                System.out.println("Что-то пошло не так при запросе с сервера.");
                System.out.println("Сервер вернул код состояния: " + responseGetTask1.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        HttpRequest requestGetHistory = HttpRequest.newBuilder()
                .uri(URI.create(basicUri + "history/"))
                .version(HttpClient.Version.HTTP_1_1)
                .GET()
                .build();
        ArrayList<Task> receivedHistory = new ArrayList<>();
        Type arrayListTaskType = new TypeToken<ArrayList<Task>>(){}.getType();
        try {
            HttpResponse<String> responseGetHistory = client.send(requestGetHistory, HttpResponse.BodyHandlers.ofString());
            if (responseGetHistory.statusCode() == 200) {
                String historyJson = responseGetHistory.body();
                JsonElement jsonElement = JsonParser.parseString(historyJson);
                receivedHistory = gson.fromJson(jsonElement, arrayListTaskType);
            } else {
                System.out.println("Что-то пошло не так при запросе с сервера.");
                System.out.println("Сервер вернул код состояния: " + responseGetHistory.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }

        assertNotNull(receivedHistory, "Полученный список истории пуст.");
        assertEquals(3, receivedHistory.size(), "Размер полученного списка отличается от ожидаемого.");
        assertEquals(epic1Id, receivedHistory.get(0).getId(), "Порядок вызовов не совпадает с ожидаемым.");
        assertEquals(task1Id,
                receivedHistory.get(receivedHistory.size() - 1).getId(),
                "Порядок вызовов не совпадает с ожидаемым.");
    }

    @Test
    void shouldReturnListOfPrioritizedTasks() {
        int task1Id = taskManager.addTask(task1);
        int task2Id = taskManager.addTask(task2);
        int epic1Id = taskManager.addEpic(epic1);
        String task1PostJson = gson.toJson(taskManager.getTask(task1Id));
        String task2PostJson = gson.toJson(taskManager.getTask(task2Id));
        String epic1PostJson = gson.toJson(taskManager.getEpic(epic1Id));
        HttpRequest requestPostTask1 = HttpRequest.newBuilder()
                .uri(URI.create(basicUri + "task/"))
                .version(HttpClient.Version.HTTP_1_1)
                .POST(HttpRequest.BodyPublishers.ofString(task1PostJson))
                .build();
        HttpRequest requestPostTask2 = HttpRequest.newBuilder()
                .uri(URI.create(basicUri + "task/"))
                .version(HttpClient.Version.HTTP_1_1)
                .POST(HttpRequest.BodyPublishers.ofString(task2PostJson))
                .build();
        HttpRequest requestPostEpic1 = HttpRequest.newBuilder()
                .uri(URI.create(basicUri + "epic/"))
                .version(HttpClient.Version.HTTP_1_1)
                .POST(HttpRequest.BodyPublishers.ofString(epic1PostJson))
                .build();
        try {
            client.send(requestPostTask1, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }

        try {
            client.send(requestPostTask2, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }
        try {
            client.send(requestPostEpic1, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }

        HttpRequest requestGetListOfPrioritizedTasks = HttpRequest.newBuilder()
                .uri(basicUri)
                .version(HttpClient.Version.HTTP_1_1)
                .GET()
                .build();
        ArrayList<Task> receivedPrioritized = new ArrayList<>();
        Type arrayListTaskType = new TypeToken<ArrayList<Task>>(){}.getType();
        try {
            HttpResponse<String> responseGetListOfPrioritizedTasks =
                    client.send(requestGetListOfPrioritizedTasks, HttpResponse.BodyHandlers.ofString());
            if (responseGetListOfPrioritizedTasks.statusCode() == 200) {
                String prioritizedJson = responseGetListOfPrioritizedTasks.body();
                JsonElement jsonElement = JsonParser.parseString(prioritizedJson);
                receivedPrioritized = gson.fromJson(jsonElement, arrayListTaskType);
            } else {
                System.out.println("Что-то пошло не так при запросе с сервера.");
                System.out.println("Сервер вернул код состояния: " + responseGetListOfPrioritizedTasks.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }

        assertNotNull(receivedPrioritized, "Полученный список пуст.");
        assertEquals(2, receivedPrioritized.size(), "Размер полученного списка отличается от ожидаемого.");
    }
}