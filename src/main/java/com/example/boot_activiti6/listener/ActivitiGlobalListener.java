package com.example.boot_activiti6.listener;

import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.ActivitiEventType;

/**
 * @author zxb 2023/9/1 9:31
 */
@Slf4j
public class ActivitiGlobalListener implements ActivitiEventListener {
    @Override
    public void onEvent(ActivitiEvent event) {
        String executionId = event.getExecutionId();
        String processDefinitionId = event.getProcessDefinitionId();
        String processInstanceId = event.getProcessInstanceId();
        ActivitiEventType type = event.getType();
        log.info("getExecutionId 为 {}, processDefinitionId 为 {}, processInstanceId 为 {}, type 为 {}", executionId, processDefinitionId, processInstanceId, type);
    }

    @Override
    public boolean isFailOnException() {
        return true;
    }
}
