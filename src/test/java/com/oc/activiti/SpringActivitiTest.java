package com.oc.activiti;

import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

/**
 * @author SxL
 * Created on 10/15/2018 2:35 PM.
 */
@ContextConfiguration(locations = {"classpath:activiti-context.xml"})
public class SpringActivitiTest extends ActivitiApplicationTests {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private RepositoryService repositoryService;

    @Test
//    @Deployment(resources = {"my-process-spring.bpmn20.xml"})
    public void test() {
        repositoryService.createDeployment().name("my-process")
                .addClasspathResource("my-process-spring.bpmn20.xml").deploy();
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("my-process");
        assert processInstance != null;
        Task task = taskService.createTaskQuery().singleResult();
        taskService.complete(task.getId());
    }
}
