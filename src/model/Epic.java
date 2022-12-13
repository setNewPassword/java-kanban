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

//    @Override
//    public void setTaskStatus(Status taskStatus) {
//        throw new RuntimeException("Ошибка: статус эпика нельзя установить принудительно!");
//    }

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
