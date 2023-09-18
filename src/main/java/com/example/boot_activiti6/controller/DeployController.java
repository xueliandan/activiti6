package com.example.boot_activiti6.controller;

import lombok.extern.slf4j.Slf4j;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
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
}
