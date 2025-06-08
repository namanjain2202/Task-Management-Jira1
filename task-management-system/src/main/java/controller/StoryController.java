package controller;

import model.Story;
import model.Task;
import service.StoryService;

import java.util.List;

public class StoryController {

    private final StoryService storyService;

    public StoryController(StoryService storyService) {
        this.storyService = storyService;
    }

    public Story createStory(String title, String description, List<Task> tasks) {
        try {
            return storyService.createStory(title, description, tasks);
        } catch (Exception e) {
            System.out.println("Error creating story: " + e.getMessage());
            return null;
        }
    }
}
