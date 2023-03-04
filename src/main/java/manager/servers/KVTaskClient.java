package main.java.manager.servers;

import javax.net.ssl.SSLSession;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

public class KVTaskClient {
    private final URI uri;

    private String apiToken;
    private final HttpClient httpClient;

    public KVTaskClient(URI uri) throws IOException, InterruptedException {
        this.uri = uri;
        httpClient = HttpClient.newHttpClient();
        apiToken = register(httpClient, uri);
    }

    private String register(HttpClient httpClient, URI receivedUri) throws IOException, InterruptedException {
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
        HttpResponse<String> response = new HttpResponse<String>() {
            @Override
            public int statusCode() {
                return 0;
            }

            @Override
            public HttpRequest request() {
                return null;
            }

            @Override
            public Optional<HttpResponse<String>> previousResponse() {
                return Optional.empty();
            }

            @Override
            public HttpHeaders headers() {
                return null;
            }

            @Override
            public String body() {
                return null;
            }

            @Override
            public Optional<SSLSession> sslSession() {
                return Optional.empty();
            }

            @Override
            public URI uri() {
                return null;
            }

            @Override
            public HttpClient.Version version() {
                return null;
            }
        };
        try {
            response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        } catch (IOException exception) {
            System.out.println(response);
        }

        if (response.statusCode() != 200) {
            throw new RuntimeException("Ошибка сохранения данных на сервер.");
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
            throw new RuntimeException("Ошибка получения данных с сервера.");
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
