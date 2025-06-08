package service;

import factory.TaskFactory;
import model.Task;
import repository.TaskRepository;

import java.util.Date;

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

    public void updateTask(String taskId, String title, String description, Date deadline, Task.Status status) {
        Task task = taskRepo.findById(taskId);
        if (task == null) throw new RuntimeException("Task not found!");
        task.update(title, description, deadline, status);
    }

    public void deleteTask(String taskId) {
        taskRepo.delete(taskId);
    }

    public void moveTask(String taskId, String newParentId) {
        Task task = taskRepo.findById(taskId);
        if (task == null) throw new RuntimeException("Task not found!");
        task.setParentId(newParentId);
    }

    public TaskRepository getTaskRepo() {
        return taskRepo;
    }
}
