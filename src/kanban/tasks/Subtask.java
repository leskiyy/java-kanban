package kanban.tasks;

public class Subtask extends Task {

    private final int epicId;

    Subtask(String title, String description, int epicId) {
        super(title, description);
        this.epicId = epicId;
    }

    int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return super.toString().replaceFirst("Task", "Subtask").replace("}","") +
                ", epicId=" + epicId +
                '}';
    }
}
