import model.Task;
import model.User;
import service.*;

import java.util.*;

public class TestCases {
    public static void main(String[] args) {
        UserService userService = new UserService();
        TaskService taskService = new TaskService();
        StoryService storyService = new StoryService();
        WorkloadService workloadService = new WorkloadService(taskService.getTaskRepo());

        // TEST CASE 1 - Register + Login
        User user = userService.register("Alice", "alice@example.com", "pass");
        User loggedInUser = userService.login("alice@example.com", "pass");
        assert loggedInUser != null && loggedInUser.getName().equals("Alice");
        System.out.println("TEST CASE 1 PASSED");

        // TEST CASE 2 - Create Task
        Task task1 = taskService.createTask("Task 1", "Desc 1", new Date(), user.getId());
        assert task1 != null && task1.getAssignedUserId().equals(user.getId());
        System.out.println("TEST CASE 2 PASSED");

        // TEST CASE 3 - Create Subtask
        Task subtask1 = taskService.createSubtask(task1.getId(), "Subtask 1", "Sub Desc", new Date(), user.getId());
        assert subtask1 != null && subtask1.getParentId().equals(task1.getId());
        System.out.println("TEST CASE 3 PASSED");

        // TEST CASE 4 - Move Subtask
        taskService.moveTask(subtask1.getId(), null);
        assert subtask1.getParentId() == null;
        System.out.println("TEST CASE 4 PASSED");

        // TEST CASE 5 - Create Story with tasks
        List<Task> tasksForStory = Arrays.asList(task1);
        var story = storyService.createStory("Story 1", "Story Desc", tasksForStory);
        assert story != null && story.getTasks().size() == 1;
        System.out.println("TEST CASE 5 PASSED");

        // TEST CASE 6 - Workload Stats
        Map<Task.Status, Integer> workload = workloadService.getUserWorkload(user.getId());
        assert workload.containsKey(Task.Status.PENDING);
        System.out.println("TEST CASE 6 PASSED");

        // TEST CASE 7 - Update Task
        taskService.updateTask(task1.getId(), "Updated Task", "Updated Desc", new Date(), Task.Status.COMPLETED);
        Task updatedTask = taskService.getTaskRepo().findById(task1.getId());
        assert updatedTask.getStatus() == Task.Status.COMPLETED;
        System.out.println("TEST CASE 7 PASSED");

        // TEST CASE 8 - Delete Task
        taskService.deleteTask(task1.getId());
        assert taskService.getTaskRepo().findById(task1.getId()) == null;
        System.out.println("TEST CASE 8 PASSED");

        // TEST CASE 9 - Invalid Login
        try {
            userService.login("wrong@example.com", "pass");
            assert false : "Expected exception for invalid login";
        } catch (RuntimeException ex) {
            System.out.println("TEST CASE 9 PASSED (caught exception: " + ex.getMessage() + ")");
        }

        System.out.println("ALL TEST CASES PASSED");
    }
}
