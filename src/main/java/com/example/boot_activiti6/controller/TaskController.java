package com.example.boot_activiti6.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.boot_activiti6.exception.BusinessException;
import com.example.boot_activiti6.model.constant.ProcessConstant;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.model.*;
import org.activiti.bpmn.model.Process;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.*;
import org.activiti.engine.delegate.event.ActivitiEventType;
import org.activiti.engine.task.Task;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
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

    @Resource
    private RuntimeService runtimeService;

    @Resource
    private TaskService taskService;

    @Resource
    private RepositoryService repositoryService;

    /**
     * 通过 Task 对象设置任务执行人，是不会触发任务监听器的
     * task1.setAssignee(managerApply); 这样是不行的
     * 通过 TaskService 来设置任务执行人，才能触发任务监听器
     * taskService.setAssignee(task.getId(), managerApply); 这样才可以
     *
     * @return 是否成功
     */
    @GetMapping(path = "/assignee-set")
    public String setTaskAssignee(@RequestParam(value = "taskId") String taskId) {
        // 这里我是直接看数据库知道任务 ID 的，实际业务中要看情况获取
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        // 获取运行时的参数
        String variables = (String) runtimeService.getVariable(task.getExecutionId(), "variables");
        JSONObject jsonObject = JSON.parseObject(variables);
        // 从运行时参数中获取执行人
        String managerApply = jsonObject.getString("manager_apply");
        // 测试就直接写死执行人了
        taskService.setAssignee(task.getId(), managerApply);
        return "success";
    }

    @GetMapping(path = "/assignee-set-direct")
    public String setTaskAssignee(@RequestParam(value = "taskId") String taskId,
                                  @RequestParam(value = "assignee") String assignee) {
        // 这里我是直接看数据库知道任务 ID 的，实际业务中要看情况获取
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        // 测试就直接写死执行人了
        taskService.setAssignee(task.getId(), assignee);
        return "success";
    }

    @GetMapping(path = "/completeTask")
    public String completeEvectionApply(@RequestParam(value = "taskId") String taskId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        log.warn("结束的任务名为：{}， 任务 ID 为 {}", task.getName(), task.getId());
        // 拿到任务 ID，然后 complete 去完成该任务。完成后，该节点结束。
        taskService.complete(task.getId());
        return "success";
    }

    @GetMapping(path = "/taskInfo")
    public String getCurrentTaskInfo() {
        // 任务信息需要通过 TaskService 来获取
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
        BpmnModel bpmnModel = repositoryService.getBpmnModel(ProcessConstant.PROCESS_DEF_ID);
        if (Objects.isNull(bpmnModel)) {
            throw new BusinessException("无法根据流程定义找到对应的流程模型!!!");
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
        BpmnModel bpmnModel = repositoryService.getBpmnModel(ProcessConstant.PROCESS_DEF_ID);
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
                    // 事件类型，例如 "create" 表示创建任务
                    taskListener.setEvent("assignment");
                    // 监听器实现类型
                    taskListener.setImplementationType(ImplementationType.IMPLEMENTATION_TYPE_CLASS);
                    // 监听器类名
                    taskListener.setImplementation("com.example.boot_activiti6.listener.usertask.UserTaskListener");
                    taskListeners.add(taskListener);
                }
            }
        }
        return "success";
    }

    /**
     * isSequential: 表示并行，还是顺序。
     * loop cardinality：循环基数。可选项。可以直接填整数，表示会签、或签的人数 - (会创建基数个任务实例)  使用该参数只能保证生成相应的任务，但是生成的任务没有 assign 。该参数跟下面 collection 二选一就行
     * flowable:collection: 此种方式是表示的会签、或签的具体人。这里xml只需要约定好固定的格式 即可。比如 flowable:collection="Activity_1g65lke_approverList"
     * flowable:elementVariable: 元素变量, 这里xml只需要约定好固定的格式 即可 flowable:elementVariable="approver"
     * completionCondition：完成条件。这个条件控制着这里是会签、或签如何才能算完成。
     * <p>
     * nrOfCompletedInstances: 完成的任务实例数
     * nrOfInstances: 总共生成的任务实例数(根据会签、或签指定的人数生成相应的任务数)
     * nrOfActiveInstance: 未完成实例的数目
     * loopCounter: 循环计数器，办理人在列表中的索引
     * 参考配置
     * <p>
     * 当是或签时，直接固定配置： ${nrOfCompletedInstances>=1} 即可
     * <p>
     * 当是会签时，固定配置： ${nrOfCompletedInstances==nrOfInstances} 即可
     *
     */
    public void setMultiInstance(@RequestParam("nodeId") String nodeId,
                                 @RequestParam("modelId") String modelId) {
        BpmnModel bpmnModel = repositoryService.getBpmnModel(ProcessConstant.PROCESS_DEF_ID);
        Process mainProcess = bpmnModel.getMainProcess();
        FlowElement flowElement = mainProcess.getFlowElement(nodeId);
        UserTask userTask = (UserTask) flowElement;
        // 通过变量来设置负责人
        userTask.setAssignee("${" + "masterAssign" + "}");

        // 设置多实例(会签)
        MultiInstanceLoopCharacteristics multiInstanceLoopCharacteristics = new MultiInstanceLoopCharacteristics();
        // 设置集合变量。这个变量是运行时需要的变量，要传进来，变量名和这里设置的变量名要一致
        multiInstanceLoopCharacteristics.setInputDataItem("masterAssignList");
        // 设置单个变量的名称。这个单个变量的名称必须和任务 setAssign 的变量名保持一致，不然无法获取!
        multiInstanceLoopCharacteristics.setElementVariable("masterAssign");
        // 设置为同时接收，也就是并行。可以体会串行和并行的区别。
        multiInstanceLoopCharacteristics.setSequential(false);
        // 设置流转的条件，这里也是一个表达式，因为是会签，那么就让完成实例数等于总共的实例数即可
        multiInstanceLoopCharacteristics.setCompletionCondition("${nrOfCompletedInstances=nrOfInstances}");

        // 设置该任务节点为会签节点
        userTask.setLoopCharacteristics(multiInstanceLoopCharacteristics);
        // 保存
        ObjectNode objectNode = new BpmnJsonConverter().convertToJson(bpmnModel);
        // 接下来就是要搞到这个 modelId
        repositoryService.addModelEditorSource(modelId, objectNode.toString().getBytes(StandardCharsets.UTF_8));
    }

}
