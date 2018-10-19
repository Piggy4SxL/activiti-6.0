package com.oc.activiti.core;

import lombok.extern.log4j.Log4j2;
import org.activiti.engine.IdentityService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
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
public class IdentityServiceTest {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private IdentityService identityService;

    @Test
    public void testIdentityService() {
        User user1 = identityService.newUser("user1");
        user1.setEmail("user1@123.com");
        identityService.saveUser(user1);

        User user3 = identityService.createUserQuery()
                .userId("user1")
                .singleResult();

        user3.setFirstName("xilai");
        identityService.saveUser(user3);

        User user2 = identityService.newUser("user2");
        user2.setEmail("user2@123.com");
        identityService.saveUser(user2);

        Group group1 = identityService.newGroup("group1");
        identityService.saveGroup(group1);

        Group group2 = identityService.newGroup("group2");
        identityService.saveGroup(group2);

        identityService.createMembership("user1", "group1");
        identityService.createMembership("user2", "group2");
        identityService.createMembership("user1", "group2");

        List<User> userList = identityService.createUserQuery()
                .memberOfGroup("group2")
                .listPage(0, 100);

        for (User user : userList) {
            log.info("user = {}", ToStringBuilder.reflectionToString(user, ToStringStyle.JSON_STYLE));
        }

        List<Group> groupList = identityService.createGroupQuery()
                .groupMember("user1")
                .listPage(0, 100);

        for (Group group : groupList) {
            log.info("group = {}", ToStringBuilder.reflectionToString(group, ToStringStyle.JSON_STYLE));
        }
    }
}
