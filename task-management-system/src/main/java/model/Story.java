package model;

import java.util.*;

public class Story {
    private String id;
    private String title;
    private String description;
    private List<String> taskIds;

    public Story(String title, String description) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.description = description;
        this.taskIds = new ArrayList<>();
    }

    public void addTask(Task task) {
        taskIds.add(task.getId());
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getTasks() {
        return new ArrayList<>(taskIds);
    }

    @Override
    public String toString() {
        return "Story{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", tasks=" + taskIds.size() +
                '}';
    }
}
