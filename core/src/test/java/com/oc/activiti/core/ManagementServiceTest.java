package com.oc.activiti.core;

import com.oc.activiti.core.mapper.MyCustomMapper;
import lombok.extern.log4j.Log4j2;
import org.activiti.engine.ManagementService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.impl.cmd.AbstractCustomSqlExecution;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.management.TablePage;
import org.activiti.engine.runtime.DeadLetterJobQuery;
import org.activiti.engine.runtime.Job;
import org.activiti.engine.runtime.JobQuery;
import org.activiti.engine.runtime.SuspendedJobQuery;
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
public class ManagementServiceTest {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private ManagementService managementService;

    @Test
    public void testJobQuery() {
        repositoryService.createDeployment().name("test")
                .addClasspathResource("process/my-process-job.bpmn20.xml")
                .deploy();

        List<Job> timerJobList = managementService.createTimerJobQuery()
                .listPage(0, 100);

        for (Job timerJob : timerJobList) {
            log.info("timerJob = {}", timerJob);
        }

        JobQuery jobQuery = managementService.createJobQuery();

        SuspendedJobQuery suspendedJobQuery = managementService.createSuspendedJobQuery();

        DeadLetterJobQuery deadLetterJobQuery = managementService.createDeadLetterJobQuery();
    }

    @Test
    public void testTableQuery() {
        repositoryService.createDeployment().name("test")
                .addClasspathResource("process/my-process-job.bpmn20.xml")
                .deploy();

        TablePage tablePage = managementService.createTablePageQuery()
                .tableName(managementService.getTableName(ProcessDefinitionEntity.class))
                .listPage(0, 100);

        List<Map<String, Object>> rows = tablePage.getRows();

        for (Map<String, Object> row : rows) {
            log.info("row = {}", row);
        }
    }

    @Test
    public void testCustomSql() {
        repositoryService.createDeployment().name("test")
                .addClasspathResource("process/my-process.bpmn20.xml")
                .deploy();

        runtimeService.startProcessInstanceByKey("my-process");

        List<Map<String, Object>> mapList = managementService.executeCustomSql(new AbstractCustomSqlExecution<MyCustomMapper, List<Map<String, Object>>>(MyCustomMapper.class) {
            @Override
            public List<Map<String, Object>> execute(MyCustomMapper o) {
                return o.findAll();
            }
        });

        for (Map<String, Object> map : mapList) {
            log.info("map = {}", map);
        }
    }

    @Test
    public void testCommand() {
        repositoryService.createDeployment().name("test")
                .addClasspathResource("process/my-process.bpmn20.xml")
                .deploy();

        runtimeService.startProcessInstanceByKey("my-process");

        ProcessDefinitionEntity processDefinitionEntity = managementService.executeCommand(
                commandContext -> commandContext.getProcessDefinitionEntityManager()
                        .findLatestProcessDefinitionByKey("my-process"));

        log.info("processDefinitionEntity = {}", processDefinitionEntity);
    }
}
