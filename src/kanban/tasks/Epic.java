package kanban.tasks;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {

    private List<Integer> subtasksIds = new ArrayList<>();

    Epic(String title, String description) {
        super(title, description);
    }

    List<Integer> getSubtasksIds() {
        return subtasksIds;
    }

    void setSubtasksIds(List<Integer> subtasksIds) {
        this.subtasksIds = subtasksIds;
    }

    @Override
    public String toString() {
        return super.toString().replaceFirst("Task", "Epic").replace("}","") +
                ", subtasksIds=" + subtasksIds +
                '}';
    }
}
