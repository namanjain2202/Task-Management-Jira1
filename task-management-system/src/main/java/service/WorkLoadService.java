package service;

import model.Task;
import model.TaskStatus;
import model.TaskPriority;
import model.Story;
import repository.TaskRepository;
import repository.StoryRepository;
import exception.UserNotFoundException;
import exception.StoryNotFoundException;

import java.util.*;

public class WorkloadService {
    private final TaskRepository taskRepo;
    private final StoryRepository storyRepo;
    private final UserService userService;

    public WorkloadService(TaskRepository taskRepo, UserService userService) {
        this.taskRepo = taskRepo;
        this.storyRepo = new StoryRepository();
        this.userService = userService;
    }

    public Map<TaskStatus, Integer> getUserWorkload(String userId) {
        // Verify user exists
        userService.getUserById(userId);
        
        List<Task> tasks = taskRepo.findAllByUserId(userId);
        Map<TaskStatus, Integer> workload = new HashMap<>();
        
        // Initialize all statuses with 0
        for (TaskStatus status : TaskStatus.values()) {
            workload.put(status, 0);
        }
        
        // Count tasks by status
        for (Task task : tasks) {
            TaskStatus status = task.getStatus();
            workload.put(status, workload.get(status) + 1);
        }
        
        return workload;
    }

    public Map<String, Object> getDetailedWorkload(String userId) {
        List<Task> tasks = taskRepo.findAllByUserId(userId);
        Map<String, Object> detailedWorkload = new HashMap<>();
        
        // Count by status
        Map<TaskStatus, Integer> statusCount = new HashMap<>();
        for (TaskStatus status : TaskStatus.values()) {
            statusCount.put(status, 0);
        }
        
        // Count by priority
        Map<TaskPriority, Integer> priorityCount = new HashMap<>();
        for (TaskPriority priority : TaskPriority.values()) {
            priorityCount.put(priority, 0);
        }
        
        // Count total tasks and subtasks
        int totalTasks = 0;
        int totalSubtasks = 0;
        
        for (Task task : tasks) {
            // Update status count
            TaskStatus status = task.getStatus();
            statusCount.put(status, statusCount.get(status) + 1);
            
            // Update priority count
            TaskPriority priority = task.getPriority();
            if (priority != null) {
                priorityCount.put(priority, priorityCount.get(priority) + 1);
            }
            
            // Count tasks and subtasks
            if (task.getParentTaskId() == null) {
                totalTasks++;
            } else {
                totalSubtasks++;
            }
        }
        
        detailedWorkload.put("statusCount", statusCount);
        detailedWorkload.put("priorityCount", priorityCount);
        detailedWorkload.put("totalTasks", totalTasks);
        detailedWorkload.put("totalSubtasks", totalSubtasks);
        
        return detailedWorkload;
    }

    public Map<String, Object> getUserWorkloadDetails(String userId) {
        // Verify user exists
        userService.getUserById(userId);
        
        List<Task> allTasks = taskRepo.findAllByUserId(userId);
        Map<String, Object> workloadDetails = new HashMap<>();
        
        // Separate tasks, subtasks, and stories
        List<Task> rootTasks = new ArrayList<>();
        List<Task> subtasks = new ArrayList<>();
        List<Story> stories = new ArrayList<>();
        
        // Group tasks by their parent task
        Map<String, List<Task>> tasksByParent = new HashMap<>();
        
        for (Task task : allTasks) {
            String parentTaskId = task.getParentTaskId();
            if (parentTaskId == null) {
                rootTasks.add(task);
            } else {
                subtasks.add(task);
                tasksByParent.computeIfAbsent(parentTaskId, k -> new ArrayList<>()).add(task);
            }
        }
        
        // Get all stories and their tasks
        for (Story story : storyRepo.findAll()) {
            List<String> storyTaskIds = story.getTasks();
            if (!storyTaskIds.isEmpty()) {
                // Check if any of the story's tasks belong to this user
                boolean hasUserTasks = storyTaskIds.stream()
                    .anyMatch(taskId -> allTasks.stream()
                        .anyMatch(task -> task.getId().equals(taskId)));
                
                if (hasUserTasks) {
                    stories.add(story);
                }
            }
        }
        
        // Add all components to the result
        workloadDetails.put("rootTasks", rootTasks);
        workloadDetails.put("subtasks", subtasks);
        workloadDetails.put("stories", stories);
        workloadDetails.put("tasksByParent", tasksByParent);
        
        // Add summary counts
        Map<String, Integer> summary = new HashMap<>();
        summary.put("totalRootTasks", rootTasks.size());
        summary.put("totalSubtasks", subtasks.size());
        summary.put("totalStories", stories.size());
        workloadDetails.put("summary", summary);
        
        return workloadDetails;
    }
}
