package model;

import java.util.Objects;

public class SubTask extends Task {
    private int epicID; // Поле хранит ID эпика, к которому относится подзадача

    public SubTask(String title, String extraInfo, int epicID) {
        super(title, extraInfo, Status.NEW);
        this.epicID = epicID;
    }

    public SubTask(int id, String title, String extraInfo, int epicID) {
        super(id, title, extraInfo, Status.NEW);
        this.epicID = epicID;
    }

    public int getEpicID() {
        return epicID;
    }

    public void setEpicID(int epicID) {
        this.epicID = epicID;
    }

    /*@Override
    public void setTaskStatus(Status taskStatus) {
        super.taskStatus = taskStatus;

    }*/

    @Override
    public String toString() {
        return "SubTask{" + "id=" + super.getId() +
                        ", epicID=" + epicID +
                        ", title='" + super.getTitle() + '\'' +
                        ", extraInfo='" + super.getExtraInfo() + '\'' +
                        ", taskStatus=" + super.getTaskStatus() + '}';
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
