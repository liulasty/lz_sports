package com.lz.service.impl;

import com.lz.common.enums.EventStatus;
import com.lz.common.enums.GenderLimit;
import com.lz.common.enums.ProjectCategory;
import com.lz.common.enums.RegistrationStatus;
import com.lz.common.enums.UserRole;
import com.lz.common.enums.UserStatus;
import com.lz.dto.RegistrationDTO;
import com.lz.entity.Event;
import com.lz.entity.Project;
import com.lz.entity.Registration;
import com.lz.entity.User;
import com.lz.mapper.EventMapper;
import com.lz.mapper.ProjectMapper;
import com.lz.mapper.RegistrationMapper;
import com.lz.mapper.UserMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

@SpringBootTest
@ActiveProfiles("test")
class RegistrationMapperIntegrationTest {

    @Autowired
    private RegistrationMapper registrationMapper;

    @Autowired
    private EventMapper eventMapper;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private UserMapper userMapper;

    private final List<Long> registrationIds = new ArrayList<>();
    private final List<Long> projectIds = new ArrayList<>();
    private final List<Long> eventIds = new ArrayList<>();
    private final List<Long> userIds = new ArrayList<>();

    @AfterEach
    void tearDown() {
        for (Long registrationId : registrationIds) {
            registrationMapper.deleteById(registrationId);
        }
        for (Long projectId : projectIds) {
            projectMapper.deleteById(projectId);
        }
        for (Long eventId : eventIds) {
            eventMapper.deleteById(eventId);
        }
        for (Long userId : userIds) {
            userMapper.deleteById(userId);
        }
    }

    @Test
    void selectScoreEntryCandidatesShouldOnlyReturnApprovedAndConfirmedRegistrations() {
        Event event = buildEvent();
        eventMapper.insert(event);
        eventIds.add(event.getId());

        Project project = buildProject(event.getId());
        projectMapper.insert(project);
        projectIds.add(project.getId());

        Registration approved = createRegistration(project, RegistrationStatus.APPROVED, "approved");
        Registration confirmed = createRegistration(project, RegistrationStatus.CONFIRMED, "confirmed");
        createRegistration(project, RegistrationStatus.PENDING, "pending");
        createRegistration(project, RegistrationStatus.REJECTED, "rejected");

        List<RegistrationDTO> candidates = registrationMapper.selectScoreEntryCandidates(event.getId(), project.getId());

        assertEquals(2, candidates.size());
        assertIterableEquals(
                List.of(approved.getId(), confirmed.getId()),
                candidates.stream().map(RegistrationDTO::getRegistrationId).toList()
        );
        assertIterableEquals(
                List.of("APPROVED", "CONFIRMED"),
                candidates.stream().map(RegistrationDTO::getRegistrationStatus).toList()
        );
    }

    private Registration createRegistration(Project project, RegistrationStatus status, String suffix) {
        User user = buildUser(suffix);
        userMapper.insert(user);
        userIds.add(user.getId());

        Registration registration = new Registration();
        registration.setAthleteId(user.getId());
        registration.setEventId(project.getEventId());
        registration.setItemId(project.getId());
        registration.setSchoolId(1L);
        registration.setRegistrationTime(new Date());
        registration.setRegistrationStatus(status);
        registration.initTime();
        registrationMapper.insert(registration);
        registrationIds.add(registration.getId());
        return registration;
    }

    private Event buildEvent() {
        LocalDateTime now = LocalDateTime.now().minusDays(1);
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        Event event = new Event();
        event.setEventName("score-filter-" + suffix);
        event.setEventDescription("integration-test");
        event.setImageUrls("https://example.com/test.png");
        event.setRegistrationStartTime(toDate(now));
        event.setRegistrationEndTime(toDate(now.plusDays(1)));
        event.setEventStartTime(toDate(now.plusDays(2)));
        event.setEventEndTime(toDate(now.plusDays(3)));
        event.setEventStatus(EventStatus.OPEN);
        event.setMaxItemsPerAthlete(3);
        event.setSchoolId(1L);
        event.initTime();
        return event;
    }

    private Project buildProject(Long eventId) {
        LocalDateTime now = LocalDateTime.now().plusDays(2);
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        Project project = new Project();
        project.setEventId(eventId);
        project.setItemName("100米-" + suffix);
        project.setCategory(ProjectCategory.CUSTOM);
        project.setLimitation(GenderLimit.ALL);
        project.setMaxAttendance(20);
        project.setAttendance(0);
        project.setStartTime(toDate(now));
        project.setEndTime(toDate(now.plusHours(1)));
        project.initTime();
        return project;
    }

    private User buildUser(String suffix) {
        String token = UUID.randomUUID().toString().replace("-", "");
        User user = new User();
        user.setUsername("score_candidate_" + suffix + "_" + token.substring(0, 8));
        user.setPassword("test-password");
        user.setName("Candidate " + suffix);
        user.setGender("男");
        user.setEmail("score_candidate_" + suffix + "_" + token.substring(0, 8) + "@example.com");
        user.setUserType(UserRole.ATHLETE);
        user.setIsFirstLogin(false);
        user.setUnreadCount(0);
        user.setStatus(UserStatus.ACTIVE);
        user.setSchoolId(1L);
        user.initTime();
        return user;
    }

    private Date toDate(LocalDateTime value) {
        return Date.from(value.atZone(ZoneId.systemDefault()).toInstant());
    }
}
