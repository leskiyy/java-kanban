package com.yandex.kanban.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ManagersTest {

    @Test
    void initManager() {
        TaskManager tm = Managers.getDefault();
        HistoryManager hm = Managers.getDefaultHistory();
        assertNotNull(tm, "TaskManager не проинициилизирован");
        assertNotNull(hm, "HistoryManager не проинициилизирован");
    }

}