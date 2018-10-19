package com.oc.activiti.core;

import com.google.common.collect.Maps;
import lombok.extern.log4j.Log4j2;
import org.activiti.engine.*;
import org.activiti.engine.history.*;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.runtime.ProcessInstanceBuilder;
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
public class HistoryServiceTest {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private FormService formService;

    @Test
    public void testHistoryService() {
        repositoryService.createDeployment().name("test")
                .addClasspathResource("process/my-process.bpmn20.xml")
                .deploy();

        ProcessInstanceBuilder processInstanceBuilder = runtimeService.createProcessInstanceBuilder();

        Map<String, Object> variables = Maps.newHashMap();
        variables.put("key0", "value0");
        variables.put("key1", "value1");
        variables.put("key2", "value2");


        Map<String, Object> transientVariables = Maps.newHashMap();
        transientVariables.put("key", "value");

        ProcessInstance processInstance = processInstanceBuilder.processDefinitionKey("my-process")
                .variables(variables)
                .transientVariables(transientVariables)
                .start();

        runtimeService.setVariable(processInstance.getId(), "key1", "key1_1");

        Task task = taskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .singleResult();

        Map<String, String> properties = Maps.newHashMap();
        properties.put("fKey1", "fValue1");
        properties.put("key2", "value2_2");

        formService.submitTaskFormData(task.getId(), properties);

        List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery()
                .listPage(0, 100);

        for (HistoricProcessInstance historicProcessInstance : historicProcessInstances) {
            log.info("historicProcessInstance = {}", toString(historicProcessInstance));
        }

        List<HistoricActivityInstance> historicActivityInstances = historyService.createHistoricActivityInstanceQuery()
                .listPage(0, 100);

        for (HistoricActivityInstance historicActivityInstance : historicActivityInstances) {
            log.info("historicActivityInstance = {}", toString(historicActivityInstance));
        }

        List<HistoricTaskInstance> historicTaskInstances = historyService.createHistoricTaskInstanceQuery()
                .listPage(0, 100);

        for (HistoricTaskInstance historicTaskInstance : historicTaskInstances) {
            log.info("historicTaskInstance = {}", toString(historicTaskInstance));
        }

        List<HistoricVariableInstance> historicVariableInstances = historyService.createHistoricVariableInstanceQuery()
                .listPage(0, 100);

        for (HistoricVariableInstance historicVariableInstance : historicVariableInstances) {
            log.info("historicVariableInstance = {}", toString(historicVariableInstance));
        }

        List<HistoricDetail> historicDetails = historyService.createHistoricDetailQuery()
                .listPage(0, 100);

        for (HistoricDetail historicDetail : historicDetails) {
            log.info("historicDetail = {}", toString(historicDetail));
        }

        ProcessInstanceHistoryLog processInstanceHistoryLog = historyService.createProcessInstanceHistoryLogQuery(processInstance.getId())
                .includeActivities()
                .includeComments()
                .includeFormProperties()
                .includeTasks()
                .includeVariables()
                .includeVariableUpdates()
                .singleResult();

        List<HistoricData> historicData = processInstanceHistoryLog.getHistoricData();

        for (HistoricData historicDatum : historicData) {
            log.info("historicData = {}", toString(historicDatum));
        }

        historyService.deleteHistoricProcessInstance(processInstance.getId());

        HistoricProcessInstance historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstance.getId())
                .singleResult();

        log.info("historicProcessInstance = {}", toString(historicProcessInstance));
    }

    private String toString(Object o) {
        return o != null ? ToStringBuilder.reflectionToString(o, ToStringStyle.JSON_STYLE) : null;
    }
}
