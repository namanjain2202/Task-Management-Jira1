import model.Task;
import model.User;
import service.*;
import model.Story;
import model.TaskStatus;
import repository.StoryRepository;

import java.util.*;

public class Main {
    public static void main(String[] args) {
        UserService userService = new UserService();
        TaskService taskService = new TaskService();
        StoryService storyService = new StoryService();
        WorkloadService workloadService = new WorkloadService(taskService.getTaskRepo());

        // Register and login
        User user = userService.register("John", "john@example.com", "1234");
        User loggedInUser = userService.login("john@example.com", "1234");
        System.out.println("Logged in user: " + loggedInUser.getName());

        // Create task
        Task task1 = taskService.createTask("Task 1", "Desc 1", new Date(), user.getId());
        System.out.println(task1);

        // Create subtask
        Task subtask1 = taskService.createSubtask(task1.getId(), "Subtask 1", "SubDesc 1", new Date(), user.getId());
        System.out.println(subtask1);

        // Move subtask
        taskService.moveTask(subtask1.getId(), null);

        // Create Story
        List<Task> tasksForStory = Arrays.asList(task1);
        storyService.createStory("Story 1", "Story Desc", tasksForStory);

        // Get workload
        Map<TaskStatus, Integer> workload = workloadService.getUserWorkload(user.getId());
        System.out.println("Workload: " + workload);

        // Create a story repository
        StoryRepository repository = new StoryRepository();

        // Create some stories
        Story story1 = new Story("Implement Login", "Create login functionality");
        Story story2 = new Story("Add Search", "Implement search feature");

        // Save stories to repository
        repository.save(story1);
        repository.save(story2);

        // Find a story by ID
        Story foundStory = repository.findById(story1.getId());
        System.out.println("Found story: " + foundStory);

        // Get all stories
        System.out.println("\nAll stories:");
        repository.findAll().forEach(story -> System.out.println(story));
    }
}
