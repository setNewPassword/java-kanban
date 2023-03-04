package main.java.manager.servers;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import main.java.manager.Managers;
import main.java.manager.implementation.HttpTaskManager;
import main.java.model.Epic;
import main.java.model.SubTask;
import main.java.model.Task;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class HttpTaskServer {
    private static final int PORT = 8080;
    private static final String GET = "GET";
    private static final String POST = "POST";
    private static final String DELETE = "DELETE";
    HttpServer httpServer;

    private final HttpTaskManager taskManager = new HttpTaskManager(URI.create("http://localhost:8078/"));

    private static final Gson gson = Managers.getGson();

    public HttpTaskServer() throws IOException, InterruptedException {
    }

    public void startServer() {
        try {
            System.out.println("Запускаем HttpTaskServer на порту " + PORT + ".");
            httpServer = HttpServer.create();
            httpServer.bind(new InetSocketAddress(PORT), 0);
            httpServer.createContext("/tasks/task/", new TaskHandler());
            httpServer.createContext("/tasks/subtask/", new SubtaskHandler());
            httpServer.createContext("/tasks/epic/", new EpicHandler());
            httpServer.createContext("/tasks/history/", new HistoryHandler());
            httpServer.createContext("/tasks/", new PrioritizedTasksHandler());
            httpServer.start();
        } catch (IOException e) {
            System.out.println("Ошибка запуска сервера.");
        }
    }

    public void stopServer() {
        System.out.println("Останавливаем HttpTaskServer.");
        httpServer.stop(0);
    }

    class TaskHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            URI uri = httpExchange.getRequestURI();
            String stringUri = uri.toString();
            String method = httpExchange.getRequestMethod();

            try {
                switch (method) {
                    case GET:
                        if (stringUri.equals("/tasks/task/")) {
                            Map<Integer, Task> tasks = taskManager.getTasks();
                            String response = gson.toJson(tasks);
                            httpExchange.sendResponseHeaders(200, 0);
                            try (OutputStream outputStream = httpExchange.getResponseBody()) {
                                outputStream.write(response.getBytes());
                            }
                        } else if (stringUri.startsWith("/tasks/task/?id=")) {
                            String[] id = stringUri.split("=");
                            Task task = taskManager.getTask(Integer.parseInt(id[1]));
                            String response = gson.toJson(task);
                            httpExchange.sendResponseHeaders(200, 0);
                            try (OutputStream outputStream = httpExchange.getResponseBody()) {
                                outputStream.write(response.getBytes());
                            }
                        } else
                            throw new RuntimeException("Строка запроса составлена с ошибкой.");
                        break;

                    case POST:
                        InputStream inputStream = httpExchange.getRequestBody();
                        String jsonString = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                        JsonElement jsonElement = JsonParser.parseString(jsonString);
                        if (!jsonElement.isJsonObject()) {
                            throw new RuntimeException("Получен не JsonObject.");
                        }
                        JsonObject jsonObject = jsonElement.getAsJsonObject();
                        Task task = gson.fromJson(jsonObject, Task.class);
                        Map<Integer, Task> tasks = taskManager.getTasks();
                        if (stringUri.equals("/tasks/task/")) {
                            if (tasks.containsValue(task)) {
                                taskManager.updateTask(task);
                            } else {
                                taskManager.addTask(task);
                            }
                        }
                        httpExchange.sendResponseHeaders(201, 0);
                        httpExchange.close();
                        break;

                    case DELETE:
                        if (stringUri.equals("/tasks/task/")) {
                            taskManager.clearTasks();
                        } else if (stringUri.startsWith("/tasks/task/?id=")){
                            String[] id = stringUri.split("=");
                            taskManager.removeTask(Integer.parseInt(id[1]));
                        }
                        httpExchange.sendResponseHeaders(200, 0);
                        httpExchange.close();
                        break;

                    default:
                        throw new RuntimeException("Вызван неподдерживаемый метод.");
                }
            } catch (Throwable e) {
                httpExchange.sendResponseHeaders(400, 0);
                httpExchange.close();
            }
        }
    }

    class SubtaskHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            URI uri = httpExchange.getRequestURI();
            String stringUri = uri.toString();
            String method = httpExchange.getRequestMethod();
            try {
                switch (method) {
                    case GET:
                        if (stringUri.equals("/tasks/subtask/")) {
                            Map<Integer, SubTask> subTasks = taskManager.getSubTasks();
                            String response = gson.toJson(subTasks);
                            httpExchange.sendResponseHeaders(200, 0);
                            try (OutputStream outputStream = httpExchange.getResponseBody()) {
                                outputStream.write(response.getBytes());
                            }
                        } else if (stringUri.startsWith("/tasks/subtask/?id=")) {
                            String[] id = stringUri.split("=");
                            SubTask subTask = taskManager.getSubTask(Integer.parseInt(id[1]));
                            String response = gson.toJson(subTask);
                            httpExchange.sendResponseHeaders(200, 0);
                            try (OutputStream outputStream = httpExchange.getResponseBody()) {
                                outputStream.write(response.getBytes());
                            }
                        } else if (stringUri.startsWith("/tasks/subtask/epic?id=")) {
                            String[] id = stringUri.split("=");
                            List<Integer> SubTasksOfEpic = taskManager.getSubTaskList(Integer.parseInt(id[1]));
                            String response = gson.toJson(SubTasksOfEpic);
                            httpExchange.sendResponseHeaders(200, 0);
                            try (OutputStream outputStream = httpExchange.getResponseBody()) {
                                outputStream.write(response.getBytes());
                            }
                        } else
                            throw new RuntimeException("Строка запроса составлена с ошибкой.");
                        break;

                    case POST:
                        InputStream inputStream = httpExchange.getRequestBody();
                        String jsonString = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                        JsonElement jsonElement = JsonParser.parseString(jsonString);
                        if (!jsonElement.isJsonObject()) {
                            throw new RuntimeException("Получен не JsonObject.");
                        }
                        JsonObject jsonObject = jsonElement.getAsJsonObject();
                        SubTask subTask = gson.fromJson(jsonObject, SubTask.class);
                        Map<Integer, SubTask> subTasks = taskManager.getSubTasks();
                        if (stringUri.equals("/tasks/subtask/")) {
                            if (subTasks.containsValue(subTask)) {
                                taskManager.updateSubTask(subTask);
                            } else {
                                taskManager.addSubTask(subTask);
                            }
                        }
                        httpExchange.sendResponseHeaders(201, 0);
                        httpExchange.close();
                        break;

                    case DELETE:
                        if (stringUri.equals("/tasks/subtask/")) {
                            taskManager.clearSubTasks();
                        } else {
                            if (stringUri.startsWith("/tasks/subtask/?id=")) {
                                String[] mass = stringUri.split("=");
                                taskManager.removeSubTask(Integer.parseInt(mass[1]));
                            }
                        }
                        httpExchange.sendResponseHeaders(200, 0);
                        httpExchange.close();
                        break;

                    default:
                        throw new RuntimeException("Вызван неподдерживаемый метод.");
                }
            } catch (Throwable e) {
                httpExchange.sendResponseHeaders(400, 0);
                httpExchange.close();
            }
        }
    }

    class EpicHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            URI uri = httpExchange.getRequestURI();
            String stringUri = uri.toString();
            String method = httpExchange.getRequestMethod();
            try {
                switch (method) {
                    case GET:
                        if (stringUri.equals("/tasks/epic/")) {
                            Map<Integer, Epic> epicsMap = taskManager.getEpics();
                            String response = gson.toJson(epicsMap);
                            httpExchange.sendResponseHeaders(200, 0);
                            try (OutputStream outputStream = httpExchange.getResponseBody()) {
                                outputStream.write(response.getBytes());
                            }
                        } else if (stringUri.startsWith("/tasks/epic/?id=")) {
                            String[] id = stringUri.split("=");
                            Epic epic = taskManager.getEpic(Integer.parseInt(id[1]));
                            String response = gson.toJson(epic);
                            httpExchange.sendResponseHeaders(200, 0);
                            try (OutputStream outputStream = httpExchange.getResponseBody()) {
                                outputStream.write(response.getBytes());
                            }
                        } else
                            throw new RuntimeException("Строка запроса составлена с ошибкой.");
                        break;

                    case POST:
                        InputStream inputStream = httpExchange.getRequestBody();
                        String jsonString = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                        JsonElement jsonElement = JsonParser.parseString(jsonString);
                        if (!jsonElement.isJsonObject()) {
                            throw new RuntimeException("Получен не JsonObject.");
                        }
                        JsonObject jsonObject = jsonElement.getAsJsonObject();
                        Epic epic = gson.fromJson(jsonObject, Epic.class);
                        Map<Integer, Epic> epics = taskManager.getEpics();
                        if (stringUri.equals("/tasks/epic/")) {
                            if (epics.containsValue(epic)) {
                                taskManager.updateEpic(epic);
                            } else {
                                taskManager.addEpic(epic);
                            }
                        }
                        httpExchange.sendResponseHeaders(201, 0);
                        httpExchange.close();
                        break;

                    case DELETE:
                        if (stringUri.equals("/tasks/epic/")) {
                            taskManager.clearEpics();
                        } else {
                            if (stringUri.startsWith("/tasks/epic/?id=")) {
                                String[] mass = stringUri.split("=");
                                taskManager.removeEpic(Integer.parseInt(mass[1]));
                            }
                        }
                        httpExchange.sendResponseHeaders(200, 0);
                        httpExchange.close();
                        break;

                    default:
                        throw new RuntimeException("Вызван неподдерживаемый метод.");
                }
            } catch (Throwable e) {
                httpExchange.sendResponseHeaders(400, 0);
                httpExchange.close();
            }
        }
    }

    class HistoryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            URI uri = httpExchange.getRequestURI();
            String stringUri = uri.toString();
            String method = httpExchange.getRequestMethod();
            try {
                if (GET.equals(method)) {
                    if (stringUri.equals("/tasks/history/")) {
                        List<Task> history = taskManager.getHistory().getHistory();
                        String response = gson.toJson(history);
                        httpExchange.sendResponseHeaders(200, 0);
                        try (OutputStream outputStream = httpExchange.getResponseBody()) {
                            outputStream.write(response.getBytes());
                        }
                    } else
                        throw new RuntimeException("Строка запроса составлена с ошибкой.");
                } else {
                    throw new RuntimeException("Вызван неподдерживаемый метод.");
                }
            } catch (Exception e) {
                httpExchange.sendResponseHeaders(400, 0);
                httpExchange.close();
            }
        }
    }

    class PrioritizedTasksHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            URI uri = httpExchange.getRequestURI();
            String stringUri = uri.toString();
            String method = httpExchange.getRequestMethod();
            try {
                if (GET.equals(method)) {
                    if (stringUri.equals("/tasks/")) {
                        List<Task> prioritizedTasks = taskManager.getPrioritizedTasks();
                        String response = gson.toJson(prioritizedTasks);
                        httpExchange.sendResponseHeaders(200, 0);
                        try (OutputStream outputStream = httpExchange.getResponseBody()) {
                            outputStream.write(response.getBytes());
                        }
                    } else
                        throw new RuntimeException("Строка запроса составлена с ошибкой.");
                } else {
                    throw new RuntimeException("Вызван неподдерживаемый метод.");
                }
            } catch (Throwable e) {
                httpExchange.sendResponseHeaders(400, 0);
                httpExchange.close();
            }
        }
    }

}