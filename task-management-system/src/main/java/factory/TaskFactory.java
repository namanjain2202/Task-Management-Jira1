package factory;

import model.Story;
import model.Task;

import java.util.Date;

public class TaskFactory {
    public static Task createTask(String title, String description, Date deadline) {
        return new Task(title, description, deadline);
    }

    public static Story createStory(String title, String description) {
        return new Story(title, description);
    }
}
