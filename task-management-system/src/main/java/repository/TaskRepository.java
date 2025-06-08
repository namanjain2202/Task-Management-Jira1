package repository;

import model.Task;
import java.util.List;

public interface TaskRepository {
    Task createTask(Task task);
    Task updateTask(Task task);
    void deleteTask(Long taskId);
    Task getTaskById(Long taskId);
    List<Task> getAllTasks();
    List<Task> getTasksByUserId(Long userId);
}