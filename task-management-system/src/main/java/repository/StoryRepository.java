package repository;

import model.Story;

import java.util.*;

public class StoryRepository {
    private final Map<String, Story> stories = new HashMap<>();

    public void save(Story story) {
        stories.put(story.getId(), story);
    }

    public Story findById(String id) {
        return stories.get(id);
    }

    public Collection<Story> findAll() {
        return stories.values();
    }
}
