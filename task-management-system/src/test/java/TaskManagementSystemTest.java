import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import service.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class TaskManagementSystemTest {
    private UserService userService;
    private TaskService taskService;
    private StoryService storyService;
    private WorkloadService workloadService;
    private User testUser;

    @BeforeEach
    void setUp() {
        userService = new UserService();
        taskService = new TaskService();
        storyService = new StoryService();
        workloadService = new WorkloadService(taskService.getTaskRepo());
        testUser = userService.register("TestUser", "test@example.com", "password123");
    }

    @Test
    void testUserRegistrationAndLogin() {
        User user = userService.register("John", "john@example.com", "password");
        assertNotNull(user);
        assertEquals("John", user.getName());
        
        User loggedInUser = userService.login("john@example.com", "password");
        assertNotNull(loggedInUser);
        assertEquals("John", loggedInUser.getName());
    }

    @Test
    void testTaskCreationAndRetrieval() {
        Task task = taskService.createTask("Test Task", "Test Description", new Date(), testUser.getId());
        assertNotNull(task);
        assertEquals("Test Task", task.getTitle());
        assertEquals(TaskStatus.PENDING, task.getStatus());
    }

    @Test
    void testSubtaskManagement() {
        Task parentTask = taskService.createTask("Parent Task", "Parent Description", new Date(), testUser.getId());
        Task subtask = taskService.createSubtask(parentTask.getId(), "Subtask", "Subtask Description", new Date(), testUser.getId());
        
        assertNotNull(subtask);
        assertEquals("Subtask", subtask.getTitle());
        assertTrue(taskService.getTaskById(parentTask.getId()).getSubtasks().contains(subtask.getId()));
    }

    @Test
    void testStoryManagement() {
        Task task = taskService.createTask("Story Task", "Task Description", new Date(), testUser.getId());
        Story story = storyService.createStory("Test Story", "Story Description", List.of(task));
        
        assertNotNull(story);
        assertEquals("Test Story", story.getTitle());
        assertTrue(story.getTasks().contains(task.getId()));
    }

    @Test
    void testWorkloadCalculation() {
        taskService.createTask("Task 1", "Description 1", new Date(), testUser.getId());
        taskService.createTask("Task 2", "Description 2", new Date(), testUser.getId());
        
        Map<TaskStatus, Integer> workload = workloadService.getUserWorkload(testUser.getId());
        assertTrue(workload.containsKey(TaskStatus.PENDING));
        assertEquals(2, workload.get(TaskStatus.PENDING));
    }

    @Test
    void testTaskStatusUpdate() {
        Task task = taskService.createTask("Status Test", "Description", new Date(), testUser.getId());
        taskService.updateTaskStatus(task.getId(), TaskStatus.IN_PROGRESS);
        
        Task updatedTask = taskService.getTaskById(task.getId());
        assertEquals(TaskStatus.IN_PROGRESS, updatedTask.getStatus());
    }
}
