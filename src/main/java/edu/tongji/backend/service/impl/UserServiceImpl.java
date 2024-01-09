package edu.tongji.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.tongji.backend.entity.Profile;
import edu.tongji.backend.mapper.ProfileMapper;
import edu.tongji.backend.util.Jwt;
import edu.tongji.backend.dto.LoginDTO;
import edu.tongji.backend.entity.User;
import edu.tongji.backend.mapper.UserMapper;
import edu.tongji.backend.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {
    @Autowired
    UserMapper userMapper;
    @Autowired
    ProfileMapper profileMapper;
    @Override
    public LoginDTO login(String contact, String password){
//        System.out.println(userMapper);
        LoginDTO loginDTO = new LoginDTO();
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.select("user_id", "role", "name")
                .eq("contact", contact)
                .eq("password", password);
        var result = userMapper.selectOne(wrapper);
//        System.out.println("user: " + result);

        if(result == null){
            return null;
        }

        Map<String,Object> jwtInfo = new HashMap<>();
        jwtInfo.put("userId", result.getUserId());
        jwtInfo.put("userPermission", result.getRole());
        String jwt = Jwt.generate(jwtInfo);

        loginDTO.setToken(jwt);
        loginDTO.setRole(result.getRole());
        loginDTO.setName(result.getName());

//        System.out.println("loginDTO: " + loginDTO);

        return loginDTO;
    }

    // 病人注册
    @Override
    @Transactional
    public Integer register(String name, String password, String contact, String gender, Integer age){
            QueryWrapper<User> wrapper = new QueryWrapper<>();
            wrapper.select("user_id")
                    .eq("contact", contact);
            var result = userMapper.selectOne(wrapper);
            if (result != null) {
                return -1;  // 手机号已被注册
            }

            User user = new User();
            user.setName(name);
            user.setContact(contact);
            user.setPassword(password);
            user.setRole("patient");
            int userNum = userMapper.insert(user);
            result = userMapper.selectOne(wrapper);
            Profile profile = new Profile();
            profile.setPatientId(result.getUserId());
            System.out.println("The UID:"+result.getUserId());
            profile.setGender(gender);
            profile.setAge(age);
            int profileNum = profileMapper.insert(profile);
            System.out.println("userNum: " + userNum + ", profileNum: " + profileNum);
            if(userNum==1&&profileNum==0)
                throw new RuntimeException("Register failed,rollback!");
            return userNum == 1 && profileNum == 1 ? 1 : 0;

    }

    // 医生注册
    @Override
    public Integer register(String name, String password, String contact){
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.select("user_id")
                .eq("contact", contact);
        var result = userMapper.selectOne(wrapper);
        if(result != null){
            return -1;  // 手机号已被注册
        }

        User user = new User();
        user.setName(name);
        user.setContact(contact);
        user.setPassword(password);
        user.setRole("doctor");
        int userNum = userMapper.insert(user);

        return userNum == 1 ? 1 : 0;
    }
    @Override
    public Integer getUserId(String contact)
    {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        wrapper.eq("contact", contact);
        User user = userMapper.selectOne(wrapper);
        return user.getUserId();
    }
}
