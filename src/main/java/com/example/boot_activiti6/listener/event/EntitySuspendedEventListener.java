package com.example.boot_activiti6.listener.event;

import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.delegate.event.ActivitiEventType;

/**
 * @author zxb 2023/9/11 21:21
 */
@Slf4j
public class EntitySuspendedEventListener implements ActivitiEventListener {
    @Override
    public void onEvent(ActivitiEvent event) {
        String processDefinitionId = event.getProcessDefinitionId();
        String processInstanceId = event.getProcessInstanceId();
        String executionId = event.getExecutionId();
        ActivitiEventType type = event.getType();
        log.info("-----执行 EntitySuspendedEvent 事件监听器,流程定义ID:{},流程实例ID:{},执行对象ID:{}, 事件类型:{}", processDefinitionId, processInstanceId, executionId, type);
    }

    @Override
    public boolean isFailOnException() {
        return false;
    }
}