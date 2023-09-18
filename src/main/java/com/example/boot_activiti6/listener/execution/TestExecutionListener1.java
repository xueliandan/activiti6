package com.example.boot_activiti6.listener.execution;

import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Component;

/**
 * @author zxb 2023/9/4 22:04
 */
@Slf4j
@Component
public class TestExecutionListener1   {

//    @Override
    public void notify(DelegateExecution delegateExecution) {
        String eventName = delegateExecution.getEventName();
        String processInsBusinessKey = delegateExecution.getProcessInstanceBusinessKey();
        String processInstanceId = delegateExecution.getProcessInstanceId();
        log.info("[eventName : {}, processInsBusinessKey : {}, processInstanceId : {}]", eventName, processInsBusinessKey,processInstanceId);
        log.info("TestExecutionListener1111111  {}",delegateExecution.getCurrentFlowElement().getName());
    }
}
