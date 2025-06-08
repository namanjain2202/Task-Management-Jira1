package controller;

import model.Task;
import service.TaskService;

import java.util.Date;
import java.util.List;

public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    public Task createTask(String title, String description, Date deadline, String userId) {
        try {
            return taskService.createTask(title, description, deadline, userId);
        } catch (Exception e) {
            System.out.println("Error creating task: " + e.getMessage());
            return null;
        }
    }

    public Task createSubtask(String parentTaskId, String title, String description, Date deadline, String userId) {
        try {
            return taskService.createSubtask(parentTaskId, title, description, deadline, userId);
        } catch (Exception e) {
            System.out.println("Error creating subtask: " + e.getMessage());
            return null;
        }
    }

    public List<Task> getTasksForUser(String userId) {
        try {
            return taskService.getTasksByUser(userId);
        } catch (Exception e) {
            System.out.println("Error fetching tasks: " + e.getMessage());
            return null;
        }
    }

    public void updateTask(String taskId, String title, String description, Date deadline, Task.Status status) {
        try {
            taskService.updateTask(taskId, title, description, deadline, status);
            System.out.println("Task updated successfully");
        } catch (Exception e) {
            System.out.println("Error updating task: " + e.getMessage());
        }
    }

    public void deleteTask(String taskId) {
        try {
            taskService.deleteTask(taskId);
            System.out.println("Task deleted successfully");
        } catch (Exception e) {
            System.out.println("Error deleting task: " + e.getMessage());
        }
    }

    public void moveTask(String taskId, String newParentId) {
        try {
            taskService.moveTask(taskId, newParentId);
            System.out.println("Task moved successfully");
        } catch (Exception e) {
            System.out.println("Error moving task: " + e.getMessage());
        }
    }
}
