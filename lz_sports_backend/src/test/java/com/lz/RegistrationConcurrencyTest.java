package com.lz;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lz.entity.Event;
import com.lz.entity.Project;
import com.lz.entity.Registration;
import com.lz.entity.User;
import com.lz.mapper.EventMapper;
import com.lz.mapper.ProjectMapper;
import com.lz.mapper.RegistrationMapper;
import com.lz.mapper.UserMapper;
import com.lz.service.RegistrationService;
import com.lz.service.UserService;
import com.lz.common.context.BaseContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class RegistrationConcurrencyTest {

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private EventMapper eventMapper;

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private RegistrationMapper registrationMapper;

    private Long eventId;
    private Long projectId;
    private final int MAX_ATTENDANCE = 10;
    private final int THREAD_COUNT = 50;

    @BeforeEach
    void setUp() {
        // Clear data
        registrationMapper.delete(null);
        projectMapper.delete(null);
        eventMapper.delete(null);
        // userMapper.delete(null); // Be careful with user table if it has init data

        // 1. Create Event
        Event event = new Event();
        event.setEventName("Concurrency Test Event");
        event.setStatus(com.lz.common.enums.EventStatus.PUBLISHED);
        event.setRegistrationStart(new Date());
        event.setRegistrationDeadline(new Date(System.currentTimeMillis() + 86400000)); // +1 day
        event.setSchoolId(1L);
        eventMapper.insert(event);
        this.eventId = event.getEventId();

        // 2. Create Project with limited slots
        Project project = new Project();
        project.setEventId(eventId);
        project.setItemName("Limited Slots Race");
        project.setMaxAttendance(MAX_ATTENDANCE);
        project.setAttendance(0);
        project.setLimitation("无限制");
        project.setSchoolId(1L);
        projectMapper.insert(project);
        this.projectId = project.getItemId();
        
        // 3. Ensure enough users exist (mock users)
        // In real test, we might need to mock BaseContext or login
        // For this test, we need to mock BaseContext.getCurrentId() in each thread
    }

    @Test
    void testConcurrentRegistration() throws InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < THREAD_COUNT; i++) {
            final long userId = 1000L + i; // Simulate different user IDs
            executorService.submit(() -> {
                try {
                    // Mock Thread Local Context
                    BaseContext.setCurrentId(userId);
                    
                    // We need to ensure Athlete exists for this user in the service logic
                    // Since we mocked AthleteMapper/User logic in previous steps or need to insert real data:
                    // Ideally we should insert Users/Athletes here.
                    // For simplicity, let's assume we can mock the check or insert them.
                    // Inserting simplified user for test:
                    /*
                    User u = new User();
                    u.setUserId(userId);
                    u.setUserName("user" + userId);
                    u.setUserType(com.lz.common.enums.UserRole.ATHLETE);
                    userMapper.insert(u); // This might fail if ID strategy is AUTO. 
                    // Better to rely on service mocking or proper data setup.
                    */
                    
                    registrationService.add(projectId);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    // System.out.println("Registration failed: " + e.getMessage());
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown();
                    BaseContext.removeCurrentId();
                }
            });
        }

        latch.await(30, TimeUnit.SECONDS);
        executorService.shutdown();

        // Verify
        Project updatedProject = projectMapper.selectById(projectId);
        System.out.println("Success: " + successCount.get());
        System.out.println("Fail: " + failCount.get());
        System.out.println("Project Attendance: " + updatedProject.getAttendance());

        assertEquals(MAX_ATTENDANCE, updatedProject.getAttendance(), "Attendance should not exceed max");
        assertEquals(MAX_ATTENDANCE, successCount.get(), "Success count should match max slots");
    }
}
