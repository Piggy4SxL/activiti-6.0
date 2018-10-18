package com.oc.activiti.core;

import com.google.common.collect.Maps;
import lombok.extern.log4j.Log4j2;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.*;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.CollectionUtils;

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
public class TaskServiceTest {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Test
    public void testTaskService() {
        repositoryService.createDeployment().name("test")
                .addClasspathResource("process/my-process-task.bpmn20.xml").deploy();

        Map<String, Object> variables = Maps.newHashMap();
        variables.put("message", "my test message !!!");

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("my-process", variables);

        Task task = taskService.createTaskQuery().singleResult();

        log.info("task = {}", ToStringBuilder.reflectionToString(task, ToStringStyle.JSON_STYLE));
        log.info("task.document = {}", task.getDescription());

        taskService.setVariable(task.getId(), "key", "value");
        taskService.setVariableLocal(task.getId(), "localKey", "localValue");

        Map<String, Object> taskServiceVariables = taskService.getVariables(task.getId());
        Map<String, Object> taskServiceVariablesLocal = taskService.getVariablesLocal(task.getId());
        Map<String, Object> processVariables = runtimeService.getVariables(task.getExecutionId());

        log.info("taskServiceVariables = {}", taskServiceVariables);
        log.info("taskServiceVariablesLocal = {}", taskServiceVariablesLocal);
        log.info("processVariables = {}", processVariables);

        Map<String, Object> completeVar = Maps.newConcurrentMap();
        completeVar.put("cKey", "cValue");
        taskService.complete(task.getId(), completeVar);

        Task task1 = taskService.createTaskQuery()
                .taskId(task.getId()).singleResult();

        log.info("task = {}", task1);
    }

    @Test
    public void testTaskServiceUser() {
        repositoryService.createDeployment().name("test")
                .addClasspathResource("process/my-process-task.bpmn20.xml").deploy();

        Map<String, Object> variables = Maps.newHashMap();
        variables.put("message", "my test message !!!");

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("my-process", variables);

        Task task = taskService.createTaskQuery().singleResult();

        log.info("task = {}", ToStringBuilder.reflectionToString(task, ToStringStyle.JSON_STYLE));
        log.info("task.document = {}", task.getDescription());

        taskService.setOwner(task.getId(), "sxl");

        List<Task> UnassignedTaskList = taskService.createTaskQuery()
                .taskCandidateUser("sxl")
                .taskUnassigned()
                .listPage(0, 100);

        for (Task unassignedTask : UnassignedTaskList) {
            try {
                taskService.claim(unassignedTask.getId(), "sxl");
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }

        List<IdentityLink> identityLinksForTask = taskService.getIdentityLinksForTask(task.getId());

        for (IdentityLink identityLink : identityLinksForTask) {
            log.info("identityLink = {}", identityLink);
        }

        List<Task> taskList = taskService.createTaskQuery()
                .taskAssignee("sxl")
                .listPage(0, 100);

        for (Task task1 : taskList) {
            taskService.complete(task1.getId());
        }

        taskList = taskService.createTaskQuery()
                .taskAssignee("sxl")
                .listPage(0, 100);

        log.info("Task is complete ? {}", CollectionUtils.isEmpty(taskList));
    }

    @Test
    public void testTaskAttachment() {
        repositoryService.createDeployment().name("test")
                .addClasspathResource("process/my-process-task.bpmn20.xml").deploy();

        Map<String, Object> variables = Maps.newHashMap();
        variables.put("message", "my test message !!!");

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("my-process", variables);

        Task task = taskService.createTaskQuery().singleResult();

        taskService.createAttachment("url", task.getId(), processInstance.getProcessInstanceId(), "name",
                "desc", "/test/url");

        List<Attachment> taskAttachments = taskService.getTaskAttachments(task.getId());

        for (Attachment taskAttachment : taskAttachments) {
            log.info("attachment = {}", ToStringBuilder.reflectionToString(taskAttachment, ToStringStyle.JSON_STYLE));
        }
    }

    @Test
    public void testTaskComment() {
        repositoryService.createDeployment().name("test")
                .addClasspathResource("process/my-process-task.bpmn20.xml").deploy();

        Map<String, Object> variables = Maps.newHashMap();
        variables.put("message", "my test message !!!");

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("my-process", variables);

        Task task = taskService.createTaskQuery().singleResult();

        taskService.addComment(task.getId(), processInstance.getId(), "comment1");
        taskService.addComment(task.getId(), processInstance.getId(), "comment2");

        List<Comment> taskComments = taskService.getTaskComments(task.getId());

        for (Comment taskComment : taskComments) {
            log.info("comment = {}", ToStringBuilder.reflectionToString(taskComment, ToStringStyle.JSON_STYLE));
        }

        taskService.setOwner(task.getId(), "sxl");
        taskService.setAssignee(task.getId(), "sxl");
        List<Event> taskEvents = taskService.getTaskEvents(task.getId());

        for (Event taskEvent : taskEvents) {
            log.info("event = {}", ToStringBuilder.reflectionToString(taskEvent, ToStringStyle.JSON_STYLE));
        }
    }
}
