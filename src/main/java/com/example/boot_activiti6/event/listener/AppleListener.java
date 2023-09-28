package com.example.boot_activiti6.event.listener;

import com.example.boot_activiti6.event.entity.AppleEvent;
import com.google.common.eventbus.Subscribe;

/**
 * @author zxb 2023/9/26 8:54
 */
public class AppleListener {
    @Subscribe
    public void execute(AppleEvent appleEvent) {
        System.out.println(appleEvent + "制作完毕!");
    }
}
