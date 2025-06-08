package service;

import factory.TaskFactory;
import model.Story;
import model.Task;
import repository.StoryRepository;

import java.util.List;

public class StoryService {
    private final StoryRepository storyRepo = new StoryRepository();

    public Story createStory(String title, String description, List<Task> tasks) {
        Story story = TaskFactory.createStory(title, description);
        for (Task task : tasks) {
            story.addTask(task);
        }
        storyRepo.save(story);
        return story;
    }

    public StoryRepository getStoryRepo() {
        return storyRepo;
    }
}
