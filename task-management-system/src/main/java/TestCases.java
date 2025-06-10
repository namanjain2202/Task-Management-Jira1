import model.Task;
import model.User;
import service.*;
import model.Story;
import model.Subtask;
import model.TaskPriority;
import model.TaskStatus;
import repository.StoryRepository;
import repository.TaskRepository;
import exception.TaskNotFoundException;
import exception.TaskManagementException;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TestCases {
    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        UserService userService = new UserService();
        TaskService taskService = new TaskService();
        StoryService storyService = new StoryService();
        WorkloadService workloadService = new WorkloadService(taskService.getTaskRepo());
        User user = null;

        // TEST CASE 1 - Register + Login
        try {
            user = userService.register("Alice", "alice@example.com", "pass");
            User loggedInUser = userService.login("alice@example.com", "pass");
            assert loggedInUser != null && loggedInUser.getName().equals("Alice");
            System.out.println("TEST CASE 1 PASSED");
            passed++;
        } catch (Throwable t) {
            System.out.println("TEST CASE 1 FAILED: " + t.getMessage());
            failed++;
        }

        // TEST CASE 2 - Create Task
        Task task1 = null;
        try {
            task1 = taskService.createTask("Task 1", "Desc 1", new Date(), user.getId());
            assert task1 != null && task1.getAssignedUserId().equals(user.getId());
            System.out.println("TEST CASE 2 PASSED");
            passed++;
        } catch (Throwable t) {
            System.out.println("TEST CASE 2 FAILED: " + t.getMessage());
            failed++;
        }

        // TEST CASE 3 - Create Subtask
        Task subtask1 = null;
        try {
            subtask1 = taskService.createSubtask(task1.getId(), "Subtask 1", "Sub Desc", new Date(), user.getId());
            assert subtask1 != null && subtask1.getParentId().equals(task1.getId());
            System.out.println("TEST CASE 3 PASSED");
            passed++;
        } catch (Throwable t) {
            System.out.println("TEST CASE 3 FAILED: " + t.getMessage());
            failed++;
        }

        // TEST CASE 4 - Move Subtask
        try {
            taskService.moveTask(subtask1.getId(), null);
            assert subtask1.getParentId() == null;
            System.out.println("TEST CASE 4 PASSED");
            passed++;
        } catch (Throwable t) {
            System.out.println("TEST CASE 4 FAILED: " + t.getMessage());
            failed++;
        }

        // TEST CASE 5 - Create Story with tasks
        try {
            List<Task> tasksForStory = Arrays.asList(task1);
            var story = storyService.createStory("Story 1", "Story Desc", tasksForStory);
            assert story != null && story.getTasks().size() == 1;
            System.out.println("TEST CASE 5 PASSED");
            passed++;
        } catch (Throwable t) {
            System.out.println("TEST CASE 5 FAILED: " + t.getMessage());
            failed++;
        }

        // TEST CASE 6 - Basic Workload Stats
        try {
            Map<TaskStatus, Integer> workload = workloadService.getUserWorkload(user.getId());
            assert workload.containsKey(TaskStatus.PENDING);
            System.out.println("TEST CASE 6 PASSED");
            passed++;
        } catch (Throwable t) {
            System.out.println("TEST CASE 6 FAILED: " + t.getMessage());
            failed++;
        }

        // TEST CASE 7 - Update Task
        try {
            taskService.updateTask(task1.getId(), "Updated Task", "Updated Desc", new Date(), TaskStatus.COMPLETED);
            Task updatedTask = taskService.getTaskRepo().findById(task1.getId());
            assert updatedTask.getStatus() == TaskStatus.COMPLETED;
            System.out.println("TEST CASE 7 PASSED");
            passed++;
        } catch (Throwable t) {
            System.out.println("TEST CASE 7 FAILED: " + t.getMessage());
            failed++;
        }

        // TEST CASE 8 - Delete Task
        try {
            taskService.deleteTask(task1.getId());
            assert taskService.getTaskRepo().findById(task1.getId()) == null;
            System.out.println("TEST CASE 8 PASSED");
            passed++;
        } catch (Throwable t) {
            System.out.println("TEST CASE 8 FAILED: " + t.getMessage());
            failed++;
        }

        // TEST CASE 9 - Invalid Login
        try {
            userService.login("wrong@example.com", "pass");
            assert false : "Expected exception for invalid login";
            System.out.println("TEST CASE 9 FAILED: No exception thrown");
            failed++;
        } catch (RuntimeException ex) {
            System.out.println("TEST CASE 9 PASSED (caught exception: " + ex.getMessage() + ")");
            passed++;
        } catch (Throwable t) {
            System.out.println("TEST CASE 9 FAILED: " + t.getMessage());
            failed++;
        }

        // Run advanced test cases
        try {
            testConcurrentTaskCreation();
            testTaskValidation();
            testTaskStatusTransitions();
            testTaskMovement();
            testDetailedWorkload();
        } catch (Exception e) {
            System.err.println("Test execution failed: " + e.getMessage());
            e.printStackTrace();
        }

        System.out.println("\n==============================");
        System.out.println("SUMMARY: " + passed + " PASSED, " + failed + " FAILED");
        System.out.println("==============================");
        if (failed == 0) {
            System.out.println("ALL TEST CASES PASSED");
        } else {
            System.out.println("SOME TEST CASES FAILED");
        }
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

    private static void testTaskMovement() {
        System.out.println("\nTesting task movement...");
        TaskService taskService = new TaskService();
        UserService userService = new UserService();
        
        try {
            // Create a user
            User user = userService.register("TestUser", "test@example.com", "pass");
            
            // Create parent tasks
            Task parent1 = taskService.createTask("Parent 1", "Description", new Date(), user.getId());
            Task parent2 = taskService.createTask("Parent 2", "Description", new Date(), user.getId());
            
            // Create a task to move
            Task taskToMove = taskService.createTask("Task to Move", "Description", new Date(), user.getId());
            
            // Test 1: Move task to parent1
            taskService.moveTask(taskToMove.getId(), parent1.getId());
            assert taskToMove.getParentId().equals(parent1.getId());
            System.out.println("✅ Test passed: Move task to parent1");
            
            // Test 2: Move task to parent2
            taskService.moveTask(taskToMove.getId(), parent2.getId());
            assert taskToMove.getParentId().equals(parent2.getId());
            System.out.println("✅ Test passed: Move task to parent2");
            
            // Test 3: Move task to null (make it a root task)
            taskService.moveTask(taskToMove.getId(), null);
            assert taskToMove.getParentId() == null;
            System.out.println("✅ Test passed: Move task to root level");
            
            // Test 4: Try to move non-existent task
            try {
                taskService.moveTask("non-existent-id", parent1.getId());
                System.out.println("❌ Test failed: Should have thrown exception for non-existent task");
            } catch (TaskNotFoundException e) {
                System.out.println("✅ Test passed: Caught expected exception for non-existent task");
            }
            
            // Test 5: Try to move to non-existent parent
            try {
                taskService.moveTask(taskToMove.getId(), "non-existent-parent");
                System.out.println("❌ Test failed: Should have thrown exception for non-existent parent");
            } catch (TaskNotFoundException e) {
                System.out.println("✅ Test passed: Caught expected exception for non-existent parent");
            }
            
            // Test 6: Try to create circular dependency
            Task child = taskService.createSubtask(parent1.getId(), "Child", "Description", new Date(), user.getId());
            try {
                taskService.moveTask(parent1.getId(), child.getId());
                System.out.println("❌ Test failed: Should have thrown exception for circular dependency");
            } catch (TaskManagementException e) {
                System.out.println("✅ Test passed: Caught expected exception for circular dependency");
            }
            
        } catch (Exception e) {
            System.err.println("❌ Test failed: " + e.getMessage());
        }
    }

    private static void testDetailedWorkload() {
        System.out.println("\nTesting detailed workload...");
        TaskService taskService = new TaskService();
        WorkloadService workloadService = new WorkloadService(taskService.getTaskRepo());
        UserService userService = new UserService();
        StoryService storyService = new StoryService();
        
        try {
            // Create a user
            User user = userService.register("WorkloadUser", "workload@example.com", "pass");
            
            // Create tasks with different statuses and priorities
            Task task1 = taskService.createTask("High Priority Task", "Description", new Date(), user.getId());
            task1.setPriority(TaskPriority.HIGH);
            task1.update(task1.getTitle(), task1.getDescription(), task1.getDeadline(), TaskStatus.IN_PROGRESS);
            
            Task task2 = taskService.createTask("Medium Priority Task", "Description", new Date(), user.getId());
            task2.setPriority(TaskPriority.MEDIUM);
            task2.update(task2.getTitle(), task2.getDescription(), task2.getDeadline(), TaskStatus.COMPLETED);
            
            Task task3 = taskService.createTask("Low Priority Task", "Description", new Date(), user.getId());
            task3.setPriority(TaskPriority.LOW);
            task3.update(task3.getTitle(), task3.getDescription(), task3.getDeadline(), TaskStatus.PENDING);
            
            // Create a subtask
            Task subtask = taskService.createSubtask(task1.getId(), "Subtask", "Description", new Date(), user.getId());
            subtask.setPriority(TaskPriority.CRITICAL);
            
            // Create a story with tasks
            List<Task> tasksForStory = Arrays.asList(task2, task3);
            Story story = storyService.createStory("Test Story", "Story Description", tasksForStory);
            
            // Get detailed workload
            Map<String, Object> workloadDetails = workloadService.getUserWorkloadDetails(user.getId());
            
            // Print user's workload details
            System.out.println("\n=== User Workload Details ===");
            System.out.println("User: " + user.getName() + " (" + user.getEmail() + ")");
            
            // Print root tasks
            @SuppressWarnings("unchecked")
            List<Task> rootTasks = (List<Task>) workloadDetails.get("rootTasks");
            System.out.println("\nRoot Tasks:");
            for (Task task : rootTasks) {
                System.out.println("- " + task.getTitle() + 
                    " (Priority: " + task.getPriority() + 
                    ", Status: " + task.getStatus() + ")");
            }
            
            // Print subtasks
            @SuppressWarnings("unchecked")
            List<Task> subtasks = (List<Task>) workloadDetails.get("subtasks");
            System.out.println("\nSubtasks:");
            for (Task task : subtasks) {
                Task parent = taskService.getTaskById(task.getParentId());
                System.out.println("- " + task.getTitle() + 
                    " (Parent: " + (parent != null ? parent.getTitle() : "Unknown") + 
                    ", Priority: " + task.getPriority() + 
                    ", Status: " + task.getStatus() + ")");
            }
            
            // Print stories
            @SuppressWarnings("unchecked")
            List<Story> stories = (List<Story>) workloadDetails.get("stories");
            System.out.println("\nStories:");
            for (Story s : stories) {
                System.out.println("\nStory: " + s.getTitle());
                System.out.println("Description: " + s.getDescription());
                System.out.println("Tasks in this story:");
                for (String taskId : s.getTasks()) {
                    Task task = taskService.getTaskById(taskId);
                    if (task != null) {
                        System.out.println("  - " + task.getTitle() + 
                            " (Priority: " + task.getPriority() + 
                            ", Status: " + task.getStatus() + ")");
                    }
                }
            }
            
            // Print tasks by parent
            @SuppressWarnings("unchecked")
            Map<String, List<Task>> tasksByParent = (Map<String, List<Task>>) workloadDetails.get("tasksByParent");
            System.out.println("\nTasks by Parent:");
            for (Map.Entry<String, List<Task>> entry : tasksByParent.entrySet()) {
                Task parent = taskService.getTaskById(entry.getKey());
                System.out.println("\nParent: " + (parent != null ? parent.getTitle() : "Unknown"));
                for (Task task : entry.getValue()) {
                    System.out.println("  - " + task.getTitle() + 
                        " (Priority: " + task.getPriority() + 
                        ", Status: " + task.getStatus() + ")");
                }
            }
            
            // Verify the data
            assert rootTasks.size() == 3;
            assert subtasks.size() == 1;
            assert stories.size() == 1;
            assert tasksByParent.containsKey(task1.getId());
            assert tasksByParent.get(task1.getId()).size() == 1;
            
            System.out.println("\n✅ All workload data verified successfully!");
            
        } catch (Exception e) {
            System.err.println("❌ Test failed: " + e.getMessage());
        }
    }
}
