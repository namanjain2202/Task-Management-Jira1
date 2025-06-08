package service;

import model.Task;
import model.TaskStatus;
import repository.TaskRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WorkloadService {
    private final TaskRepository taskRepo;

    public WorkloadService(TaskRepository taskRepo) {
        this.taskRepo = taskRepo;
    }

    public Map<TaskStatus, Integer> getUserWorkload(String userId) {
        List<Task> tasks = taskRepo.findAllByUserId(userId);
        Map<TaskStatus, Integer> workload = new HashMap<>();
        for (Task task : tasks) {
            workload.put(task.getStatus(), workload.getOrDefault(task.getStatus(), 0) + 1);
        }
        return workload;
    }
}
