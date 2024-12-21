package com.yandex.kanban.util;

import com.yandex.kanban.model.Epic;
import com.yandex.kanban.model.Subtask;
import com.yandex.kanban.model.Task;
import com.yandex.kanban.model.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Converter {
    private static final String TASK_FORMAT = "%d;%s;%s;%s;%s;%s;%d;%d;%b"; // "id;type;title;description;status;startTime;duration;historyManagerPosition;isInPrioritizedTasks"
    private static final String EPIC_FORMAT = "%d;%s;%s;%s;%s;%s;%d;%s;%d;%b"; // "id;type;title;description;status;startTime;duration;[sub,ids];historyManagerPosition;isInPrioritizedTasks"
    private static final String SUBTASK_FORMAT = "%d;%s;%s;%s;%s;%s;%d;%d;%d;%b"; // "id;type;title;description;status;startTime;duration;historyManagerPosition;epicID;isInPrioritizedTasks(always false)"
    private static final DateTimeFormatter START_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");

    public static String taskToString(Task task, int historyManagerPosition, boolean isInPrioritizedTasks) {
        if (task instanceof Epic) {
            return epicToString((Epic) task, historyManagerPosition);
        } else if (task instanceof Subtask) {
            return subtaskToString((Subtask) task, historyManagerPosition, isInPrioritizedTasks);
        } else {
            return String.format(TASK_FORMAT, task.getId(), TaskTypes.TASK, task.getTitle(), task.getDescription(),
                    task.getStatus(), formatStartTime(task.getStartTime()), task.getDuration().toMinutes(),
                    historyManagerPosition, isInPrioritizedTasks);
        }
    }

    public static Task stringToTask(String value) {
        String[] parts = value.split(";");

        int id = Integer.parseInt(parts[0]);
        String type = parts[1];
        String title = parts[2];
        String description = parts[3];
        TaskStatus status = TaskStatus.valueOf(parts[4]);
        LocalDateTime startTime = formStringToDateTime(parts[5]);
        Duration duration = Duration.ofMinutes(Long.parseLong(parts[6]));
        switch (type) {
            case "TASK" -> {
                Task task = new Task(title, description);
                task.setId(id);
                task.setStatus(status);
                task.setStartTime(startTime);
                task.setDuration(duration);
                return task;
            }
            case "SUBTASK" -> {
                Subtask subtask = new Subtask(title, description);
                subtask.setId(id);
                subtask.setStatus(status);
                subtask.setStartTime(startTime);
                subtask.setDuration(duration);
                subtask.setEpicId(Integer.parseInt(parts[7]));
                return subtask;
            }
            case "EPIC" -> {
                Epic epic = new Epic(title, description);
                epic.setId(id);
                epic.setStatus(status);
                epic.setStartTime(startTime);
                epic.setDuration(duration);
                List<Integer> subIds = new ArrayList<>();
                for (String subId : parts[7].replaceAll("\\]|\\[", "").split(", ")) {
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


    private static String subtaskToString(Subtask subtask, int historyManagerPosition, boolean isInPrioritizedTasks) {
        return String.format(SUBTASK_FORMAT, subtask.getId(), TaskTypes.SUBTASK, subtask.getTitle(),
                subtask.getDescription(), subtask.getStatus(), formatStartTime(subtask.getStartTime()),
                subtask.getDuration().toMinutes(), subtask.getEpicId(), historyManagerPosition, isInPrioritizedTasks);
    }

    private static String epicToString(Epic epic, int historyManagerPosition) {
        return String.format(EPIC_FORMAT, epic.getId(), TaskTypes.EPIC, epic.getTitle(), epic.getDescription(),
                epic.getStatus(), formatStartTime(epic.getStartTime()), epic.getDuration().toMinutes(),
                epic.getSubtasksIds(), historyManagerPosition, false);
    }

    private static String formatStartTime(LocalDateTime startTime) {
        try {
            return startTime.format(START_TIME_FORMATTER);
        } catch (NullPointerException e) {
            return "null";
        }
    }

    private static LocalDateTime formStringToDateTime(String formatTime) {
        if (formatTime.equals("null")) {
            return null;
        } else {
            return LocalDateTime.parse(formatTime, START_TIME_FORMATTER);
        }
    }

    enum TaskTypes {
        TASK,
        EPIC,
        SUBTASK
    }
}
