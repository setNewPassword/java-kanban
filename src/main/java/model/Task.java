package main.java.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

public class Task {
    private String title;
    private String extraInfo;
    private int id;
    private Status taskStatus;
    private LocalDateTime startTime;
    private Duration duration;


    public Task(String title, String extraInfo) {   // Конструктор — название и инфо
        this.title = title;
        this.extraInfo = extraInfo;
        this.taskStatus = Status.NEW;
    }

    public Task(String title, String extraInfo, LocalDateTime startTime, Duration duration) {
        this.title = title;                         // Конструктор — название, инфо, старт и продолжительность
        this.extraInfo = extraInfo;
        this.taskStatus = Status.NEW;
        this.startTime = startTime;
        this.duration = duration;
    }

    public Task(String title, String extraInfo, Status taskStatus) {    // Конструктор — название, инфо и статус
        this.title = title;
        this.extraInfo = extraInfo;
        this.taskStatus = taskStatus;
    }

    public Task(String title, String extraInfo, Status taskStatus, LocalDateTime startTime, Duration duration) {
        this.title = title;                         // Конструктор — название, инфо, статус, старт и продолжительность
        this.extraInfo = extraInfo;
        this.taskStatus = taskStatus;
        this.startTime = startTime;
        this.duration = duration;
    }

    public Task(int id, String title, String extraInfo, Status taskStatus) { // Айди, название, инфо и статус
        this.id = id;
        this.title = title;
        this.extraInfo = extraInfo;
        this.taskStatus = taskStatus;
    }

    public Task(int id, String title, String extraInfo,
                Status taskStatus, LocalDateTime startTime, Duration duration) {    // Айди, название, инфо, статус,
        this.id = id;                                                               // стартТайм, продолжительность
        this.title = title;
        this.extraInfo = extraInfo;
        this.taskStatus = taskStatus;
        this.startTime = startTime;
        this.duration = duration;
    }

    public Task(int id, String title, String extraInfo) { // Айди, название и инфо
        this.id = id;
        this.title = title;
        this.extraInfo = extraInfo;
    }

    public Task(int id, String title, String extraInfo, LocalDateTime startTime, Duration duration) {
        this.id = id;
        this.title = title;
        this.extraInfo = extraInfo;
        this.startTime = startTime;
        this.duration = duration;
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

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
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

    public TaskType getTaskType() {
        return TaskType.TASK;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public Duration getDuration() {
        return duration;
    }

    public Optional<LocalDateTime> getEndTime(){
        if (startTime == null|| duration == null){
            return Optional.empty();
        }
        return Optional.of(startTime.plus(duration));
    }

    @Override
    public String toString() {
        return "Task{" +
                "title='" + title + '\'' +
                ", extraInfo='" + extraInfo + '\'' +
                ", id=" + id +
                ", taskStatus=" + taskStatus +
                ", startTime=" + startTime +
                ", duration=" + duration +
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
                && taskStatus == task.taskStatus && Objects.equals(startTime, task.startTime)
                && Objects.equals(duration, task.duration);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, extraInfo, id, taskStatus, startTime, duration);
    }

}
