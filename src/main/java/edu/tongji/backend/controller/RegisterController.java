package edu.tongji.backend.controller;

import edu.tongji.backend.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import edu.tongji.backend.dto.RegisterDTO;
import edu.tongji.backend.util.Response;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController  //用于处理 HTTP 请求并返回 JSON 格式的数据
@RequestMapping("/api/register")  //对应的api路径
public class RegisterController {
    @Autowired  //自动装填接口的实现类
    IUserService userService;

    @PostMapping("/patient")  //对应的api路径
    public Response<Boolean> registerPatient(@RequestBody RegisterDTO info)
    {
//        System.out.println(info);
        if (info.getContact() == null || info.getPassword() == null)  //如果请求中的内容不完整
        {

            return Response.fail("手机号或密码为空");  //返回错误信息
        }
        //The password must contain at least one digit, one lowercase, one uppercase and one special character,the length should be between 8 and 16.
        if (!info.getPassword().matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]).{8,16}$")) {
            return Response.fail("After 2024/1/7,register rules are updated!" +
                    "The password must contain at least one digit, one lowercase, one uppercase and one special character,the length should be between 8 and 16");
        }
        //The phone number must be 11 digits and it's the valid phone number in China mainland.
        if (!info.getContact().matches("^[1][3,4,5,7,8][0-9]{9}$")) {
            return Response.fail("After 2024/1/7,register rules are updated!"+
                    "The phone number must be 11 digits and it's the valid phone number in China mainland.");
        }
        //The name must be 2-10 characters and it can only contain either all Chinese characters or all English characters.
        if (!info.getName().matches("^[\\u4e00-\\u9fa5]{2,15}$") && !info.getName().matches("^[a-zA-Z]{2,50}$")) {
            return Response.fail("After 2024/1/7,register rules are updated!"+
                    "The name must be 2-10 characters and it can only contain either all Chinese characters or all English characters.");
        }
        System.out.println(info);

        Integer result = userService.register(info.getName(), info.getPassword(), info.getContact(), info.getGender(), info.getAge());  //调用接口的register函数
        if (result == -1)  //如果返回的result为false
        {
            return Response.fail("手机号已被注册");  //返回错误信息
        }
        else if (result == 0)
        {
            return Response.fail("注册失败");  //返回错误信息
        }
        return Response.success(true, "注册成功");
    }

    @PostMapping("/doctor")  //对应的api路径
    public Response<Boolean> registerDoctor(@RequestBody RegisterDTO info)
    {
//        System.out.println(info);
        if (info.getContact() == null || info.getPassword() == null)  //如果请求中的内容不完整
        {
            return Response.fail("手机号或密码为空");  //返回错误信息
        }
        Integer result = userService.register(info.getName(), info.getPassword(), info.getContact());  //调用接口的register函数
        if (result == -1)  //如果返回的result为false
        {
            return Response.fail("手机号已被注册");  //返回错误信息
        }
        else if (result == 0)
        {
            return Response.fail("注册失败");  //返回错误信息
        }
        return Response.success(true, "注册成功");
    }
}
