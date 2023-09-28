package com.example.boot_activiti6.event.listener;

import com.example.boot_activiti6.event.entity.CakeEvent;
import com.google.common.eventbus.Subscribe;

/**
 * @author zxb 2023/9/22 15:06
 */
public class CakeListener {

    @Subscribe
    public void execute(CakeEvent cakeEvent) {
        System.out.println(cakeEvent + "制作完毕!");
    }
}
