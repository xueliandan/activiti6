package com.example.boot_activiti6.listener.usertask;

import org.activiti.engine.HistoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateTask;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.runtime.ProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author zxb 2023/8/30 9:49
 */
@Component
public class UserTaskCompleteListener implements TaskListener {

    @Resource
    private HistoryService historyService;

    @Resource
    private RuntimeService runtimeService;

    private static final Logger log = LoggerFactory.getLogger(UserTaskCompleteListener.class);

    @Override
    public void notify(DelegateTask delegateTask) {
        String name = delegateTask.getName();
        String assignee = delegateTask.getAssignee();
        // 当前活动的流程实例
        List<ProcessInstance> activeProcessInstances = runtimeService.createProcessInstanceQuery()
                .active()
                .list();
        int activeProcSize = activeProcessInstances.size();
        // 已完成的流程实例
        List<HistoricProcessInstance> completedProcessInstances = historyService.createHistoricProcessInstanceQuery()
                .finished()
                .list();
        int completeProcSize = completedProcessInstances.size();
        log.info("任务 [{}] 已被 [{}] 完成, 当前活动的流程实例数为 {}, 已完成的流程实例数为 {}", name, assignee, activeProcSize, completeProcSize);
    }
}
