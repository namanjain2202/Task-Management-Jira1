public class Subtask extends Task {
    private String parentTaskId;

    public Subtask(String id, String title, String description, String deadline, String assignedUser, String parentTaskId) {
        super(id, title, description, deadline, assignedUser);
        this.parentTaskId = parentTaskId;
    }

    public String getParentTaskId() {
        return parentTaskId;
    }

    public void setParentTaskId(String parentTaskId) {
        this.parentTaskId = parentTaskId;
    }
}