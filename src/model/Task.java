package model;

import java.util.Objects;

public class Task {
    private String title;
    private String extraInfo;
    private int id;
    private Status taskStatus;


    public Task(String title, String extraInfo) {   // Конструктор — название и инфо
        this.title = title;
        this.extraInfo = extraInfo;
        this.taskStatus = Status.NEW;
    }

    public Task(String title, String extraInfo, Status taskStatus) {    // Конструктор — название, инфо и статус
        this.title = title;
        this.extraInfo = extraInfo;
        this.taskStatus = taskStatus;
    }

    public Task(int id, String title, String extraInfo, Status taskStatus) { // Айди, название, инфо и статус
        this.id = id;
        this.title = title;
        this.extraInfo = extraInfo;
        this.taskStatus = taskStatus;
    }

    public Task(int id, String title, String extraInfo) { // Айди, название и инфо
        this.id = id;
        this.title = title;
        this.extraInfo = extraInfo;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setExtraInfo(String extraInfo) {
        this.extraInfo = extraInfo;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTaskStatus(Status taskStatus) {
        this.taskStatus = taskStatus;
    }

    public String getTitle() {
        return title;
    }

    public String getExtraInfo() {
        return extraInfo;
    }

    public int getId() {
        return id;
    }

    public Status getTaskStatus() {
        return taskStatus;
    }

    @Override
    public String toString() {
        return "Task{" +
                "title='" + title + '\'' +
                ", extraInfo='" + extraInfo + '\'' +
                ", id=" + id +
                ", taskStatus=" + taskStatus +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Task task = (Task) o;
        return id == task.id && Objects.equals(title, task.title) && Objects.equals(extraInfo, task.extraInfo)
                && taskStatus == task.taskStatus;
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, extraInfo, id, taskStatus);
    }

}
