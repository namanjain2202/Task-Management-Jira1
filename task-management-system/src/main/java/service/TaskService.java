package service;

import factory.TaskFactory;
import model.Task;
import model.TaskStatus;
import repository.TaskRepository;
import exception.TaskNotFoundException;
import exception.TaskManagementException;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

public class TaskService {
    private final TaskRepository taskRepo = new TaskRepository();

    public Task createTask(String title, String description, Date deadline, String userId) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("Task title cannot be empty");
        }
        if (deadline == null) {
            throw new IllegalArgumentException("Task deadline cannot be null");
        }
        Task task = TaskFactory.createTask(title, description, deadline);
        task.setAssignedUserId(userId);
        taskRepo.save(task);
        return task;
    }

    public Task createSubtask(String parentTaskId, String title, String description, Date deadline, String userId) {
        Task parent = taskRepo.findById(parentTaskId);
        if (parent == null) {
            throw new TaskNotFoundException("Parent task not found with id: " + parentTaskId);
        }
        Task subtask = TaskFactory.createTask(title, description, deadline);
        subtask.setAssignedUserId(userId);
        parent.addSubtask(subtask);
        taskRepo.save(subtask);
        return subtask;
    }

    public void updateTask(String taskId, String title, String description, Date deadline, TaskStatus status) {
        Task task = taskRepo.findById(taskId);
        if (task == null) {
            throw new TaskNotFoundException("Task not found with id: " + taskId);
        }
        task.update(title, description, deadline, status);
    }

    public void deleteTask(String taskId) {
        Task task = taskRepo.findById(taskId);
        if (task == null) {
            throw new TaskNotFoundException("Task not found with id: " + taskId);
        }
        // Remove from parent if exists
        String parentTaskId = task.getParentTaskId();
        if (parentTaskId != null) {
            Task parent = taskRepo.findById(parentTaskId);
            if (parent != null) {
                parent.removeSubtask(task);
            }
        }
        taskRepo.delete(taskId);
    }

    public void moveTask(String taskId, String newParentTaskId) {
        Task task = getTaskById(taskId);
        
        // Check for circular dependency
        if (newParentTaskId != null) {
            Task newParent = getTaskById(newParentTaskId);
            if (taskId.equals(newParentTaskId)) {
                throw new TaskManagementException("Cannot move task to itself");
            }
            // Check if new parent is a descendant of the task
            String currentParentId = newParent.getParentTaskId();
            while (currentParentId != null) {
                if (currentParentId.equals(taskId)) {
                    throw new TaskManagementException("Circular dependency detected");
                }
                Task currentParent = getTaskById(currentParentId);
                currentParentId = currentParent.getParentTaskId();
            }
        }

        // Remove from current parent if exists
        String currentParentId = task.getParentTaskId();
        if (currentParentId != null) {
            Task currentParent = getTaskById(currentParentId);
            currentParent.removeSubtask(task);
        }

        // Add to new parent if specified
        if (newParentTaskId != null) {
            Task newParent = getTaskById(newParentTaskId);
            newParent.addSubtask(task);
        }
    }

    public List<Task> getTasksByUser(String userId) {
        List<Task> allTasks = new ArrayList<>(taskRepo.findAll());
        List<Task> userTasks = new ArrayList<>();
        for (Task task : allTasks) {
            if (userId.equals(task.getAssignedUserId())) {
                userTasks.add(task);
            }
        }
        return userTasks;
    }

    public Task getTaskById(String taskId) {
        Task task = taskRepo.findById(taskId);
        if (task == null) {
            throw new TaskNotFoundException("Task not found with id: " + taskId);
        }
        return task;
    }

    public void updateTaskStatus(String taskId, TaskStatus newStatus) {
        Task task = taskRepo.findById(taskId);
        if (task == null) throw new RuntimeException("Task not found!");
        task.update(task.getTitle(), task.getDescription(), task.getDeadline(), newStatus);
        taskRepo.save(task);
    }

    public TaskRepository getTaskRepo() {
        return taskRepo;
    }
}

