import kanban.tasks.TaskManager;
import kanban.tasks.TaskStatus;

public class Main {
    public static void main(String[] args) {
        //для тестов
        TaskManager taskManager = new TaskManager();

        taskManager.createTask("Casual Task", "do smth casual");
        taskManager.createEpic("Epic", "a lot to do");
        taskManager.createEpic("Epic2", "a lot to do2");
        taskManager.createSubtask("1", "do something1", 2);
        taskManager.createSubtask("2", "do something2", 2);
        taskManager.createSubtask("3", "do something3", 3);
        taskManager.updateStatus(TaskStatus.DONE, 4);
        taskManager.updateStatus(TaskStatus.IN_PROGRESS, 5);
        taskManager.updateStatus(TaskStatus.DONE, 6);
        taskManager.createSubtask("4", "do something4", 3);
        taskManager.createTask("Casual Task2", "do smth casual2");
        taskManager.updateStatus(TaskStatus.DONE, 7);
        taskManager.getAllKindOfTasks().forEach(System.out::println);
    }
}
