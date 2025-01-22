package com.yandex.kanban.util;

import com.google.gson.Gson;
import com.yandex.kanban.adapters.DurationAdapter;
import com.yandex.kanban.adapters.LocalDateTimeAdapter;
import com.yandex.kanban.model.Task;

import java.time.Duration;
import java.time.LocalDateTime;

public class GsonHolder {

    private static Gson gson = new Gson().newBuilder()
            .setPrettyPrinting()
            .serializeNulls()
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(Task.class, new TaskDeserializer())
            .create();

    private GsonHolder() {
    }

    public static Gson getGson() {
        return gson;
    }
}
