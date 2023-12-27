package edu.tongji.backend;

import edu.tongji.backend.mapper.ProfileMapper;
import edu.tongji.backend.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BackendApplicationTests {
    @Autowired
    UserMapper userMapper;
    @Autowired
    ProfileMapper profileMapper;
    @Test
    void contextLoads() {
    }
    @Test
    void testSelect(){
        System.out.println(userMapper.selectById(1));
        System.out.println(profileMapper.selectById(1));
    }
}
