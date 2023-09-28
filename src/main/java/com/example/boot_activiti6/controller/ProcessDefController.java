package com.example.boot_activiti6.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.example.boot_activiti6.model.constant.ProcessConstant;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.HistoryService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * @author zxb 2023/8/30 17:52
 */
@Slf4j
@RestController
@RequestMapping(path = "/")
public class ProcessDefController {

    /**
     * 注入流程引擎
     */
    @Resource
    private ProcessEngine processEngine;

    @Resource
    private RepositoryService repositoryService;


    //***************************************** 根据流程定义启动流程 *****************************************//

    /**
     * 启动流程时不带上参数
     */
    @PostMapping(path = "/start")
    public String startProcessInstance() {
        // 流程定义的key,通过这个key来启动流程实例,这个 key 是 act_re_procdef 表中的 KEY_ 字段
        // 获取 RuntimeService
        RuntimeService runtimeService = processEngine.getRuntimeService();
        ProcessInstance processInstance = runtimeService
                .startProcessInstanceById(ProcessConstant.PROCESS_DEF_ID);
        print(processInstance);
        return "success";
    }

    /**
     * 根据流程图模板创建一个流程实例并启动，带上流程参数
     */
    @PostMapping(path = "/startWithVariables")
    public String startProcessInstance(@RequestBody String variables) {
        // 流程定义的key,通过这个key来启动流程实例,这个 key 是 act_re_procdef 表中的 KEY_ 字段
        // 获取 RuntimeService
        RuntimeService runtimeService = processEngine.getRuntimeService();
        // 使用流程定义的 key 【启动流 程实例】，key对应 evection.bpmn 文件中id的属性值，使用key值启动，默认是按照最新版本的流程定义启动
        Map<String, Object> applyVariablesMap = JSON.parseObject(variables, new TypeReference<Map<String, Object>>() {
        });
        ProcessInstance processInstance = runtimeService
                // startProcessInstanceByKey方法还可以设置其他的参数，比如流程变量。
                .startProcessInstanceById(ProcessConstant.PROCESS_DEF_ID, applyVariablesMap);
        print(processInstance);
        return "success";
    }

    private void print(ProcessInstance processInstance) {
        LocalDateTime localDateTime = LocalDateTime.now();
        String deploymentTime = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        // 创建的流程实例的 ID
        String processInstanceId = processInstance.getId();
        // 流程定义 ID 就是流程模板的 ID，也就是 act_re_procdef 表中的 ID
        String processDefinitionId = processInstance.getProcessDefinitionId();
        String activityId = processInstance.getActivityId();
        log.warn("流程实例 ID 为 {}，流程定义 ID 为 {}，当前活动 ID 为 {}，启动时间为 {}", processInstanceId, processDefinitionId, activityId, deploymentTime);
    }

    //***************************************** 查看流程定义信息 *****************************************//

    @GetMapping(path = "/processDefInfo")
    public String getProcessDefInfo() {
        RepositoryService repositoryService = processEngine.getRepositoryService();

        // 可以通过流程定义 id 直接获取流程定义对象 : repositoryService.getProcessDefinition("myProcess_1:1:4");
        // 也可以使用 ProcessDefinitionQuery 来创建一个查询对象，实现更强大的查询
        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();
        List<ProcessDefinition> processDefinitionList = processDefinitionQuery.processDefinitionKey(ProcessConstant.PROCESS_DEF_KEY).orderByProcessDefinitionVersion().desc().list();
        for (ProcessDefinition processDefinition : processDefinitionList) {
            log.warn("流程定义 id = {}", processDefinition.getId());
            log.warn("流程定义 name= {},", processDefinition.getName());
            log.warn("流程定义 key = {}", processDefinition.getKey());
            log.warn("流程定义 Version = {}", processDefinition.getVersion());
            log.warn("流程部署ID = {}", processDefinition.getDeploymentId());
        }
        return "success";
    }

    //***************************************** 删除流程定义信息 *****************************************//

    @GetMapping(path = "/deleteProcessDefInfo")
    public String deleteProcessDefInfo() {
        RepositoryService repositoryService = processEngine.getRepositoryService();
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(ProcessConstant.PROCESS_DEF_ID).singleResult();
        String deploymentId = processDefinition.getDeploymentId();
        // 根据部署 id 删除流程定义。如果该流程顶一下有正在运行的流程实例，则报错
        repositoryService.deleteDeployment(deploymentId);
        // 级联删除，cascade 设置为 true，则如果该流程定义下有运行的流程实例，也可以删除，一起删掉。
        // repositoryService.deleteDeployment(deploymentId, true);
        return "success";
    }

    //***************************************** 查看历史流程相关信息 *****************************************//

    @GetMapping(path = "/getHisProcessInsInfo")
    public String getHisProcessInsInfo() {
        HistoryService historyService = processEngine.getHistoryService();
        HistoricProcessInstanceQuery historicProcessInstanceQuery = historyService.createHistoricProcessInstanceQuery();
        // 可以通过 act_hi_procinst 中的 PROC_INST_ID_ 字段值去查询
        // historicProcessInstanceQuery.processInstanceId("2501");
        // 也可以通过  act_hi_procinst 中的 PROC_DEF_ID_ 字段值去查询
        historicProcessInstanceQuery.processDefinitionId(ProcessConstant.PROCESS_DEF_ID);
        // 设置结果根据开始时间升序排列
        historicProcessInstanceQuery.orderByProcessInstanceStartTime().asc();
        List<HistoricProcessInstance> list = historicProcessInstanceQuery.list();
        for (HistoricProcessInstance historicProcessInstance : list) {
            String businessKey = historicProcessInstance.getBusinessKey();
            String processDefinitionId = historicProcessInstance.getProcessDefinitionId();
            String id = historicProcessInstance.getId();
            log.warn("业务 ID ： {}， 流程定义 ID ： {}， 流程实例 ID ： {}", businessKey, processDefinitionId, id);
        }
        return "success";
    }

    /**
     * 获取某个流程实例它所有的审批活动
     */
    @GetMapping(path = "/getHisProcessActInfo")
    public String getHisProcessActInfo() {
        HistoryService historyService = processEngine.getHistoryService();
        HistoricActivityInstanceQuery historicActivityInstanceQuery = historyService.createHistoricActivityInstanceQuery();
        // 可以通过 act_hi_actinst 中的 PROC_INST_ID_ 字段值去查询
        // historicProcessInstanceQuery.processInstanceId("2501");
        // 也可以通过  act_hi_actinst 中的 PROC_DEF_ID_ 字段值去查询
        historicActivityInstanceQuery.processDefinitionId(ProcessConstant.PROCESS_DEF_ID);
        // 设置结果根据开始时间升序排列
        historicActivityInstanceQuery.orderByHistoricActivityInstanceStartTime().asc();
        List<HistoricActivityInstance> list = historicActivityInstanceQuery.list();
        for (HistoricActivityInstance historicActivityInstance : list) {
            String activityId = historicActivityInstance.getActivityId();
            String activityName = historicActivityInstance.getActivityName();
            String assignee = historicActivityInstance.getAssignee();
            String processDefinitionId = historicActivityInstance.getProcessDefinitionId();
            String processInstanceId = historicActivityInstance.getProcessInstanceId();
            log.warn("活动 ID ： {}，活动名称 ： {}，审批人 ： {}，流程定义 ID ： {}， 流程实例 ID ： {}", activityId, activityName, assignee, processDefinitionId, processInstanceId);
        }
        return "success";
    }

}
