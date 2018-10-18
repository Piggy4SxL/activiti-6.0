package com.oc.activiti.core;

import com.google.common.collect.Maps;
import lombok.extern.log4j.Log4j2;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.Execution;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceBuilder;
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
public class RuntimeServiceTest {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Test
    public void testStartProcess() {
        repositoryService.createDeployment()
                .name("test").addClasspathResource("process/my-process.bpmn20.xml")
                .deploy();

        Map<String, Object> variables = Maps.newHashMap();
        variables.put("key", "value");

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("my-process", variables);

        log.info("process instance = {}", processInstance);
    }

    @Test
    public void testStartProcessById() {
        repositoryService.createDeployment()
                .name("test").addClasspathResource("process/my-process.bpmn20.xml")
                .deploy();

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

        Map<String, Object> variables = Maps.newHashMap();
        variables.put("key", "value");

        ProcessInstance processInstance = runtimeService.startProcessInstanceById(processDefinition.getId(), variables);

        log.info("process instance = {}", processInstance);
    }

    @Test
    public void testProcessInstanceBuilder() {
        repositoryService.createDeployment()
                .name("test").addClasspathResource("process/my-process.bpmn20.xml")
                .deploy();

        Map<String, Object> variables = Maps.newHashMap();
        variables.put("key", "value");

        ProcessInstanceBuilder processInstanceBuilder = runtimeService.createProcessInstanceBuilder();
        ProcessInstance processInstance = processInstanceBuilder.businessKey("businessKey")
                .variables(variables)
                .processDefinitionKey("my-process")
                .start();

        log.info("process instance = {}", processInstance);
    }

    @Test
    public void testVariables() {
        repositoryService.createDeployment()
                .name("test").addClasspathResource("process/my-process.bpmn20.xml")
                .deploy();

        Map<String, Object> variables = Maps.newHashMap();
        variables.put("key", "value");
        variables.put("key1", "value1");

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("my-process", variables);

        log.info("process instance = {}", processInstance);

        runtimeService.setVariable(processInstance.getId(), "key2", "value2");
        runtimeService.setVariable(processInstance.getId(), "key", "value2");

        Map<String, Object> variables1 = runtimeService.getVariables(processInstance.getId());
        log.info("variables = {}", variables1);
    }

    @Test
    public void testProcessInstanceQuery() {
        repositoryService.createDeployment()
                .name("test").addClasspathResource("process/my-process.bpmn20.xml")
                .deploy();

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("my-process");

        log.info("process instance = {}", processInstance);

        ProcessInstance processInstance1 = runtimeService.createProcessInstanceQuery()
                .processDefinitionId(processInstance.getId())
                .singleResult();
    }

    @Test
    public void testExecutionQuery() {
        repositoryService.createDeployment()
                .name("test").addClasspathResource("process/my-process.bpmn20.xml")
                .deploy();

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("my-process");

        log.info("process instance = {}", processInstance);

        List<Execution> executionList = runtimeService.createExecutionQuery()
                .listPage(0, 100);

        for (Execution execution : executionList) {
            log.info("execution = {}", execution);
        }
    }
}
