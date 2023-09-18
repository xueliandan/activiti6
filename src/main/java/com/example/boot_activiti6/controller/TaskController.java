package com.example.boot_activiti6.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.boot_activiti6.listener.event.EntityCreateEventListener;
import com.example.boot_activiti6.listener.event.TaskCompletedEventListener;
import com.example.boot_activiti6.listener.usertask.UserTaskListener;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.model.*;
import org.activiti.bpmn.model.Process;
import org.activiti.engine.*;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * @author zxb 2023/9/1 10:38
 */
@Slf4j
@RestController
@RequestMapping(path = "/task")
public class TaskController {

    @Autowired
    private ProcessEngine processEngine;


    /**
     * 通过 Task 对象设置任务执行人，是不会触发任务监听器的
     * task1.setAssignee(managerApply); 这样是不行的
     * 通过 TaskService 来设置任务执行人，才能触发任务监听器
     * taskService.setAssignee(task.getId(), managerApply); 这样才可以
     *
     * @return 是否成功
     */
    @GetMapping(path = "/assignee-set")
    public String setManagerTaskAssignee(@RequestParam(value = "taskId") String taskId,
                                         @RequestParam(value = "assignee") String assignee) {
        TaskService taskService = processEngine.getTaskService();
        // 这里我是直接看数据库知道任务 ID 的，实际业务中要看情况获取
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        RuntimeService runtimeService = processEngine.getRuntimeService();
//        // 获取运行时的参数
        String variables = (String) runtimeService.getVariable(task.getExecutionId(), "variables");
        JSONObject jsonObject = JSON.parseObject(variables);
        // 从运行时参数中获取执行人
        String managerApply = jsonObject.getString("manager_apply");
//         测试就直接写死执行人了
////         运行时添加监听器
        taskService.setAssignee(task.getId(), managerApply);
        return "success";
    }

    @GetMapping(path = "/completeTask")
    public String completeEvectionApply(@RequestParam(value = "taskId") String taskId) {
        TaskService taskService = processEngine.getTaskService();
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        log.warn("结束的任务名为：{}， 任务 ID 为 {}", task.getName(), task.getId());
        // 拿到任务 ID，然后 complete 去完成该任务。完成后，该节点结束。
        taskService.complete(task.getId());
        return "success";
    }

    @GetMapping(path = "/taskInfo")
    public String getCurrentTaskInfo() {
        // 任务信息需要通过 TaskService 来获取
        TaskService taskService = processEngine.getTaskService();
        Task task1 = taskService.createTaskQuery().taskId("11").singleResult();
        log.warn("流程定义 ID ： {}", task1.getProcessDefinitionId());
        log.warn("流程实例 ID ： {}", task1.getProcessInstanceId());
        log.warn("流程变量 ： {}", task1.getProcessVariables());
        log.warn("任务负责人为 ： {}", task1.getAssignee());
        log.warn("任务名称为： {}", task1.getName());
        log.warn("任务 ID 为 : {}", task1.getId());
        // 创建任务查询
//        List<Task> assigneeTasks = taskService.createTaskQuery()
//                // 指定流程定义的 key，告诉 activiti 你要查询哪个流程中的任务，key 是 act_re_procdef 的 KEY_ 字段的值
//                .processDefinitionKey("myProcess_1")
//                // .processDefinitionId("myProcess_1:1:4") // 也可以使用流程定义的 ID 进行查询
//                // 指定谁负责的任务，告诉 activiti 你要查询当前审批流程中谁负责的任务
//                .taskAssignee(assignee)
//                // .singleResult() 该用户可能负责多个任务，如果你确定只有一个，可以用 singleResult
//                .list();
//        for (Task task : assigneeTasks) {
//            log.warn("流程定义 ID ： {}", task.getProcessDefinitionId());
//            log.warn("流程实例 ID ： {}", task.getProcessInstanceId());
//            log.warn("流程变量 ： {}", task.getProcessVariables());
//            log.warn("任务负责人为 ： {}", task.getAssignee());
//            log.warn("任务名称为： {}", task.getName());
//            log.warn("任务 ID 为 : {}", task.getId());
//        }
        return "success";
    }

    @Resource
    RuntimeService runtimeService;
    @Resource
    TaskService taskService;
    @Resource
    ManagementService managementService;
    @Resource
    RepositoryService repositoryService;


    /**
     * 事件监听器
     */
    @GetMapping(path = "/events")
    public String getEventListener() {
        BpmnModel bpmnModel = repositoryService.getBpmnModel("EvectionProcess:7:60009");
        Process mainProcess = bpmnModel.getMainProcess();
        List<EventListener> eventListeners = mainProcess.getEventListeners();
        EventListener taskCompletedListener = new EventListener();
        taskCompletedListener.setEntityType(ActivitiEventType.TASK_COMPLETED.name());
        taskCompletedListener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
        taskCompletedListener.setImplementation("com.example.boot_activiti6.listener.event.TaskCompletedEventListener");
        eventListeners.add(taskCompletedListener);
//        repositoryService.saveModel(bpmnModel);
        return "success";
    }

    /**
     * 执行监听器，执行监听器所有节点均可配置(除网关外)
     *
     * @param nodeId 节点 ID
     * @return success
     */
    @GetMapping(path = "/executions")
    public String getExecutionsListener(@RequestParam("nodeId") String nodeId) {
        BpmnModel bpmnModel = repositoryService.getBpmnModel("EvectionProcess:7:60009");
        if (Objects.isNull(bpmnModel)) {
            throw new RuntimeException("无法根据流程定义找到对应的流程模型!!!");
        }
        Process mainProcess = bpmnModel.getMainProcess();
        Collection<FlowElement> flowElements = mainProcess.getFlowElements();
        // 执行监听器
        for (FlowElement flowElement : flowElements) {
            String id = flowElement.getId();
            if (id.equals(nodeId)) {
                List<ActivitiListener> executionListeners = flowElement.getExecutionListeners();
                // 创建一个执行监听器
                ActivitiListener executionListener = new ActivitiListener();
                executionListener.setEvent("end");
                executionListener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
                executionListener.setImplementation("com.example.boot_activiti6.listener.execution.TestExecutionListener1");
                executionListeners.add(executionListener);
            }
        }
        return "success";
    }

    /**
     * 用户监听器
     */
    @GetMapping(path = "/tasks")
    public String getUserTaskListener() {
        BpmnModel bpmnModel = repositoryService.getBpmnModel("EvectionProcess:7:60009");
        Process mainProcess = bpmnModel.getMainProcess();
        Collection<FlowElement> flowElements = mainProcess.getFlowElements();
        // 任务监听器
        for (FlowElement flowElement : flowElements) {
            if (flowElement instanceof UserTask) {
                UserTask userTask = (UserTask) flowElement;
                String id = userTask.getId();
                if (id.equals("finance_apply")) {
                    List<ActivitiListener> taskListeners = userTask.getTaskListeners();
                    ActivitiListener taskListener = new ActivitiListener();
                    taskListener.setEvent("assignment"); // 事件类型，例如 "create" 表示创建任务
                    taskListener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS); // 监听器实现类型
                    taskListener.setImplementation("com.example.boot_activiti6.listener.usertask.UserTaskListener4"); // 监听器类名
                    taskListeners.add(taskListener);
                }
            }
        }
        return "success";
    }

}
