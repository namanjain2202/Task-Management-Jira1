package repository;

import model.Task;

import java.util.*;

public class TaskRepository {
    private final Map<String, Task> tasks = new HashMap<>();

    public void save(Task task) {
        tasks.put(task.getId(), task);
    }

    public Task findById(String id) {
        return tasks.get(id);
    }

    public void delete(String id) {
        tasks.remove(id);
    }

    public List<Task> findAllByUserId(String userId) {
        List<Task> result = new ArrayList<>();
        for (Task task : tasks.values()) {
            if (userId.equals(task.getAssignedUserId())) {
                result.add(task);
            }
        }
        return result;
    }

    public Collection<Task> findAll() {
        return tasks.values();
    }
}
