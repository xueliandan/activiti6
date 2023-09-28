package com.example.boot_activiti6.controller;

import com.example.boot_activiti6.model.constant.ProcessConstant;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.BpmnModel;
import org.activiti.editor.language.json.converter.BpmnJsonConverter;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.Model;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.zip.ZipInputStream;

/**
 * @author zxb 2023/8/30 17:50
 */
@Slf4j
@RestController
@RequestMapping(path = "/deploy")
public class DeployController {

    /**
     * 注入流程引擎
     */
    @Resource
    private ProcessEngine processEngine;


    /**
     * 部署一个流程图,就是将流程图文件和图像保存到数据库中,同时创建该流程图定义模板
     */
    @GetMapping(path = "/processDeploy")
    public String processDefine() {
        // 通过 processEngine 获取仓库
        RepositoryService repositoryService = processEngine.getRepositoryService();
        // 创建一个部署对象
        Deployment deployment = repositoryService.createDeployment()
                // bpmn 文件，此种方式一次就只能加载一个文件，一定要是 .bpmn 结尾，不然 act_re_prodef 表中不会有记录产生
                .addClasspathResource("processes/evection.bpmn")
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
        assert inputStream != null;
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


    /**
     * 根据模型进行部署。需要将模型转换成 Bpmn 对象，然后将 editorJson 转成流，去部署。
     * 部署的时候一定要切记，资源名称必须是 .bpmn20.xml 结尾
     */
    @GetMapping(path = "/deployFromModel")
    public String deployFromModel(@RequestParam(value = "modelId") String modelId) throws IOException {
        RepositoryService repositoryService = processEngine.getRepositoryService();
        Model model = repositoryService.getModel(modelId);
        String processName = model.getName();
        // 一定一定一定要将部署的资源名称，也就是部署的资源名，改成 .bpmn20.xml 后缀结尾的
        // 不然部署后，有部署记录，却没有流程定义记录
        if (!StringUtils.endsWith(processName, ProcessConstant.BPMN_SUFFIX)) {
            processName += ProcessConstant.BPMN_SUFFIX;
        }
        // 获取 editorSource 的字节数组转成 BpmnModel
        byte[] modelEditorSourceBytes = repositoryService.getModelEditorSource(model.getId());
        JsonNode editor = new ObjectMapper().readTree(modelEditorSourceBytes);
        BpmnJsonConverter bpmnJsonConverter = new BpmnJsonConverter();
        BpmnModel bpmnModel = bpmnJsonConverter.convertToBpmnModel(editor);
        // 再将 bpmnModel 转成 xml 字节数组
        BpmnXMLConverter bpmnXmlConverter = new BpmnXMLConverter();
        byte[] bpmnBytes = bpmnXmlConverter.convertToXML(bpmnModel);
        ByteArrayInputStream in = new ByteArrayInputStream(bpmnBytes);
        // 可以通过字节进行部署，也可以通过输入流进行部署
        Deployment deployment = repositoryService.createDeployment()
                .name(model.getName())
                .key(model.getKey())
//                .addBytes(model.getName(), bpmnBytes)
                .disableSchemaValidation()
                .addInputStream(processName, in)
                .deploy();
        String deploymentId = deployment.getId();
        log.info("ModelId:{} 部署成功, deploymentId:{}", modelId, deploymentId);

        // 设置模型最新的部署 ID
        model.setDeploymentId(deploymentId);
        // 保存模型
        repositoryService.saveModel(model);
        return "success";
    }
}
