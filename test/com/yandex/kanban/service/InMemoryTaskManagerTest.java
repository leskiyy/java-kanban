package com.yandex.kanban.service;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {
    @Override
    TaskManager getTaskManager() {
        return new InMemoryTaskManager();
    }
}
