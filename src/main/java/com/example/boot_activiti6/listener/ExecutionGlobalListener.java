package com.example.boot_activiti6.listener;

import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;

/**
 * @author zxb 2023/9/1 14:56
 */
@Slf4j
public class ExecutionGlobalListener  implements ExecutionListener {
    @Override
    public void notify(DelegateExecution execution) {

    }
}
