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
import exception.UserNotFoundException;
import exception.StoryNotFoundException;

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
        WorkloadService workloadService = new WorkloadService(taskService.getTaskRepo(), userService);
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
            subtask1 = taskService.createSubtask(task1.getId(), "Subtask 1", "SubDesc 1", new Date(), user.getId());
            assert subtask1 != null && subtask1.getParentTaskId().equals(task1.getId());
            System.out.println("TEST CASE 3 PASSED");
            passed++;
        } catch (Throwable t) {
            System.out.println("TEST CASE 3 FAILED: " + t.getMessage());
            failed++;
        }

        // TEST CASE 4 - Update Task
        try {
            taskService.updateTask(task1.getId(), "Updated Task 1", "Updated Desc 1", new Date(), TaskStatus.IN_PROGRESS);
            Task updatedTask = taskService.getTaskById(task1.getId());
            assert updatedTask.getTitle().equals("Updated Task 1");
            assert updatedTask.getStatus() == TaskStatus.IN_PROGRESS;
            System.out.println("TEST CASE 4 PASSED");
            passed++;
        } catch (Throwable t) {
            System.out.println("TEST CASE 4 FAILED: " + t.getMessage());
            failed++;
        }

        // TEST CASE 5 - Delete Task
        try {
            taskService.deleteTask(subtask1.getId());
            try {
                taskService.getTaskById(subtask1.getId());
                System.out.println("TEST CASE 5 FAILED: Task still exists after deletion");
                failed++;
            } catch (TaskNotFoundException e) {
                System.out.println("TEST CASE 5 PASSED");
                passed++;
            }
        } catch (Throwable t) {
            System.out.println("TEST CASE 5 FAILED: " + t.getMessage());
            failed++;
        }

        // TEST CASE 6 - Invalid Login
        try {
            userService.login("alice@example.com", "wrongpass");
            System.out.println("TEST CASE 6 FAILED: Should have thrown exception for invalid password");
            failed++;
        } catch (IllegalArgumentException e) {
            System.out.println("TEST CASE 6 PASSED");
            passed++;
        }

        // TEST CASE 7 - Create Story
        try {
            Task task2 = taskService.createTask("Task 2", "Desc 2", new Date(), user.getId());
            List<Task> tasksForStory = Arrays.asList(task2);
            Story story = storyService.createStory("Story 1", "Story Desc", tasksForStory);
            assert story != null && story.getTasks().contains(task2.getId());
            System.out.println("TEST CASE 7 PASSED");
            passed++;
        } catch (Throwable t) {
            System.out.println("TEST CASE 7 FAILED: " + t.getMessage());
            failed++;
        }

        // TEST CASE 8 - Get Workload
        try {
            Map<TaskStatus, Integer> workload = workloadService.getUserWorkload(user.getId());
            assert workload.containsKey(TaskStatus.IN_PROGRESS);
            assert workload.get(TaskStatus.IN_PROGRESS) > 0;
            System.out.println("TEST CASE 8 PASSED");
            passed++;
        } catch (Throwable t) {
            System.out.println("TEST CASE 8 FAILED: " + t.getMessage());
            failed++;
        }

        // TEST CASE 9 - Invalid User
        try {
            workloadService.getUserWorkload("nonexistent");
            System.out.println("TEST CASE 9 FAILED: Should have thrown exception for non-existent user");
            failed++;
        } catch (UserNotFoundException e) {
            System.out.println("TEST CASE 9 PASSED");
            passed++;
        }

        // Print summary
        System.out.println("\n==============================");
        System.out.println("SUMMARY: " + passed + " PASSED, " + failed + " FAILED");
        System.out.println("==============================");
        if (failed == 0) {
            System.out.println("ALL TEST CASES PASSED");
        }

        // Test concurrent task creation
        testConcurrentTaskCreation(taskService, user.getId());

        // Test task validation
        testTaskValidation(taskService, user.getId());

        // Test task status transitions
        testTaskStatusTransitions(taskService, user.getId());

        // Test task movement
        testTaskMovement(taskService, user.getId());

        // Test detailed workload
        testDetailedWorkload();
    }

    private static void testConcurrentTaskCreation(TaskService taskService, String userId) {
        System.out.println("\nTesting concurrent task creation...");
        int numThreads = 10;
        int tasksPerThread = 10;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                for (int j = 0; j < tasksPerThread; j++) {
                    try {
                        taskService.createTask("Concurrent Task " + j, "Description", new Date(), userId);
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        failureCount.incrementAndGet();
                    }
                }
            });
        }

        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println("Concurrent task creation results: Success=" + successCount.get() + ", Failure=" + failureCount.get());
    }

    private static void testTaskValidation(TaskService taskService, String userId) {
        System.out.println("\nTesting task validation...");
        
        // Test null title
        try {
            taskService.createTask(null, "Description", new Date(), userId);
            System.out.println("Test failed: Should have thrown exception for null title");
        } catch (IllegalArgumentException e) {
            System.out.println("Test passed: Caught exception for null title");
        }

        // Test empty title
        try {
            taskService.createTask("", "Description", new Date(), userId);
            System.out.println("Test failed: Should have thrown exception for empty title");
        } catch (IllegalArgumentException e) {
            System.out.println("Test passed: Caught exception for empty title");
        }

        // Test null deadline
        try {
            taskService.createTask("Title", "Description", null, userId);
            System.out.println("Test failed: Should have thrown exception for null deadline");
        } catch (IllegalArgumentException e) {
            System.out.println("Test passed: Caught exception for null deadline");
        }
    }

    private static void testTaskStatusTransitions(TaskService taskService, String userId) {
        System.out.println("\nTesting task status transitions...");
        
        Task task = taskService.createTask("Status Test", "Description", new Date(), userId);
        
        // Test valid transition to IN_PROGRESS
        try {
            taskService.updateTask(task.getId(), task.getTitle(), task.getDescription(), task.getDeadline(), TaskStatus.IN_PROGRESS);
            Task updatedTask = taskService.getTaskById(task.getId());
            assert updatedTask.getStatus() == TaskStatus.IN_PROGRESS;
            System.out.println("✅ Test passed: Valid status transition to IN_PROGRESS");
        } catch (Exception e) {
            System.out.println("❌ Test failed: " + e.getMessage());
        }
        
        // Test valid transition to COMPLETED
        try {
            taskService.updateTask(task.getId(), task.getTitle(), task.getDescription(), task.getDeadline(), TaskStatus.COMPLETED);
            Task updatedTask = taskService.getTaskById(task.getId());
            assert updatedTask.getStatus() == TaskStatus.COMPLETED;
            System.out.println("✅ Test passed: Valid status transition to COMPLETED");
        } catch (Exception e) {
            System.out.println("❌ Test failed: " + e.getMessage());
        }
        
        // Test invalid transition back to PENDING
        try {
            taskService.updateTask(task.getId(), task.getTitle(), task.getDescription(), task.getDeadline(), TaskStatus.PENDING);
            System.out.println("❌ Test failed: Should have thrown exception for invalid status transition");
        } catch (Exception e) {
            System.out.println("✅ Test passed: Caught exception for invalid status transition");
        }
    }

    private static void testTaskMovement(TaskService taskService, String userId) {
        System.out.println("\nTesting task movement...");
        
        // Create parent tasks
        Task parent1 = taskService.createTask("Parent 1", "Description", new Date(), userId);
        Task parent2 = taskService.createTask("Parent 2", "Description", new Date(), userId);
        Task child = taskService.createTask("Child", "Description", new Date(), userId);
        
        // Test moving to parent1
        try {
            taskService.moveTask(child.getId(), parent1.getId());
            Task movedTask = taskService.getTaskById(child.getId());
            assert movedTask.getParentTaskId().equals(parent1.getId());
            System.out.println("✅ Test passed: Move task to parent1");
        } catch (Exception e) {
            System.out.println("❌ Test failed: " + e.getMessage());
        }
        
        // Test moving to parent2
        try {
            taskService.moveTask(child.getId(), parent2.getId());
            Task movedTask = taskService.getTaskById(child.getId());
            assert movedTask.getParentTaskId().equals(parent2.getId());
            System.out.println("✅ Test passed: Move task to parent2");
        } catch (Exception e) {
            System.out.println("❌ Test failed: " + e.getMessage());
        }
        
        // Test moving to root level
        try {
            taskService.moveTask(child.getId(), null);
            Task movedTask = taskService.getTaskById(child.getId());
            assert movedTask.getParentTaskId() == null;
            System.out.println("✅ Test passed: Move task to root level");
        } catch (Exception e) {
            System.out.println("❌ Test failed: " + e.getMessage());
        }
        
        // Test moving non-existent task
        try {
            taskService.moveTask("nonexistent", parent1.getId());
            System.out.println("❌ Test failed: Should have thrown exception for non-existent task");
        } catch (TaskNotFoundException e) {
            System.out.println("✅ Test passed: Caught expected exception for non-existent task");
        }
        
        // Test moving to non-existent parent
        try {
            taskService.moveTask(child.getId(), "nonexistent");
            System.out.println("❌ Test failed: Should have thrown exception for non-existent parent");
        } catch (TaskNotFoundException e) {
            System.out.println("✅ Test passed: Caught expected exception for non-existent parent");
        }
        
        // Test circular dependency
        try {
            taskService.moveTask(parent1.getId(), child.getId());
            System.out.println("✅ Test passed: Caught expected exception for circular dependency");
        } catch (TaskManagementException e) {
            System.out.println("❌ Test failed: Should have thrown exception for circular dependency");
            
        }
    }

    private static void testDetailedWorkload() {
        System.out.println("\nTesting detailed workload...");
        TaskService taskService = new TaskService();
        UserService userService = new UserService();
        WorkloadService workloadService = new WorkloadService(taskService.getTaskRepo(), userService);
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
                Task parent = taskService.getTaskById(task.getParentTaskId());
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
