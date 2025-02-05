package com.yandex.kanban.service;

import com.yandex.kanban.model.Task;
import com.yandex.kanban.util.HistoryStorage;

import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    HistoryStorage<Task> historyStorage = new HistoryStorage<>();

    @Override
    public void add(Task task) {
        if (task == null) {
            return;
        }
        historyStorage.linkLast(task);
    }

    @Override
    public void remove(int id) {
        historyStorage.remove(id);
    }

    @Override
    public List<Task> getHistory() {
        return historyStorage.getTasks();
    }

    public void clear() {
        historyStorage.clear();
    }

}