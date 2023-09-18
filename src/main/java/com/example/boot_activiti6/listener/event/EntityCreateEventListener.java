package com.example.boot_activiti6.listener.event;

import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.model.ActivitiListener;
import org.activiti.bpmn.model.EventListener;
import org.activiti.bpmn.model.ImplementationType;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.springframework.stereotype.Component;

/**
 * @author zxb 2023/9/11 21:20
 */
@Slf4j
@Component
public class EntityCreateEventListener extends EventListener implements ActivitiEventListener {

    @Override
    public void onEvent(ActivitiEvent event) {
        String executionId = event.getExecutionId();
        String processDefinitionId = event.getProcessDefinitionId();
        String processInstanceId = event.getProcessInstanceId();
        ActivitiEventType type = event.getType();
        log.info("EntityCreateEventListener: getExecutionId 为 {}, processDefinitionId 为 {}, processInstanceId 为 {}, type 为 {}", executionId, processDefinitionId, processInstanceId, type);
    }

    @Override
    public boolean isFailOnException() {
        return false;
    }
}
