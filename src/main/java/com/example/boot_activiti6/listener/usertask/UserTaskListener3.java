package com.example.boot_activiti6.listener.usertask;

import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.impl.persistence.entity.VariableInstance;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author zxb 2023/8/30 9:49
 */
@Slf4j
@Component("userTaskListener3")
public class UserTaskListener3 implements TaskListener {

    @Override
    public void notify(DelegateTask delegateTask) {
        String name = delegateTask.getName();
        String eventName = delegateTask.getEventName();
        Map<String, VariableInstance> variableInstances = delegateTask.getVariableInstances();
        String assignee = delegateTask.getAssignee();
        log.warn("33333333333333333用户任务节点的审批人为 : {}, 任务名称为 : {}, 事件名称为 : {}, 流程变量为 : {}", assignee, name, eventName, variableInstances);
    }
}
