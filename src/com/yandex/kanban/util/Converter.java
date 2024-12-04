package com.yandex.kanban.util;

import com.yandex.kanban.model.Epic;
import com.yandex.kanban.model.Subtask;
import com.yandex.kanban.model.Task;
import com.yandex.kanban.model.TaskStatus;

import java.util.ArrayList;
import java.util.List;

public class Converter {
    private static final String EPIC_FORMAT = "%d;%s;%s;%s;%s;%s"; // "id;type;title;description;status;[sub,ids]"
    private static final String TASK_FORMAT = "%d;%s;%s;%s;%s"; // "id;type;title;description;status"
    private static final String SUBTASK_FORMAT = "%d;%s;%s;%s;%s;%d"; // "id;type;title;description;status;epicID"

    public static String taskToString(Task task) {
        if (task instanceof Epic) {
            return epicToString((Epic) task);
        } else if (task instanceof Subtask) {
            return subtaskToString((Subtask) task);
        } else {
            return String.format(TASK_FORMAT, task.getId(), TaskTypes.TASK, task.getTitle(), task.getDescription(),
                    task.getStatus());
        }
    }

    public static Task stringToTask(String value) {
        String[] parts = value.split(";");

        int id = Integer.parseInt(parts[0]);
        String type = parts[1];
        String title = parts[2];
        String description = parts[3];
        TaskStatus status = TaskStatus.valueOf(parts[4]);

        switch (type) {
            case "TASK" -> {
                Task task = new Task(title, description);
                task.setId(id);
                task.setStatus(status);
                return task;
            }
            case "SUBTASK" -> {
                Subtask subtask = new Subtask(title, description);
                subtask.setId(id);
                subtask.setStatus(status);
                subtask.setEpicId(Integer.parseInt(parts[5]));
                return subtask;
            }
            case "EPIC" -> {
                Epic epic = new Epic(title, description);
                epic.setId(id);
                epic.setStatus(status);
                List<Integer> subIds = new ArrayList<>();
                for (String subId : parts[5].replaceAll("\\]|\\[", "").split(", ")) {
                    subIds.add(Integer.parseInt(subId));
                }
                epic.setSubtasksIds(subIds);
                return epic;
            }
            default -> {
                return null;
            }
        }
    }


    private static String subtaskToString(Subtask subtask) {
        return String.format(SUBTASK_FORMAT, subtask.getId(), TaskTypes.SUBTASK, subtask.getTitle(),
                subtask.getDescription(), subtask.getStatus(), subtask.getEpicId());
    }

    private static String epicToString(Epic epic) {
        return String.format(EPIC_FORMAT, epic.getId(), TaskTypes.EPIC, epic.getTitle(), epic.getDescription(),
                epic.getStatus(), epic.getSubtasksIds());
    }

    enum TaskTypes {
        TASK,
        EPIC,
        SUBTASK
    }
}
