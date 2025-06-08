package model;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class Task {
    public enum Status {PENDING, IN_PROGRESS, COMPLETED}

    private String id;
    private String title;
    private String description;
    private Date deadline;
    private Status status;
    private String assignedUserId;
    private String parentId; // can be Story/Task
    private List<Task> subtasks;

    private final ReentrantLock lock = new ReentrantLock();

    public Task(String title, String description, Date deadline) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.description = description;
        this.deadline = deadline;
        this.status = Status.PENDING;
        this.subtasks = new ArrayList<>();
    }

    public void addSubtask(Task subtask) {
        lock.lock();
        try {
            subtasks.add(subtask);
        } finally {
            lock.unlock();
        }
    }

    public String getId() {
        return id;
    }

    public void update(String title, String description, Date deadline, Status status) {
        lock.lock();
        try {
            this.title = title;
            this.description = description;
            this.deadline = deadline;
            this.status = status;
        } finally {
            lock.unlock();
        }
    }

    public List<Task> getSubtasks() {
        return subtasks;
    }

    public Status getStatus() {
        return status;
    }

    public void setAssignedUserId(String userId) {
        this.assignedUserId = userId;
    }

    public String getAssignedUserId() {
        return assignedUserId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getParentId() {
        return parentId;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", status=" + status +
                ", assignedUserId='" + assignedUserId + '\'' +
                ", subtasks=" + subtasks.size() +
                '}';
    }
}
