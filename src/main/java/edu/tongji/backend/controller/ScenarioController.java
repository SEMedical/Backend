package edu.tongji.backend.controller;

import edu.tongji.backend.dto.Sport;
import edu.tongji.backend.util.Jwt;
import edu.tongji.backend.service.IScenarioService;
import edu.tongji.backend.util.Response;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController//用于处理 HTTP 请求并返回 JSON 格式的数据
@RequestMapping("/api/sports")//对应的api路径
public class ScenarioController {
    @Autowired
    IScenarioService scenarioService;

    @PostMapping("/set-scenario") //对应的api路径
    public Response<String> setScenario(HttpServletRequest request, @RequestParam String category, @RequestParam Integer minute) {
        if (request.getHeader("Authorization") == null) {
            return Response.fail("您尚未登录");
        }
        String token = request.getHeader( "Authorization");
//        System.out.println(token);
        Integer userId = (Integer) Jwt.parse(token).get("userId");
        String role = (String) Jwt.parse(token).get("userPermission");
        if (!role.equals("patient")) {
            return Response.fail("用户不是病人");
        }

        if (scenarioService.setScenario(userId, new Sport(category.toLowerCase(), minute))) {
            return Response.success("设置成功", "success");
        } else {
            return Response.fail("设置失败");
        }
    }
}
