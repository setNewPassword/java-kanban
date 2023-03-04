import com.google.gson.*;
import main.java.manager.Managers;
import main.java.manager.interfaces.TaskManager;
import main.java.manager.servers.HttpTaskServer;
import main.java.manager.servers.KVServer;
import main.java.model.Task;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
public class Main {

    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException {

        System.out.println("Поехали!");
        final URI basicUri = URI.create("http://localhost:8080/tasks/");
        HttpClient client = HttpClient.newHttpClient();
        Gson gson = Managers.getGson();
        KVServer kvServer = new KVServer();
        kvServer.start();
        HttpTaskServer httpTaskServer = new HttpTaskServer();
        httpTaskServer.startServer();
        TaskManager taskManagerMain = Managers.getDefaultInMemoryTaskManager();

        Task task1 = new Task("Выгулять собаку", "Бегать, дурачиться и валяться в снегу. Трям!");
        int task1ID = taskManagerMain.addTask(task1);
        System.out.println("Таск №1 добавлен в локальный менеджер.");

        String task1PostJson = gson.toJson(taskManagerMain.getTask(task1ID));

        HttpRequest requestPostTask1 = HttpRequest.newBuilder()
                .uri(URI.create(basicUri + "task/"))
                .version(HttpClient.Version.HTTP_1_1)
                .POST(HttpRequest.BodyPublishers.ofString(task1PostJson))
                .build();
        try {
            HttpResponse<String> responsePostTask1 = client.send(requestPostTask1, HttpResponse.BodyHandlers.ofString());
            if (responsePostTask1.statusCode() == 201) {
                System.out.println("HttpTaskServer успешно принял Таск №1.");
            } else {
                System.out.println("Таск №1 не принят сервером. Сервер вернул код состояния: " + responsePostTask1.statusCode());
                System.out.println("body: " + responsePostTask1.body().length());
                System.out.println("uri: " + responsePostTask1.uri());
                System.out.println("headers: " + responsePostTask1.headers());
                System.out.println("request: " + responsePostTask1.request());
            }
        } catch (IOException | InterruptedException e) { // обрабатываем ошибки отправки запроса
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }

        HttpRequest requestGetTask1 = HttpRequest.newBuilder()
                .uri(URI.create(basicUri + "task/?id=" + task1ID))
                .version(HttpClient.Version.HTTP_1_1)
                .GET()
                .build();

        try {
            HttpResponse<String> responseGetTask1 = client.send(requestGetTask1, HttpResponse.BodyHandlers.ofString());
            if (responseGetTask1.statusCode() == 200) {
                String task1Json = responseGetTask1.body();
                JsonElement jsonElement = JsonParser.parseString(task1Json);
                if (!jsonElement.isJsonObject()) {
                    throw new RuntimeException("Получен не JsonObject.");
                }
                JsonObject jsonObject = jsonElement.getAsJsonObject();
                Task receivedTask1 = gson.fromJson(jsonObject, Task.class);
                System.out.println("HttpTaskServer успешно вернул Таск №1.");
                System.out.println(receivedTask1);
            } else {
                System.out.println("Что-то пошло не так при запросе таска1 с сервера.");
                System.out.println("Сервер вернул код состояния: " + responseGetTask1.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Во время выполнения запроса возникла ошибка.\n" +
                    "Проверьте, пожалуйста, адрес и повторите попытку.");
        }

        httpTaskServer.stopServer();
        kvServer.stop();



    }

}
