package com.yandex.kanban.util;

import com.google.gson.*;
import com.yandex.kanban.adapters.DurationAdapter;
import com.yandex.kanban.adapters.LocalDateTimeAdapter;
import com.yandex.kanban.model.Epic;
import com.yandex.kanban.model.Subtask;
import com.yandex.kanban.model.Task;

import java.lang.reflect.Type;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class TaskDeserializer implements JsonDeserializer<Task> {
    private String taskTypeElementName = "type";
    private Gson gson;
    private Map<String, Class<? extends Task>> taskTypeReg;

    public TaskDeserializer() {
        this.gson = new Gson().newBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .create();
        taskTypeReg = new HashMap<>();
        taskTypeReg.put("TASK", Task.class);
        taskTypeReg.put("SUBTASK", Subtask.class);
        taskTypeReg.put("EPIC", Epic.class);
    }

    @Override
    public Task deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject taskObj = jsonElement.getAsJsonObject();
        JsonElement taskTypeEl = taskObj.get(taskTypeElementName);

        Class<? extends Task> taskType = taskTypeReg.get(taskTypeEl.getAsString());
        return gson.fromJson(taskObj, taskType);
    }
}
