package service;

import com.yandex.kanban.service.HistoryManager;
import com.yandex.kanban.service.Managers;
import com.yandex.kanban.service.TaskManager;
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