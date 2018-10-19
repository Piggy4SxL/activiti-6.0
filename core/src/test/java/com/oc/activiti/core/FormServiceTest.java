package com.oc.activiti.core;

import com.google.common.collect.Maps;
import lombok.extern.log4j.Log4j2;
import org.activiti.engine.FormService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.form.FormProperty;
import org.activiti.engine.form.StartFormData;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;
import java.util.Map;

/**
 * @author SxL
 * Created on 10/16/2018 1:16 PM.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Log4j2
@ContextConfiguration(locations = "classpath:activiti-context.xml")
public class FormServiceTest {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private FormService formService;

    @Autowired
    private TaskService taskService;

    @Test
    public void testFormService() {
        repositoryService.createDeployment()
                .addClasspathResource("process/my-process-form.bpmn20.xml")
                .deploy();

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                .singleResult();

        String startFormKey = formService.getStartFormKey(processDefinition.getId());
        log.info("startFormKey = {}", startFormKey);

        StartFormData startFormData = formService.getStartFormData(processDefinition.getId());
        List<FormProperty> startFormDataFormProperties = startFormData.getFormProperties();

        for (FormProperty startFormDataFormProperty : startFormDataFormProperties) {
            log.info("startFormDataFormProperty = {}", ToStringBuilder.reflectionToString(startFormDataFormProperty, ToStringStyle.JSON_STYLE));
        }

        Map<String, String> startFormProperties = Maps.newHashMap();
        startFormProperties.put("message", "my test message");
        ProcessInstance processInstance = formService.submitStartFormData(processDefinition.getId(), startFormProperties);

        Task task = taskService.createTaskQuery()
                .singleResult();

        TaskFormData taskFormData = formService.getTaskFormData(task.getId());
        List<FormProperty> taskFormDataFormProperties = taskFormData.getFormProperties();

        for (FormProperty taskFormDataFormProperty : taskFormDataFormProperties) {
            log.info("taskFormDataFormProperty = {}", ToStringBuilder.reflectionToString(taskFormDataFormProperty, ToStringStyle.JSON_STYLE));
        }

        Map<String, String> taskFormProperties = Maps.newHashMap();
        taskFormProperties.put("yesOrNo", "yes");
        formService.submitTaskFormData(task.getId(), taskFormProperties);

        Task task1 = taskService.createTaskQuery()
                .taskId(task.getId())
                .singleResult();

        log.info("task = {}", task1);

    }

}
