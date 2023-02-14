package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class SubTask extends Task {
    private final int epicID; // Поле хранит ID эпика, к которому относится подзадача

    public SubTask(String title, String extraInfo, int epicID) {
        super(title, extraInfo, Status.NEW);
        this.epicID = epicID;
    }

    public SubTask(String title, String extraInfo, int epicID, LocalDateTime startTime, Duration duration) {
        super(title, extraInfo, Status.NEW, startTime, duration);
        this.epicID = epicID;
    }

    public SubTask(int id, String title, String extraInfo, int epicID) {
        super(id, title, extraInfo, Status.NEW);
        this.epicID = epicID;
    }

    public SubTask(int id, String title, String extraInfo, Status subTaskStatus, int epicID) {
        super(id, title, extraInfo, subTaskStatus);
        this.epicID = epicID;
    }

    public SubTask(int id, String title, String extraInfo,
                   Status subTaskStatus, int epicID, LocalDateTime startTime, Duration duration) {
        super(id, title, extraInfo, subTaskStatus, startTime, duration);
        this.epicID = epicID;
    }

    public int getEpicID() {
        return epicID;
    }

    public TaskType getTaskType() {
        return TaskType.SUBTASK;
    }
    @Override
    public String toString() {
        return "SubTask{" + "id=" + super.getId() +
                ", epicID=" + epicID +
                ", title='" + super.getTitle() + '\'' +
                ", extraInfo='" + super.getExtraInfo() + '\'' +
                ", taskStatus=" + super.getTaskStatus() +
                ", startTime=" + super.getStartTime() +
                ", duration=" + super.getDuration() +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SubTask subTask = (SubTask) o;
        return epicID == subTask.epicID;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), epicID);
    }
}
