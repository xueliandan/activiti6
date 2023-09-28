/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.boot_activiti6.controller;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.activiti.editor.constants.ModelDataJsonConstants;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Model;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Tijs Rademakers
 */
@RestController
public class ModelSaveRestResource implements ModelDataJsonConstants {

    protected static final Logger LOGGER = LoggerFactory.getLogger(ModelSaveRestResource.class);

    @Resource
    private RepositoryService repositoryService;

    @Resource
    private ObjectMapper objectMapper;

    @PutMapping(value = "/model/{modelId}/save")
    @ResponseStatus(value = HttpStatus.OK)
    public void saveModel(@PathVariable String modelId, HttpServletRequest request) {
        try {

            Model model = repositoryService.getModel(modelId);
            String name = request.getParameter("name");
            String description = request.getParameter("description");
            String jsonXml = request.getParameter("json_xml");
            String svgXml = request.getParameter("svg_xml");

            String modelKey = model.getKey();
            //json_xml process_id
            if (StringUtils.isNotBlank(jsonXml)) {
                ObjectNode editorJsonNode = (ObjectNode) objectMapper.readTree(jsonXml);
                ObjectNode propertiesJsonNode = (ObjectNode) editorJsonNode.get("properties");
                if (Objects.nonNull(propertiesJsonNode)) {
                    String processKey = propertiesJsonNode.get("process_id").asText();
                    String processName = propertiesJsonNode.get("name").asText();
                    if (StringUtils.isBlank(processName)) {
                        propertiesJsonNode.put("name", name);
                    }
                    if (StringUtils.isNotBlank(processKey) && StringUtils.isBlank(modelKey)) {
                        modelKey = processKey;
                    }
                }
            }
            //如果模型没有设置key 就将流程名称设置key
            if (StringUtils.isBlank(modelKey)) {
                modelKey = name;
            }
            model.setKey(modelKey);

            ObjectNode modelJson = (ObjectNode) objectMapper.readTree(model.getMetaInfo());
            modelJson.put(MODEL_NAME, name);
            modelJson.put(MODEL_DESCRIPTION, description);
            model.setMetaInfo(modelJson.toString());
            model.setName(name);
            repositoryService.saveModel(model);
            repositoryService.addModelEditorSource(model.getId(), jsonXml.getBytes(StandardCharsets.UTF_8));
            InputStream svgStream = new ByteArrayInputStream(svgXml.getBytes(StandardCharsets.UTF_8));
            TranscoderInput input = new TranscoderInput(svgStream);

            PNGTranscoder transcoder = new PNGTranscoder();
            // Setup output
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            TranscoderOutput output = new TranscoderOutput(outStream);

            // Do the transformation
            transcoder.transcode(input, output);
            final byte[] result = outStream.toByteArray();
            repositoryService.addModelEditorSourceExtra(model.getId(), result);
            outStream.close();
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("Error saving model", e);
            throw new ActivitiException("Error saving model", e);
        }
    }
}
