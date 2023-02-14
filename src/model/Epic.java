package model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Optional;

public class Epic extends Task {
    private LocalDateTime endTime;
    private ArrayList<Integer> subTasksID = new ArrayList<>();      // Здесь хранится ID подзадач.


    public Epic(String title, String extraInfo) {
        super(title, extraInfo);
    }

    public Epic(int id, String title, String extraInfo) {
        super(id, title, extraInfo);
    }

    public ArrayList<Integer> getSubTasksID() {
        return subTasksID;
    }

    public void setSubTasksID(ArrayList<Integer> subTasksID) {
        this.subTasksID = subTasksID;
    }

    public TaskType getTaskType() {
        return TaskType.EPIC;
    }

    @Override
    public Optional<LocalDateTime> getEndTime() {
        if (endTime == null) {
            return Optional.empty();
        } else {
            return Optional.of(endTime);
        }
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getSubtasksString() {             // Возвращение строки сабтасков эпика через запятую
        if (subTasksID != null) {
            String result = "";
            for (int id : subTasksID) {
                result = String.join(",", Integer.toString(id));
            }
            return result;
        } else {
            return " ";
        }
    }
    @Override
    public String toString() {
        return "Epic{" +
                "title='" + super.getTitle() + '\'' +
                ", extraInfo='" + super.getExtraInfo() + '\'' +
                ", id=" + super.getId() +
                ", taskStatus=" + super.getTaskStatus() +
                ", subTasksID=" + subTasksID +
                ", startTime=" + super.getStartTime() +
                ", duration=" + super.getDuration() +
                ", endTime=" + endTime +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Epic epic = (Epic) o;
        return Objects.equals(subTasksID, epic.subTasksID) && Objects.equals(endTime, epic.endTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subTasksID, endTime);
    }
}
