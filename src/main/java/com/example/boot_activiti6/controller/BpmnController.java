package com.example.boot_activiti6.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
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
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
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

    /**
     * 根据流程图模板创建一个流程实例并启动：创建一个出差审批流程
     */
    @PostMapping(path = "/start")
    public String startProcessInstance(@RequestBody String variables) {
        // 流程定义的key,通过这个key来启动流程实例,这个 key 是 act_re_procdef 表中的 KEY_ 字段
        String processId = "EvectionProcess:7:60009";
        // 获取 RuntimeService
        RuntimeService runtimeService = processEngine.getRuntimeService();
        // 使用流程定义的 key 【启动流程实例】，key对应 evection.bpmn 文件中id的属性值，使用key值启动，默认是按照最新版本的流程定义启动
        Map<String, Object> applyVariablesMap = JSON.parseObject(variables, new TypeReference<Map<String, Object>>() {
        });
        ProcessInstance processInstance = runtimeService
                // startProcessInstanceByKey方法还可以设置其他的参数，比如流程变量。
                .startProcessInstanceById(processId, applyVariablesMap);
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
