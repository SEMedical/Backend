package edu.tongji.backend.controller;

import edu.tongji.backend.dto.RealTimeSportDTO;
import edu.tongji.backend.dto.SportDetailedDTO;
import edu.tongji.backend.dto.SportPlanDTO;
import edu.tongji.backend.dto.SportRecordDTO;
import edu.tongji.backend.exception.GlycemiaException;
import edu.tongji.backend.service.IExerciseService;
import edu.tongji.backend.service.IProfileService;
import edu.tongji.backend.service.IRunningService;
import edu.tongji.backend.service.IUserService;
//import edu.tongji.backend.util.GlobalEventChecker;
import edu.tongji.backend.util.Jwt;
import edu.tongji.backend.util.Response;
import edu.tongji.backend.util.TimeTypeChecker;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.ibatis.jdbc.Null;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController//用于处理 HTTP 请求并返回 JSON 格式的数据
@RequestMapping("/api/sports")//对应的api路径
public class SportController {
    @Autowired
    IExerciseService exerciseService;
    @Autowired
    IUserService userService;
    @Autowired
    IProfileService profileService;
    @Autowired
    IRunningService runningService;
    @GetMapping("/startDoingSport") //对应的api路径
    public Response<Null> startExercise(HttpServletRequest request)
    {
        String token = request.getHeader( "Authorization");
        String user_id= Jwt.parse(token).get("userId").toString();
        //确认用户是否存在，是否是病人
        try {
            this.checkUser(user_id);
            Integer ans= exerciseService.addExercise(user_id);
            if(ans !=null&&ans!=0)
                return Response.success(null,"开始运动");
            else
                return Response.fail("数据库更新失败");
        }catch (Exception e){
            System.out.println(e.getMessage());
           return Response.fail(e.getMessage());
        }
    }
    private void checkUser(String user_id)
    {
        if(userService.getById(user_id)==null)
            throw new GlycemiaException("user isn't exist");
        else if(!userService.getById(user_id).getRole().equals("patient"))
            throw new GlycemiaException("user isn't a patient");
        else if(profileService.getByPatientId(user_id)==null)
            throw new GlycemiaException("exception with registration of the user"+user_id);
    }

    @GetMapping("/stopDoingSport") //对应的api路径
    public Response<Null> stopSport(HttpServletRequest request)//把请求中的内容映射到user
    {
        String token = request.getHeader( "Authorization");
        String user_id=Jwt.parse(token).get("userId").toString();
        //确认用户是否存在，是否是病人
        try {
            this.checkUser(user_id);
            Integer ans= exerciseService.finishExercise(user_id);
            if(ans !=null&&ans!=0)
                return Response.success(null,"结束运动");
            else
                return Response.fail("运动方案不存在");
        }catch (Exception e){
            return Response.fail(e.getMessage());
        }
    }
    @GetMapping("/realTimeSportData")
    public Response<RealTimeSportDTO> getRealTimeSportData(HttpServletRequest request)
    {
        String token = request.getHeader("Authorization");
        String user_id = Jwt.parse(token).get("userId").toString();
        //确认用户是否存在，是否是病人
        try {
            this.checkUser(user_id);
            RealTimeSportDTO ans= exerciseService.getRealTimeSport(user_id);
            if(ans !=null)
                return Response.success(ans,"成功获取实时运动数据");
            else
                return Response.fail("实时运动数据不存在");
        }catch (Exception e){
            return Response.fail(e.getMessage());
        }
    }

    @GetMapping("/sportRecord") //对应的api路径
    public Response<SportRecordDTO> getTotalRecord(HttpServletRequest request)//把请求中的内容映射到user
    {
        String token = request.getHeader( "Authorization");
        String user_id=Jwt.parse(token).get("userId").toString();
        //确认用户是否存在，是否是病人
        try {
            this.checkUser(user_id);
            SportRecordDTO ans= exerciseService.getSportRecord(user_id);
            if(ans !=null)
                return Response.success(ans,"成功获取运动记录");
            else
                return Response.fail("运动记录不存在");
        }catch (Exception e){
            System.out.println(e.getMessage());
            return Response.fail(e.getMessage());
        }
    }
    @PostMapping("/detailedSportRecord")
    public Response<SportDetailedDTO> getDetailRecord
            (HttpServletRequest request, @RequestParam String time_type,@RequestParam String category)
    {
        String token = request.getHeader("Authorization");
        String user_id = Jwt.parse(token).get("userId").toString();
        //确认用户是否存在，是否是病人
        try {
            this.checkUser(user_id);
            int check = TimeTypeChecker.check(time_type);
            if (check == 0)
                return Response.fail("time_type is invalid");

            SportDetailedDTO ans= exerciseService.getDetailedSportRecord(user_id,check,category);
            if(ans !=null)
                return Response.success(ans,"成功获取运动记录");
            else
                return Response.fail("运动记录不存在");
        }catch (Exception e){
            System.out.println(e.getMessage());
            return Response.fail(e.getMessage());
        }
    }
    @GetMapping("/realTimeHeartRate")
    public Response<Integer> getRealTimeHeartRate(HttpServletRequest request)
    {
        String token = request.getHeader("Authorization");
        String user_id = Jwt.parse(token).get("userId").toString();
        //确认用户是否存在，是否是病人
        try {
            this.checkUser(user_id);
            Integer ans= exerciseService.getRealTimeHeartRate(user_id);
            if(ans !=null)
                return Response.success(ans,"成功获取实时心率");
            else
                return Response.fail("实时心率不存在");
        }catch (Exception e){
            return Response.fail(e.getMessage());
        }
    }
    @GetMapping("/sportPlan")
    public Response<SportPlanDTO> getSportPlan(HttpServletRequest request)
    {
        String token = request.getHeader("Authorization");
        String user_id = Jwt.parse(token).get("userId").toString();
        //确认用户是否存在，是否是病人
        try {
            this.checkUser(user_id);
            SportPlanDTO ans= exerciseService.getSportPlan(user_id);
            if(ans !=null)
                return Response.success(ans,"成功获取运动方案");
            else
                return Response.fail("运动方案不存在");
        }catch (Exception e){
            System.out.println(e.getMessage());
            return Response.fail(e.getMessage());
        }
    }

}
