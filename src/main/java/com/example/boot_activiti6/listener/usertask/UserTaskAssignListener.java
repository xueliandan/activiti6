package com.example.boot_activiti6.listener.usertask;

import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author zxb 2023/8/30 9:49
 */
@Component
public class UserTaskAssignListener implements TaskListener {

    private static final Logger log = LoggerFactory.getLogger(UserTaskAssignListener.class);

    @Override
    public void notify(DelegateTask delegateTask) {
        String name = delegateTask.getName();
        String assignee = delegateTask.getAssignee();
        log.info("给任务 [{}] 设置责任人 [{}]", name, assignee);
    }
}
