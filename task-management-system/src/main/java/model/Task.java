package model;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class Task {
    private String id;
    private String title;
    private String description;
    private Date deadline;
    private TaskStatus status;
    private TaskPriority priority;
    private String assignedUserId;
    private String parentTaskId; // Changed from parentId to parentTaskId to be more explicit
    private List<Task> subtasks;

    private final ReentrantLock lock = new ReentrantLock();

    public Task(String title, String description, Date deadline) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.description = description;
        this.deadline = deadline;
        this.status = TaskStatus.PENDING;
        this.priority = TaskPriority.MEDIUM;
        this.subtasks = new ArrayList<>();
    }

    public void addSubtask(Task subtask) {
        lock.lock();
        try {
            if (subtask.getParentTaskId() != null) {
                throw new IllegalStateException("Task already has a parent");
            }
            subtask.setParentTaskId(this.id);
            subtasks.add(subtask);
        } finally {
            lock.unlock();
        }
    }

    public void removeSubtask(Task subtask) {
        lock.lock();
        try {
            if (subtasks.remove(subtask)) {
                subtask.setParentTaskId(null);
            }
        } finally {
            lock.unlock();
        }
    }

    public String getId() {
        return id;
    }

    public void update(String title, String description, Date deadline, TaskStatus status) {
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

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Date getDeadline() {
        return deadline;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public String getAssignedUserId() {
        return assignedUserId;
    }

    public void setAssignedUserId(String userId) {
        this.assignedUserId = userId;
    }

    public List<String> getSubtasks() {
        lock.lock();
        try {
            return subtasks.stream()
                    .map(Task::getId)
                    .collect(java.util.stream.Collectors.toList());
        } finally {
            lock.unlock();
        }
    }

    public void setParentTaskId(String parentTaskId) {
        this.parentTaskId = parentTaskId;
    }

    public String getParentTaskId() {
        return parentTaskId;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", status=" + status +
                ", assignedUserId='" + assignedUserId + '\'' +
                ", parentTaskId='" + parentTaskId + '\'' +
                ", subtasks=" + subtasks.size() +
                '}';
    }
}
