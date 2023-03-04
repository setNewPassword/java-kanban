package main.java.manager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import main.java.manager.implementation.FileBackedTasksManager;
import main.java.manager.implementation.HttpTaskManager;
import main.java.manager.implementation.InMemoryHistoryManager;
import main.java.manager.implementation.InMemoryTaskManager;
import main.java.manager.interfaces.HistoryManager;
import main.java.manager.interfaces.TaskManager;
import main.java.model.DurationAdapter;
import main.java.model.LocalDateTimeAdapter;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;

public class Managers {

    public static TaskManager getDefault() throws IOException, InterruptedException {
        return new HttpTaskManager(URI.create("http://localhost:8078/"));
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }

    public static FileBackedTasksManager getDefaultFBTM() {
        return new FileBackedTasksManager(new File("src/main/java/manager/implementation/Testing.csv"));
    }

    public static TaskManager getDefaultInMemoryTaskManager() {
        return new InMemoryTaskManager();
    }

    public static Gson getGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .setPrettyPrinting();
        return gsonBuilder.create();
    }

}
