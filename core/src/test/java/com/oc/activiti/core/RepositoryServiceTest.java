package com.oc.activiti.core;

import lombok.extern.log4j.Log4j2;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.DeploymentBuilder;
import org.activiti.engine.repository.DeploymentQuery;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.task.IdentityLink;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * @author SxL
 * Created on 10/16/2018 1:16 PM.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@Log4j2
@ContextConfiguration(locations = "classpath:activiti-context.xml")
public class RepositoryServiceTest {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Test
    public void testRepository() {
        DeploymentBuilder deploymentBuilder = repositoryService.createDeployment();
        deploymentBuilder.name("test")
                .addClasspathResource("process/my-process.bpmn20.xml")
                .addClasspathResource("process/second_approve.bpmn20.xml");
        Deployment deploy = deploymentBuilder.deploy();

        log.info("deploy = {}", deploy);

        DeploymentBuilder deploymentBuilder1 = repositoryService.createDeployment();
        deploymentBuilder1.name("test1")
                .addClasspathResource("process/my-process.bpmn20.xml")
                .addClasspathResource("process/second_approve.bpmn20.xml");
        Deployment deploy1 = deploymentBuilder1.deploy();

        DeploymentQuery deploymentQuery = repositoryService.createDeploymentQuery();
//        Deployment deployment = deploymentQuery.deploymentId(deploy.getId()).singleResult();
        List<Deployment> deploymentList = deploymentQuery
                .orderByDeploymenTime().asc()
                .listPage(0, 100);

        log.info("deploymentList.size = {}", deploymentList.size());

        for (Deployment deployment : deploymentList) {
            log.info("deployment = {}", deployment);
        }

//        List<ProcessDefinition> processDefinitionList = repositoryService.createProcessDefinitionQuery()
//                .deploymentId(deployment.getId())
//                .listPage(0, 100);

        List<ProcessDefinition> processDefinitionList = repositoryService.createProcessDefinitionQuery()
                .orderByProcessDefinitionKey().asc()
                .listPage(0, 100);

        for (ProcessDefinition processDefinition : processDefinitionList) {
            log.info("process definition = {}, version = {}, key = {}, id = {}",
                    processDefinition, processDefinition.getVersion(), processDefinition.getKey(), processDefinition.getId());
        }
    }

    @Test
//    @org.activiti.engine.test.Deployment(resources = "process/my-process.bpmn20.xml")
    public void testSuspend() {
        repositoryService.createDeployment()
                .name("test").addClasspathResource("process/my-process.bpmn20.xml")
                .deploy();

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

        log.info("process definition id = {}", processDefinition.getId());

        repositoryService.suspendProcessDefinitionById(processDefinition.getId());

        try {
            log.info("starting...");
            runtimeService.startProcessInstanceById(processDefinition.getId());
            log.info("started.");
        } catch (Exception e) {
            log.info("start failed.");
            log.info(e.getMessage(), e);
        }

        repositoryService.activateProcessDefinitionById(processDefinition.getId());

        log.info("starting...");
        runtimeService.startProcessInstanceById(processDefinition.getId());
        log.info("started.");
    }

    @Test
    public void testCandidate() {
        repositoryService.createDeployment()
                .name("test").addClasspathResource("process/my-process.bpmn20.xml")
                .deploy();

        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();

        log.info("process definition id = {}", processDefinition.getId());

        repositoryService.addCandidateStarterUser(processDefinition.getId(), "user");
        repositoryService.addCandidateStarterGroup(processDefinition.getId(), "group");

        List<IdentityLink> identityLinksForProcessDefinitionList = repositoryService.getIdentityLinksForProcessDefinition(processDefinition.getId());

        for (IdentityLink identityLink : identityLinksForProcessDefinitionList) {
            log.info("identityLink = {}", identityLink);
        }

        repositoryService.deleteCandidateStarterUser(processDefinition.getId(), "user");
        repositoryService.deleteCandidateStarterGroup(processDefinition.getId(), "group");

        identityLinksForProcessDefinitionList = repositoryService.getIdentityLinksForProcessDefinition(processDefinition.getId());

        for (IdentityLink identityLink : identityLinksForProcessDefinitionList) {
            log.info("identityLink = {}", identityLink);
        }
    }
}
