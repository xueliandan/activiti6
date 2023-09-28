package com.example.boot_activiti6.test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author zxb 2023/9/26 9:30
 */
public class Context {
    private final List<MyEvent> events = new CopyOnWriteArrayList<>();

    public void register(MyEvent myEvent) {
        events.add(myEvent);
    }


    public void unregister(MyEvent myEvent) {
        events.remove(myEvent);
    }

    public void post(MyEvent myEvent) {

    }
}
