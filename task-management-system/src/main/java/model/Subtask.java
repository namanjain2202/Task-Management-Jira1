package model;

import java.util.*;

public class Subtask extends Task {
    private String parentTaskId;

    public Subtask(String title, String description, Date deadline, String parentTaskId) {
        super(title, description, deadline);
        this.parentTaskId = parentTaskId;
    }

    public String getParentTaskId() {
        return parentTaskId;
    }

    public void setParentTaskId(String parentTaskId) {
        this.parentTaskId = parentTaskId;
    }
}