package com.lz;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lz.entity.User;
import com.lz.vo.UserVO;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserVOTest {

    @Test
    public void testUserVODefaultValues() throws Exception {
        User user = new User();
        user.setId(1L);
        // Explicitly leaving name, gender, studentId, gradeId, email, schoolId as null

        UserVO vo = user.toUserVO();

        // Check if default values are correctly assigned
        assertEquals("", vo.getUsername());
        assertEquals("", vo.getName());
        assertEquals("UNKNOWN", vo.getGender());
        assertEquals("", vo.getStudentId());
        assertEquals(0L, vo.getGradeId());
        assertEquals("", vo.getEmail());
        assertEquals(0L, vo.getSchoolId());
        assertEquals("https://lz-sports.oss-cn-beijing.aliyuncs.com/default-avatar.png", vo.getAvatar());

        // Serialize to JSON
        ObjectMapper mapper = new ObjectMapper();
        String json = mapper.writeValueAsString(vo);

        // Assert JSON does not contain null fields
        assertFalse(json.contains("\"name\":null"));
        assertFalse(json.contains("\"gender\":null"));
        assertFalse(json.contains("\"avatar\":null"));
        
        // Assert JSON contains default values
        assertTrue(json.contains("\"name\":\"\""));
        assertTrue(json.contains("\"gender\":\"UNKNOWN\""));
        assertTrue(json.contains("\"avatar\":\"https://lz-sports.oss-cn-beijing.aliyuncs.com/default-avatar.png\""));
    }
}
