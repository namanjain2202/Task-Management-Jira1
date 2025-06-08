package model;

import java.util.*;

public class Story {
    private String id;
    private String title;
    private String description;
    private List<Task> tasks;

    public Story(String title, String description) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.description = description;
        this.tasks = new ArrayList<>();
    }

    public void addTask(Task task) {
        task.setParentId(id);
        tasks.add(task);
    }

    public String getId() {
        return id;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    @Override
    public String toString() {
        return "Story{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", tasks=" + tasks.size() +
                '}';
    }
}
