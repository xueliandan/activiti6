package com.example.boot_activiti6.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Model;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author zxb 2023/9/27 10:40
 */
@Controller
@RequestMapping(path = "/model")
public class ModelCreateRestResource implements ModelDataJsonConstants {

    private static final Logger log = LoggerFactory.getLogger(ModelCreateRestResource.class);

    @Resource
    private RepositoryService repositoryService;

    @GetMapping(path = "/create")
    @Transactional(rollbackFor = Exception.class)
    public void create(HttpServletResponse response,
                       @RequestParam(value = "name") String name,
                       @RequestParam(value = "key") String key,
                       @RequestParam(value = "description") String description) throws IOException {

        // 页面数据初始化
        ObjectMapper objectMapper = new ObjectMapper();
        Model model = repositoryService.newModel();
        // 可以设置一些元数据
        ObjectNode objectNode = objectMapper.createObjectNode();
        objectNode.put("name", name);
        objectNode.put("key", key);
        objectNode.put("description", description);

        model.setCategory("");
        // 部署 ID 部署后再设置
        // model.setDeploymentId()
        model.setKey(key);
        model.setMetaInfo(objectNode.toString());
        model.setName(name);

        // 完善 ModelEditSource
        JsonNode metaInfoNode = objectMapper.readTree(model.getMetaInfo());
        ObjectNode editorNode = objectMapper.createObjectNode();
        editorNode.put("id", "canvas");
        editorNode.put("resourceId", "canvas");
        // 完善页面显示的模板信息
        ObjectNode stencilSetNode = objectMapper.createObjectNode();
        stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
        editorNode.set("stencilset", stencilSetNode);

        // 完善模型的属性信息，这些属性一旦设置，访问 modeler.html 拼上 modelId 时，会回显这些信息
        ObjectNode propertiesNode = objectMapper.createObjectNode();
        // 流程唯一标识
        propertiesNode.put("process_id", model.getKey());
        // 流程名称
        propertiesNode.put("name", model.getName());
        // 流程描述
        propertiesNode.put("documentation", metaInfoNode.get("description").asText());
        editorNode.set("properties", propertiesNode);

        // 保存
        repositoryService.saveModel(model);
        repositoryService.addModelEditorSource(model.getId(), editorNode.toString().getBytes(StandardCharsets.UTF_8));
        response.sendRedirect("/modeler.html?modelId=" + model.getId());
        log.info("创建模型结束，返回模型ID：{}", model.getId());
    }
}
