package main.java.manager.client;

import main.java.manager.exception.KVTaskClientLoadException;
import main.java.manager.exception.KVTaskClientPutException;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class KVTaskClient {
    private final URI uri;

    private String apiToken;
    private final HttpClient httpClient;

    public KVTaskClient(URI uri) throws IOException, InterruptedException {
        this.uri = uri;
        httpClient = HttpClient.newHttpClient();
        apiToken = register(uri);
    }

    private String register(URI receivedUri) throws IOException, InterruptedException {
        URI uri = URI.create(receivedUri.toString() + "register");
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .header("Content-Type", "application/json")
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new RuntimeException("Ошибка регистрации и получения токена.");
        }
        return response.body();
    }

    public void put(String key, String json) throws IOException, InterruptedException {
        URI uri = URI.create(this.uri + "save/" + key + "?API_TOKEN=" + apiToken);
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .version(HttpClient.Version.HTTP_1_1)
                .build();
        HttpResponse<String> response = null;
        try {
            response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException exception) {
            System.out.println("Ошибка отправки данных на сервер.");
        }

        assert response != null;
        if (response.statusCode() != 200) {
            throw new KVTaskClientPutException("Ошибка сохранения данных на сервер.");
        }
    }

    public String load(String key) throws IOException, InterruptedException {
        URI uri = URI.create(this.uri + "load/" + key + "?API_TOKEN=" + apiToken);
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .version(HttpClient.Version.HTTP_1_1)
                .header("Content-Type", "application/json")
                .build();
        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            System.out.println("Код ответа: " + response.statusCode());
            throw new KVTaskClientLoadException("Ошибка получения данных с сервера.");
        }
        return response.body();
    }

    public String getApiToken() {
        return apiToken;
    }

    public void setApiToken(String apiToken) {
        this.apiToken = apiToken;
    }
}
