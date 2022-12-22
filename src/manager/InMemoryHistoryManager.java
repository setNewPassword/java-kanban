package manager;

import manager.interfaces.HistoryManager;
import model.Task;

import java.util.ArrayList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    private final List<Task> requestHistory;

    public InMemoryHistoryManager() {
        this.requestHistory = new ArrayList<>();
    }

    @Override
    public void add(Task task) {
        while (requestHistory.size() > 9) {
            requestHistory.remove(0);
        }
        requestHistory.add(task);

    }

    @Override
    public List<Task> getRequestHistory() {
        return requestHistory;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("InMemoryHistoryManager{");
        for (Task task : requestHistory) {
                    result.append(task).append("; ");
                }
        result.append("}");
        return result.toString();
    }
}
