package com.example.boot_activiti6.controller;

import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.*;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.history.HistoricActivityInstanceQuery;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.history.HistoricProcessInstanceQuery;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.repository.ProcessDefinitionQuery;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.zip.ZipInputStream;

/**
 * @author zxb 2023/1/10 16:53
 */
@RestController
@Slf4j
public class BpmnController {

    /**
     * 注入流程引擎
     */
    @Autowired
    private ProcessEngine processEngine;

    //***************************************** 流程部署 *****************************************//

    /**
     * 部署一个流程图,就是将流程图文件和图像保存到数据库中,同时创建该流程图定义模板
     */
    @GetMapping(path = "/processDeploy")
    public String processDefine() {
        // 通过 processEngine 获取仓库
        RepositoryService repositoryService = processEngine.getRepositoryService();
        // 创建一个部署对象
        Deployment deployment = repositoryService.createDeployment()
                // bpmn 文件，此种方式一次就只能加载一个文件
                .addClasspathResource("processes/evection.xml")
                // 加载 bpmn 文件生成的 png 文件，此种方式一次就只能加载一个文件
                .addClasspathResource("processes/evection.png")
                // 添加部署的名称(也就是这个流程的名称)
                .name("员工请假审批流程")
                // 完成部署
                .deploy();
        // 输出部署的信息
        LocalDateTime localDateTime = LocalDateTime.now();
        String deploymentTime = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        log.warn("部署 ID 为 {}，部署名称为 {}，部署时间为 {}", deployment.getId(), deployment.getName(), deploymentTime);
        return "success!";
    }

    /**
     * 部署流程图压缩包
     */
    @GetMapping(path = "/processZipDeploy")
    public String processZipDeploy() {
        // 定义zip输入流
        InputStream inputStream = this
                .getClass()
                .getClassLoader()
                .getResourceAsStream("bpmn/evection.zip");
        ZipInputStream zipInputStream = new ZipInputStream(inputStream);
        // 获取repositoryService
        RepositoryService repositoryService = processEngine.getRepositoryService();
        // 流程部署
        Deployment deployment = repositoryService.createDeployment()
                .addZipInputStream(zipInputStream)
                .deploy();
        // 输出部署的信息
        LocalDateTime localDateTime = LocalDateTime.now();
        String deploymentTime = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        log.warn("部署 ID 为 {}，部署名称为 {}，部署时间为 {}", deployment.getId(), deployment.getName(), deploymentTime);
        return "success!";
    }


    //***************************************** 创建审批流程 *****************************************//

    /**
     * 根据流程图模板创建一个流程实例并启动：创建一个出差审批流程
     */
    @GetMapping(path = "/start")
    public String startProcessInstance() {
        // 流程定义的key,通过这个key来启动流程实例,这个 key 是 act_re_procdef 表中的 KEY_ 字段
        String processDefinitionKey = "myProcess_1";
        // 获取 RuntimeService
        RuntimeService runtimeService = processEngine.getRuntimeService();
        // 使用流程定义的 key 【启动流程实例】，key对应 evection.xml 文件中id的属性值，使用key值启动，默认是按照最新版本的流程定义启动
        ProcessInstance processInstance = runtimeService
                // startProcessInstanceByKey方法还可以设置其他的参数，比如流程变量。
                .startProcessInstanceByKey(processDefinitionKey);
        LocalDateTime localDateTime = LocalDateTime.now();
        String deploymentTime = localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        // 创建的流程实例的 ID
        String processInstanceId = processInstance.getId();
        // 流程定义 ID 就是流程模板的 ID，也就是 act_re_procdef 表中的 ID
        String processDefinitionId = processInstance.getProcessDefinitionId();
        String activityId = processInstance.getActivityId();
        log.warn("流程实例 ID 为 {}，流程定义 ID 为 {}，当前活动 ID 为 {}，启动时间为 {}", processInstanceId, processDefinitionId, activityId, deploymentTime);
        return "success";
    }

    //***************************************** 查看任务信息 *****************************************//

    @GetMapping(path = "/taskInfo")
    public String getCurrentTaskInfo() {
        // 创建出差申请的审批人是张三
        String assignee = "张三";
        // 任务信息需要通过 TaskService 来获取
        TaskService taskService = processEngine.getTaskService();
        // 创建任务查询
        List<Task> assigneeTasks = taskService.createTaskQuery()
                // 指定流程定义的 key，告诉 activiti 你要查询哪个流程中的任务，key 是 act_re_procdef 的 KEY_ 字段的值
                .processDefinitionKey("myProcess_1")
                // .processDefinitionId("myProcess_1:1:4") // 也可以使用流程定义的 ID 进行查询
                // 指定谁负责的任务，告诉 activiti 你要查询当前审批流程中谁负责的任务
                .taskAssignee(assignee)
                // .singleResult() 该用户可能负责多个任务，如果你确定只有一个，可以用 singleResult
                .list();
        for (Task task : assigneeTasks) {
            log.warn("流程定义 ID ： {}", task.getProcessDefinitionId());
            log.warn("流程实例 ID ： {}", task.getProcessInstanceId());
            log.warn("流程变量 ： {}", task.getProcessVariables());
            log.warn("任务负责人为 ： {}", task.getAssignee());
            log.warn("任务名称为： {}", task.getName());
            log.warn("任务 ID 为 : {}", task.getId());
        }
        return "success";
    }

    //***************************************** 查看流程定义信息 *****************************************//

    @GetMapping(path = "/processDefInfo")
    public String getProcessDefInfo() {
        RepositoryService repositoryService = processEngine.getRepositoryService();

        // 可以通过流程定义 id 直接获取流程定义对象
        // ProcessDefinition processDefinition = repositoryService.getProcessDefinition("myProcess_1:1:4");

        // 也可以使用 ProcessDefinitionQuery 来创建一个查询对象，实现更强大的查询
        ProcessDefinitionQuery processDefinitionQuery = repositoryService.createProcessDefinitionQuery();
        List<ProcessDefinition> processDefinitionList = processDefinitionQuery.processDefinitionKey("myProcess_1").orderByProcessDefinitionVersion().desc().list();
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
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId("myProcess_1:1:4").singleResult();
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
        historicProcessInstanceQuery.processDefinitionId("myProcess_1:1:4");
        // 设置结果根据开始时间升序排列
        historicProcessInstanceQuery.orderByProcessInstanceStartTime().asc();
        List<HistoricProcessInstance> list = historicProcessInstanceQuery.list();
        for (HistoricProcessInstance historicProcessInstance : list) {
            String businessKey = historicProcessInstance.getBusinessKey();
            String processDefinitionId = historicProcessInstance.getProcessDefinitionId();
            String id = historicProcessInstance.getId();
            log.warn("业务 ID ： {}， 流程定义 ID ： {}， 流程实例 ID ： {}", businessKey,processDefinitionId, id);
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
        historicActivityInstanceQuery.processDefinitionId("myProcess_1:1:4");
        // 设置结果根据开始时间升序排列
        historicActivityInstanceQuery.orderByHistoricActivityInstanceStartTime().asc();
        List<HistoricActivityInstance> list = historicActivityInstanceQuery.list();
        for (HistoricActivityInstance historicActivityInstance : list) {
            String activityId = historicActivityInstance.getActivityId();
            String activityName = historicActivityInstance.getActivityName();
            String assignee = historicActivityInstance.getAssignee();
            String processDefinitionId = historicActivityInstance.getProcessDefinitionId();
            String processInstanceId = historicActivityInstance.getProcessInstanceId();
            log.warn("活动 ID ： {}，活动名称 ： {}，审批人 ： {}，流程定义 ID ： {}， 流程实例 ID ： {}",activityId,activityName,assignee,processDefinitionId,processInstanceId);
        }
        return "success";
    }


    //***************************************** 完成审批流程中各个节点 *****************************************//

    @GetMapping(path = "/completeEvectionApply")
    public String completeEvectionApply() {
        String assignee = "张三";
        TaskService taskService = processEngine.getTaskService();
        Task task = taskService.createTaskQuery().processDefinitionId("myProcess_1:1:4").taskAssignee(assignee).singleResult();
        log.warn("结束的任务名为：{}， 任务 ID 为 {}", task.getName(), task.getId());
        // 拿到任务 ID，然后 complete 去完成该任务。完成后，该节点结束。
        taskService.complete(task.getId());
        return "success";
    }

    @GetMapping(path = "/completeManagerApply")
    public String completeManagerApply() {
        String assignee = "李四";
        TaskService taskService = processEngine.getTaskService();
        Task task = taskService.createTaskQuery().processDefinitionKey("myProcess_1").taskAssignee(assignee).singleResult();
        log.warn("结束的任务名为：{}， 任务 ID 为 {}", task.getName(), task.getId());
        taskService.complete(task.getId());
        return "success";
    }

    @GetMapping(path = "/completeMasterApply")
    public String completeMasterApply() {
        String assignee = "王五";
        TaskService taskService = processEngine.getTaskService();
        Task task = taskService.createTaskQuery().processDefinitionId("myProcess_1:1:4").taskAssignee(assignee).singleResult();
        log.warn("结束的任务名为：{}， 任务 ID 为 {}", task.getName(), task.getId());
        taskService.complete(task.getId());
        return "success";
    }

    @GetMapping(path = "/completeFinanceApply")
    public String completeFinanceApply() {
        String assignee = "赵六";
        TaskService taskService = processEngine.getTaskService();
        Task task = taskService.createTaskQuery().processDefinitionKey("myProcess_1").taskAssignee(assignee).singleResult();
        log.warn("结束的任务名为：{}， 任务 ID 为 {}", task.getName(), task.getId());
        taskService.complete(task.getId());
        return "success";
    }


    //***************************************** 获取流程定义相关资源 *****************************************//

    @GetMapping(path = "/getProcessBpmn")
    public ResponseEntity<byte[]> getProcessBpmn() throws IOException {
        HttpHeaders httpHeaders = new HttpHeaders();
        // 设置返回二进制流,不知道返回什么类型或者浏览器不支持的类型就返回二进制流就完事儿了
        httpHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        RepositoryService repositoryService = processEngine.getRepositoryService();
        // 创建一个资源查询
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                // 设置要获取哪个流程定义的资源
                .processDefinitionKey("myProcess_1").singleResult();
        // 通过查出来的流程定义，获取部署 id
        String deploymentId = processDefinition.getDeploymentId();
        String bpmnFileName = processDefinition.getResourceName();
        httpHeaders.add("Content-Disposition", "attachment;fileName=" + new String(bpmnFileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));
        // 获取 bpmn 文件的流
        InputStream bpmnResourceAsStream = repositoryService.getResourceAsStream(deploymentId, bpmnFileName);
        // 获取文件大小
        int available = bpmnResourceAsStream.available();
        log.warn("size : {}", available);
        // 创建对应大小的 byte 数组
        byte[] bytes = new byte[available];
        // 一次性读倒 byte 数组中
        int read = bpmnResourceAsStream.read(bytes);
        log.warn("read size : {}", read);
        return new ResponseEntity<>(bytes, httpHeaders, HttpStatus.OK);
    }


    @GetMapping(path = "/getProcessBpmnDiagram")
    public ResponseEntity<byte[]> getProcessBpmnDiagram() throws IOException {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.IMAGE_PNG);
        // 创建资源 service 类
        RepositoryService repositoryService = processEngine.getRepositoryService();
        // 根据流程定义 id 获取流程定义
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId("myProcess_1:1:4").singleResult();
        // 获取部署 ID
        String deploymentId = processDefinition.getDeploymentId();
        // 获取 bpmn 对应的 png 图片名称
        String diagramResourceName = processDefinition.getDiagramResourceName();
        httpHeaders.add("Content-Disposition", "attachment;fileName=" + new String(diagramResourceName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1));
        // 通过 repositoryService 来获取流文件
        InputStream diagramResourceAsStream = repositoryService.getResourceAsStream(deploymentId, diagramResourceName);

        int available = diagramResourceAsStream.available();
        byte[] bytes = new byte[available];
        diagramResourceAsStream.read(bytes);

        return new ResponseEntity<>(bytes, httpHeaders, HttpStatus.OK);
    }

}
