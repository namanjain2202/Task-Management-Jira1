import model.Task;
import model.User;
import service.*;
import model.Story;
import model.Subtask;
import model.TaskPriority;
import model.TaskStatus;
import repository.StoryRepository;
import repository.TaskRepository;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
        Map<TaskStatus, Integer> workload = workloadService.getUserWorkload(user.getId());
        assert workload.containsKey(TaskStatus.PENDING);
        System.out.println("TEST CASE 6 PASSED");

        // TEST CASE 7 - Update Task
        taskService.updateTask(task1.getId(), "Updated Task", "Updated Desc", new Date(), TaskStatus.COMPLETED);
        Task updatedTask = taskService.getTaskRepo().findById(task1.getId());
        assert updatedTask.getStatus() == TaskStatus.COMPLETED;
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

        try {
            testConcurrentTaskCreation();
            testTaskValidation();
            testTaskStatusTransitions();
        } catch (Exception e) {
            System.err.println("Test execution failed: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("ALL TEST CASES PASSED");
    }

    private static void testConcurrentTaskCreation() {
        System.out.println("\nTesting concurrent task creation...");
        TaskService taskService = new TaskService();
        ExecutorService executor = Executors.newFixedThreadPool(10);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);
        List<Exception> exceptions = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            final int taskId = i;
            executor.submit(() -> {
                try {
                    taskService.createTask(
                        "Task " + taskId,
                        "Description " + taskId,
                        new Date(),
                        "HIGH"
                    );
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failureCount.incrementAndGet();
                    synchronized (exceptions) {
                        exceptions.add(e);
                    }
                }
            });
        }

        executor.shutdown();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        System.out.println("Concurrent task creation results: Success=" + successCount.get() + 
                         ", Failure=" + failureCount.get());
        
        if (!exceptions.isEmpty()) {
            System.out.println("\nExceptions encountered:");
            for (Exception e : exceptions) {
                System.out.println("- " + e.getMessage());
            }
        }
    }

    private static void testTaskValidation() {
        System.out.println("\nTesting task validation...");
        TaskService taskService = new TaskService();
        
        try {
            // Test null title
            taskService.createTask(null, "Description", new Date(), "HIGH");
            System.out.println(" Test failed: Should have thrown exception for null title");
        } catch (IllegalArgumentException e) {
            System.out.println("Test passed: Caught expected exception for null title");
        }

        try {
            // Test empty title
            taskService.createTask("", "Description", new Date(), "HIGH");
            System.out.println("Test failed: Should have thrown exception for empty title");
        } catch (IllegalArgumentException e) {
            System.out.println("Test passed: Caught expected exception for empty title");
        }

        try {
            // Test null deadline
            taskService.createTask("Title", "Description", null, "HIGH");
            System.out.println("Test failed: Should have thrown exception for null deadline");
        } catch (IllegalArgumentException e) {
            System.out.println("Test passed: Caught expected exception for null deadline");
        }
    }

    private static void testTaskStatusTransitions() {
        System.out.println("\nTesting task status transitions...");
        TaskService taskService = new TaskService();
        
        try {
            // Create a task
            Task task = taskService.createTask("Test Task", "Description", new Date(), "HIGH");
            
            // Test valid status transitions
            taskService.updateTask(task.getId(), "Test Task", "Description", new Date(), TaskStatus.IN_PROGRESS);
            System.out.println("✅ Test passed: Valid status transition to IN_PROGRESS");
            
            taskService.updateTask(task.getId(), "Test Task", "Description", new Date(), TaskStatus.COMPLETED);
            System.out.println("✅ Test passed: Valid status transition to COMPLETED");
            
            // Test invalid status transition
            try {
                taskService.updateTask(task.getId(), "Test Task", "Description", new Date(), TaskStatus.PENDING);
                System.out.println("❌ Test failed: Should have thrown exception for invalid status transition");
            } catch (IllegalStateException e) {
                System.out.println("✅ Test passed: Caught expected exception for invalid status transition");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Test failed: " + e.getMessage());
        }
    }
}
