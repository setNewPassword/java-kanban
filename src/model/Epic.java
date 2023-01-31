package model;

import java.util.ArrayList;
import java.util.Objects;

public class Epic extends Task {
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
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Epic epic = (Epic) o;
        return Objects.equals(subTasksID, epic.subTasksID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), subTasksID);
    }
}
