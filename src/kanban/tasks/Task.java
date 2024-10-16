package kanban.tasks;

import java.util.Objects;

public class Task {

    private String title;
    private String description;
    private int id;
    private TaskStatus status;

    Task(String title, String description) {
        this.title = title;
        this.description = description;
        this.id = -1;
        this.status = TaskStatus.NEW;
    }

    String getTitle() {
        return title;
    }

    void setTitle(String title) {
        this.title = title;
    }

    String getDescription() {
        return description;
    }

    void setDescription(String description) {
        this.description = description;
    }

    int getId() {
        return id;
    }

    void setId(int id) {
        this.id = id;
    }

    TaskStatus getStatus() {
        return status;
    }

    void setStatus(TaskStatus status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return id == task.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Task{" +
                "title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", id=" + id +
                ", status=" + status +
                '}';
    }
}
