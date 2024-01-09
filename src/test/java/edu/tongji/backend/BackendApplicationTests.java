package edu.tongji.backend;

import edu.tongji.backend.controller.GlycemiaController;
import edu.tongji.backend.controller.LoginController;
import edu.tongji.backend.controller.RegisterController;
import edu.tongji.backend.dto.RegisterDTO;
import edu.tongji.backend.exception.GlycemiaException;
import edu.tongji.backend.mapper.ExerciseMapper;
import edu.tongji.backend.mapper.GlycemiaMapper;
import edu.tongji.backend.mapper.ProfileMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import edu.tongji.backend.entity.User;
import edu.tongji.backend.mapper.UserMapper;
import edu.tongji.backend.service.impl.ExerciseServiceImpl;
import edu.tongji.backend.service.impl.GlycemiaServiceImpl;
import edu.tongji.backend.service.impl.UserServiceImpl;
import edu.tongji.backend.util.Jwt;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.LocalDate;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class BackendApplicationTests {
    @Autowired
    UserMapper userMapper;
    @Autowired
    ProfileMapper profileMapper;
    @Autowired
    GlycemiaMapper glycemiaMapper;
    @Autowired
    GlycemiaController glycemiaController;
    @Autowired
    ExerciseServiceImpl exerciseService;
    @Autowired
    UserServiceImpl userService;
    @Autowired
    ExerciseMapper exerciseMapper;
    @Autowired
    GlycemiaServiceImpl glycemiaService;
    @Autowired
    RegisterController register;
    //一个用于测试实时获取运动数据的测试用例
    @Test
    void testExercise2() throws InterruptedException {
        System.out.println("Start test");
        exerciseService.addExercise("1");
        for (int i = 0; i < 5; i++) {
            Thread.sleep(1000);
            exerciseService.getRealTimeSport("1");
        }
        exerciseService.finishExercise("1");
        System.out.println("End test");
    }
    @Test
    void contextLoads() {
        glycemiaService.showGlycemiaHistoryDiagram("Week", "2", LocalDate.of(2023, 12, 27));
    }
    @Test
    void testTx1(){
        userService.register("Alice","femmves","12345678912","Female",21);
    }
    @Test
    void register(){
        register.registerPatient(new RegisterDTO("Bob","123456Aa,","16055555554","Male",21));
    }
    @Test
    void getLatestGlycemia(){
        //assertThrows(GlycemiaException.class, () -> {
        //    glycemiaService.getLatestGlycemia("1");
        //});
    }
    @Test
    void testSelect(){
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.select("user_id", "role")
                .eq("user_id", 1)
                .eq("password", "your_password2");
        System.out.println(userMapper.selectOne(wrapper));
    }
    @Test
    void testSelectGlycemia(){
        System.out.println("Start test");

        System.out.println("End test");
//        assertThrows(GlycemiaException.class, () -> {
//            glycemiaController.LookupChart("key","History", "2", "2023-12-27");
//
//        });
    }
    @Test
    void testSelectGlycemiaRecord(){
        System.out.println("Start test");
//        assertThrows(GlycemiaException.class, () -> {
//            //glycemiaController.LookupChartRecord("Week", "2", "2023-12-27");
//        });
        System.out.println("End test");

    }
    @Test
    void testExerciseTx() throws InterruptedException {
        System.out.println("Start test");
        exerciseService.addExercise("1");
        //Sleep
        try {
            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        exerciseService.finishExercise("1");
        System.out.println("End test");
    }
    @Test
    void testExerciseInsertion(){
        //assert that the time is now
        Integer exercise_id = exerciseService.addExercise("1");
        //if(exerciseMapper.selectById(exercise_id).getCategory().equalsIgnoreCase("walking"))
        if(exerciseMapper.selectById(exercise_id).getCategory().equalsIgnoreCase("yoga"))
            return;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String time = exerciseService.getRealTimeSport("1").getTime();
        Pattern pattern = Pattern.compile("\\d+"); // 匹配一个或多个数字
        Matcher matcher = pattern.matcher(time);

        if (matcher.find()) {
            // 找到匹配的数字部分
            String numericPart = matcher.group();

            // 将提取的数字部分转换为整数
            int numericValue = Integer.parseInt(numericPart);

            // 断言时间是否小于10分钟
            assertTrue(numericValue < 10, "The time is not now");
        } else {
            // 没有找到匹配的数字部分，可能需要进行错误处理
            System.err.println("No numeric part found in the time string");
        }
    }
    @Test
    void testDailyDiagram(){
        System.out.println(glycemiaService.showDailyGlycemiaDiagram("1", LocalDate.of(2024, 1, 2)));
    }
}
