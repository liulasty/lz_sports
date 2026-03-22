package com.lz;

import com.lz.dto.UserRegisterDTO;
import com.lz.entity.User;
import com.lz.mapper.UserMapper;
import com.lz.service.UserService;
import com.lz.util.JwtUtil;
import com.lz.util.RedisUtil;
import com.lz.config.AppConfig;
import com.lz.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers
public class UserRegistrationTest {

    @Container
    public static GenericContainer<?> redis = new GenericContainer<>(DockerImageName.parse("redis:6-alpine"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
        // Use H2 memory database for test
        registry.add("spring.datasource.url", () -> "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;MODE=MySQL");
        registry.add("spring.datasource.driver-class-name", () -> "org.h2.Driver");
        registry.add("spring.datasource.username", () -> "sa");
        registry.add("spring.datasource.password", () -> "");
    }

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private AppConfig appConfig;

    @BeforeEach
    void setUp() {
        userMapper.delete(null); // Clear users
        redisUtil.set("test_init", "1"); // Just to ensure redis is connected
    }

    @Test
    void testSendVerifyCode() {
        String email = "test1@qq.com";
        String ip = "127.0.0.1";
        
        String verifyToken = userService.sendVerifyCode(email, ip);
        assertNotNull(verifyToken);
        
        // Rate limiting test
        userService.sendVerifyCode("test2@qq.com", ip);
        userService.sendVerifyCode("test3@qq.com", ip);
        
        BusinessException ex = assertThrows(BusinessException.class, () -> {
            userService.sendVerifyCode("test4@qq.com", ip);
        });
        assertEquals(429, ex.getCode());
    }

    @Test
    void testVerifyCodeSuccess() {
        String verifyToken = UUID.randomUUID().toString().replace("-", "");
        String code = "123456";
        String email = "test2@qq.com";
        redisUtil.set("verify_token:" + verifyToken, code + ":" + email, 5, TimeUnit.MINUTES);

        String registerToken = userService.verifyCode(verifyToken, code);
        assertNotNull(registerToken);

        // Check one-time use
        assertNull(redisUtil.get("verify_token:" + verifyToken));
    }

    @Test
    void testVerifyCodeError() {
        String verifyToken = UUID.randomUUID().toString().replace("-", "");
        String code = "123456";
        String email = "test2@qq.com";
        redisUtil.set("verify_token:" + verifyToken, code + ":" + email, 5, TimeUnit.MINUTES);

        BusinessException ex = assertThrows(BusinessException.class, () -> {
            userService.verifyCode(verifyToken, "654321");
        });
        assertEquals("验证码错误", ex.getMessage());
    }

    @Test
    void testRegisterSuccessAndIdempotency() {
        String verifyToken = UUID.randomUUID().toString().replace("-", "");
        String email = "test3@qq.com";
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("verifyToken", verifyToken);
        claims.put("email", email);
        String registerToken = JwtUtil.genToken(claims, appConfig.getJwtKey(), 10 * 60 * 1000L);

        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setUsername("testuser3");
        dto.setPassword("Password123");
        dto.setEmail(email);

        // First registration should succeed
        userService.register(dto, registerToken);

        User user = userMapper.selectOne(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<User>().eq("email", email));
        assertNotNull(user);
        assertEquals(verifyToken, user.getVerifyToken());

        // Idempotency check: Token in blacklist
        BusinessException ex = assertThrows(BusinessException.class, () -> {
            userService.register(dto, registerToken);
        });
        assertEquals("该注册令牌已失效，请重新验证", ex.getMessage());
    }

    @Test
    void testRegisterForgedToken() {
        String forgedToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.e30.forged_signature";
        
        UserRegisterDTO dto = new UserRegisterDTO();
        dto.setUsername("testuser4");
        dto.setPassword("Password123");
        dto.setEmail("test4@qq.com");

        BusinessException ex = assertThrows(BusinessException.class, () -> {
            userService.register(dto, forgedToken);
        });
        assertEquals("无效的注册令牌", ex.getMessage());
    }
}
