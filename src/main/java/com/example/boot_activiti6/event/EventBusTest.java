package com.example.boot_activiti6.event;

import com.example.boot_activiti6.event.entity.AppleEvent;
import com.example.boot_activiti6.event.entity.CakeEvent;
import com.example.boot_activiti6.event.listener.AppleListener;
import com.example.boot_activiti6.event.listener.CakeListener;
import com.google.common.eventbus.EventBus;
import org.junit.Test;

/**
 * @author zxb 2023/9/22 15:08
 */
public class EventBusTest {

    /**
     * 原生使用 Google 的 eventBus
     */
    @Test
    public void testPrototype() {
        EventBus eventBus = new EventBus();
        eventBus.register(new CakeListener());
        eventBus.register(new AppleListener());
        CakeEvent cakeEvent = new CakeEvent();
        cakeEvent.setName("肉松面包");
        AppleEvent appleEvent = new AppleEvent();
        appleEvent.setColor("红色");
        eventBus.post(cakeEvent);
        eventBus.post(appleEvent);
    }
}
