package com.example.boot_activiti6.listener.usertask;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;


/**
 * @author zxb 2023/8/30 9:49
 */
@Component
public class UserTaskCreateListener implements TaskListener {

    private static final Logger log = LoggerFactory.getLogger(UserTaskCreateListener.class);

    @Override
    public void notify(DelegateTask delegateTask) {
        String name = delegateTask.getName();
        log.info("任务 [{}] 创建", name);
    }
}
