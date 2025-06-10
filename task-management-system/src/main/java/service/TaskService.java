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
        Task task = TaskFactory.createTask(title, description, deadline);
        task.setAssignedUserId(userId);
        taskRepo.save(task);
        return task;
    }

    public Task createSubtask(String parentTaskId, String title, String description, Date deadline, String userId) {
        Task parent = taskRepo.findById(parentTaskId);
        if (parent == null) throw new RuntimeException("Parent task not found!");
        Task subtask = TaskFactory.createTask(title, description, deadline);
        subtask.setAssignedUserId(userId);
        subtask.setParentId(parent.getId());
        parent.addSubtask(subtask);
        taskRepo.save(subtask);
        return subtask;
    }

    public void updateTask(String taskId, String title, String description, Date deadline, TaskStatus status) {
        Task task = taskRepo.findById(taskId);
        if (task == null) throw new RuntimeException("Task not found!");
        task.update(title, description, deadline, status);
    }

    public void deleteTask(String taskId) {
        taskRepo.delete(taskId);
    }

    public void moveTask(String taskId, String newParentId) {
        Task task = taskRepo.findById(taskId);
        if (task == null) throw new TaskNotFoundException("Task not found!");

        // Check if new parent exists (if provided)
        if (newParentId != null) {
            Task newParent = taskRepo.findById(newParentId);
            if (newParent == null) throw new TaskNotFoundException("New parent task not found!");
            
            // Prevent circular dependency
            if (isCircularDependency(task, newParent)) {
                throw new TaskManagementException("Circular dependency detected!");
            }
        }

        // Remove from old parent's subtasks if exists
        String oldParentId = task.getParentId();
        if (oldParentId != null) {
            Task oldParent = taskRepo.findById(oldParentId);
            if (oldParent != null) {
                oldParent.removeSubtask(task);
            }
        }

        // Add to new parent's subtasks if provided
        if (newParentId != null) {
            Task newParent = taskRepo.findById(newParentId);
            newParent.addSubtask(task);
        }

        // Update task's parent reference
        task.setParentId(newParentId);
        taskRepo.save(task);
    }

    private boolean isCircularDependency(Task task, Task newParent) {
        String currentParentId = newParent.getParentId();
        while (currentParentId != null) {
            if (currentParentId.equals(task.getId())) {
                return true;
            }
            Task currentParent = taskRepo.findById(currentParentId);
            if (currentParent == null) break;
            currentParentId = currentParent.getParentId();
        }
        return false;
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
        return taskRepo.findById(taskId);
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
