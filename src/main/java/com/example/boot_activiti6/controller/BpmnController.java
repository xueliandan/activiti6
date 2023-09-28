package com.example.boot_activiti6.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.example.boot_activiti6.model.constant.ProcessConstant;
import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.*;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * @author zxb 2023/1/10 16:53
 */
@RestController
@Slf4j
public class BpmnController {

    /**
     * 注入流程引擎
     */
    @Resource
    private ProcessEngine processEngine;

    @Resource
    private RepositoryService repositoryService;

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
                .processDefinitionKey(ProcessConstant.PROCESS_DEF_KEY).singleResult();
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
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(ProcessConstant.PROCESS_DEF_KEY).singleResult();
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
