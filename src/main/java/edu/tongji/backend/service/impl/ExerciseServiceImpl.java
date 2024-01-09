package edu.tongji.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.tongji.backend.dto.*;
import edu.tongji.backend.entity.*;
import edu.tongji.backend.exception.ExerciseException;
import edu.tongji.backend.mapper.ExamineMapper;
import edu.tongji.backend.mapper.ExerciseMapper;
import edu.tongji.backend.mapper.RunningMapper;
import edu.tongji.backend.mapper.ScenarioMapper;
import edu.tongji.backend.service.IExerciseService;
import edu.tongji.backend.service.IProfileService;
import edu.tongji.backend.util.CalorieCalculator;
import edu.tongji.backend.util.GlobalEventChecker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static java.lang.Thread.sleep;

@Service
public class ExerciseServiceImpl extends ServiceImpl<ExerciseMapper, Exercise> implements IExerciseService {
    @Autowired
    ExerciseMapper exerciseMapper;
    @Autowired
    ScenarioMapper scenarioMapper;
    @Autowired
    ExamineMapper examineMapper;
    @Autowired
    RunningMapper runningMapper;
    @Autowired
    RunningServiceImpl runningService;
    @Autowired
    IProfileService profileService;

    @Override
    public Intervals getExerciseIntervalsInOneDay(String category,String userId, String date) {
        List<ExerciseDTO> lists=exerciseMapper.getExerciseIntervalsInOneDay(category,userId, date);
        List<Map<LocalDateTime,LocalDateTime>> formattedLists=new ArrayList<>();
        Intervals intervals=new Intervals();
        for (ExerciseDTO list : lists) {
            if(list.getStartTime()==null){
                throw new ExerciseException("startTime is null ");
            }else if(list.getDuration()==null){
                throw new ExerciseException("duration is null ");
            }
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime date1 = LocalDateTime.parse(list.getStartTime(), formatter);
            LocalDateTime date2=date1.plusMinutes(list.getDuration());
            System.out.println(date2);
            formattedLists.add(new HashMap<>(Map.of(date1,date2)));
            intervals.setDatas(formattedLists);
        }
        return intervals;
    }

    @Override
    @Transactional
    public Integer addExercise(String userId) {
        finishExercise(userId);
        int user_id = Integer.parseInt(userId);
        Exercise exercise = new Exercise();
        exercise.setPatientId(user_id);
        exercise.setStartTime(LocalDateTime.now());
        System.out.println("开始时间为"+exercise.getStartTime());
        //查找这个用户的运动方案
        QueryWrapper<Scenario> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("patient_id", user_id);
        List<Scenario> scenarios = scenarioMapper.selectList(queryWrapper);
        if (scenarios.isEmpty())
            return null;
        Scenario last_scenario = scenarios.get(scenarios.size() - 1);
        exercise.setCategory(last_scenario.getCategory());
        System.out.println("找到的运动种类为"+exercise.getCategory());
        int insert_exercise = exerciseMapper.insert(exercise);//往exercise表里插入一条记录
        if (insert_exercise == 0)
        {
            System.out.println("插入exercise表失败");
            return null;
        }
        QueryWrapper<Exercise> exerciseQueryWrapper = new QueryWrapper<>();
        exerciseQueryWrapper.eq("patient_id", user_id);
        int exercise_id = exerciseMapper.selectList(exerciseQueryWrapper).get(exerciseMapper.selectList(exerciseQueryWrapper).size() - 1).getExerciseId();
        insert_exercise=exercise_id;
        System.out.println("插入exercise表成功，exercise_id为"+insert_exercise);
        int insert_running=1;
        //如果是跑步，还要往running表里插入一条记录
        if (exercise.getCategory().equalsIgnoreCase("walking")||exercise.getCategory().equalsIgnoreCase("jogging")) {
            Running running = new Running();
            running.setExerciseId(insert_exercise);
            //running表里有：distance,pace
            //他们是随着运动过程中不断变化的
            insert_running=runningMapper.insert(running);
        }
        GlobalEventChecker.getScheduler().schedule(() -> checkStopExercise(exercise_id), 1, TimeUnit.HOURS);
        return insert_exercise*insert_running;
    }
    private void checkStopExercise(int id)  {
        // 在这里添加逻辑，检查是否在60分钟内完成了CompletableFuture
        System.out.println("Checking if Function finishExercise is called within 60 minutes.");

        if (GlobalEventChecker.getBCalledFuture().isDone()) {
            System.out.println("Function finishExercise is called within 60 minutes.");
        } else {
            System.out.println("Remove the record of"+id+"in exercise table and running table");
            runningMapper.deleteById(id);
            exerciseMapper.deleteById(id);
        }
    }
    @Override
    public Integer finishExercise(String userId) {//它应该要结束当前用户的所有运动记录
        GlobalEventChecker.getBCalledFuture().complete(null);
        int user_id = Integer.parseInt(userId);
        QueryWrapper<Exercise> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("patient_id", user_id).eq("duration", 0);
        List<Exercise> exercises = exerciseMapper.selectList(queryWrapper);
        if (exercises.isEmpty())
            return null;
        //获取用户体重数据
        int weight=70;
        Profile profile = profileService.getByPatientId(userId);
        if (profile != null && profile.getWeight() != null&&profile.getWeight()>0) {
            weight = profile.getWeight();
            System.out.println("weight: " + weight);
        }
//转换时区
        ZoneId currentZoneId = ZoneId.systemDefault();
        Integer res=1;
        //遍历每一个exercise
        for (Exercise last_exercise:exercises) {
            ZonedDateTime start_time0 = last_exercise.getStartTime().atZone(ZoneId.of("UTC"));
            LocalDateTime start_time = start_time0.withZoneSameInstant(currentZoneId).toLocalDateTime();
            int duration = (int) Duration.between(start_time, LocalDateTime.now()).toMinutes();
            last_exercise.setDuration(duration);
//获取运动类型
            String category = last_exercise.getCategory();
            //更新卡路里
            int calorie = CalorieCalculator.getCalorie(category.toLowerCase(), weight, duration);
            last_exercise.setCalorie(calorie);
            //更新distance
            if(category.equalsIgnoreCase("walking")||category.equalsIgnoreCase("jogging"))
            {
                Running running=runningService.updateRunning(last_exercise.getExerciseId());
                if(running!=null)
                {
                    last_exercise.setDistance(running.getDistance());
                }
            }
            //创建这个exercise对应的mapper
            QueryWrapper<Exercise> exerciseQueryWrapper = new QueryWrapper<>();
            exerciseQueryWrapper.eq("exercise_id", last_exercise.getExerciseId());
            res*= exerciseMapper.update(last_exercise, exerciseQueryWrapper);
            if (res > 0)
                System.out.println("结束exercise成功，exercise_id为" + last_exercise.getExerciseId());
            else
                break;
        }
        return res;
    }

    @Override
    public SportRecordDTO getSportRecord(String userId) {
        SportRecordDTO ans= new SportRecordDTO();
        System.out.println("数组长度为"+ans.getMinute_record().length);
        int user_id = Integer.parseInt(userId);
        QueryWrapper<Exercise> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("patient_id", user_id).isNotNull("duration");
        //查找最近7天的运动记录，从7天前的0点到今天的现在
        queryWrapper.ge("start_time", LocalDate.now().minusDays(6).atStartOfDay());
        System.out.println("起始日期为"+LocalDate.now().minusDays(6).atStartOfDay());
        List<Exercise> exercises = exerciseMapper.selectList(queryWrapper);
        int total_minute = 0;
        int total_calorie = 0;
        HashMap<String, CategoryRecordDTO> sport_records = new HashMap<>();
        for (Exercise exercise : exercises) {
            total_minute += exercise.getDuration();
            total_calorie += exercise.getCalorie();
            //要知道这个运动是在第几天，然后在对应的位置加上运动时间
            ans.getMinute_record()[LocalDate.now().minusDays(7).until(exercise.getStartTime().toLocalDate()).getDays()-1] += exercise.getDuration();
System.out.println("在第"+(LocalDate.now().minusDays(7).until(exercise.getStartTime().toLocalDate()).getDays()-1)+"天运动了"+exercise.getDuration()+"分钟");
System.out.println("这一天的日期是"+exercise.getStartTime().toLocalDate());
            //要知道这个运动是什么种类，然后在对应的位置加上运动时间
            String category = Scenario.check(exercise.getCategory());
            if(category==null)
                continue;
            if (sport_records.containsKey(category)) {
                CategoryRecordDTO categoryRecordDTO = sport_records.get(category);
                categoryRecordDTO.setMinute(categoryRecordDTO.getMinute() + exercise.getDuration());
                categoryRecordDTO.setCalorie(categoryRecordDTO.getCalorie() + exercise.getCalorie());
                sport_records.put(category, categoryRecordDTO);
            } else {
                CategoryRecordDTO categoryRecordDTO = new CategoryRecordDTO(exercise.getDuration(), exercise.getCalorie());
                sport_records.put(category, categoryRecordDTO);
            }
        }
        ans.setTotal_minute(total_minute);
        ans.setTotal_calorie(total_calorie);
        ans.setSport_records(sport_records);
        return ans;
    }

    @Override
    public SportDetailedDTO getDetailedSportRecord(String userId, int time_type, String category) {
        SportDetailedDTO ans = new SportDetailedDTO();
        int user_id = Integer.parseInt(userId);
        LocalDateTime endTime = LocalDateTime.now();
        LocalDateTime startTime = LocalDate.now().atStartOfDay();
        //根据时间类型来确定查询的时间范围 1是一个月 2是一周 3是一天
        //如果是一天，就不用有minute_record和calorie_record
        switch (time_type) {
            case 1:
                startTime = LocalDate.now().minusDays(29).atStartOfDay();
                ans.setMinute_record(new int[30]);
                ans.setCalorie_record(new int[30]);
                break;
            case 2:
                startTime = LocalDate.now().minusDays(6).atStartOfDay();
                ans.setMinute_record(new int[7]);
                ans.setCalorie_record(new int[7]);
                break;
            default:
                ans.setMinute_record(new int[1]);
                ans.setCalorie_record(new int[1]);
                break;
        }
        System.out.println("起始日期为"+startTime+"终止日期为"+endTime+"运动种类为"+category+"时间类型为"+time_type);
        int i = 0;
        int sum_duration = 0;//总时长，从exercise表获得
        double sum_distance = 0;//总距离，从running表获得,以公里为单位
        int mean_pace = 0;//平均配速，从running表获得 单位是秒
        int running_times=0;//跑步次数，从running表获得
        //令startTime不断加一天，直到等于endTime
        while (startTime.isBefore(endTime)) {
            QueryWrapper<Exercise> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("patient_id", user_id).eq("category", category.toLowerCase());
            //查找这一天指定种类的运动记录
            queryWrapper.ge("start_time", startTime).lt("start_time", startTime.plusDays(1));
            queryWrapper.isNotNull("duration");
            List<Exercise> exercises = exerciseMapper.selectList(queryWrapper);//获得这一条的所有运动记录
            for (Exercise exercise : exercises) {
                ans.getCalorie_record()[i] += exercise.getCalorie();
                ans.getMinute_record()[i] += exercise.getDuration();
                sum_duration += exercise.getDuration();
                int exercise_id = exercise.getExerciseId();
                System.out.println("exercise_id为"+exercise_id);
                if (category.equalsIgnoreCase("walking") || category.equalsIgnoreCase("jogging"))
                {
                    //根据exercise_id查找running表
                    Running running=runningMapper.getByExerciseIdRunning(exercise_id);
                    if (running != null) {
                        System.out.println("获得的distance为"+running.getDistance());
                        sum_distance += running.getDistance();
                        mean_pace += running.getPace();
                        running_times++;
                    }
                }
            }
            startTime = startTime.plusDays(1);
            i++;
        }
        if(running_times!=0)
            mean_pace/=running_times;
        else
            mean_pace=0;

        //把minute_space格式化为xx分xx秒这样的字符串
        //如果不是跑步，就返回空字符串
        String mean_speed="";
        if (category.equalsIgnoreCase("walking") || category.equalsIgnoreCase("jogging"))
            mean_speed = String.format("%d分%d秒",mean_pace/60,mean_pace%60);
        //类似地，处理sum_duration，它的单位是分钟
        String sum_duration_str;
        if(sum_duration>60)
            sum_duration_str = String.format("%d小时%d分",sum_duration/60,sum_duration%60);
        else
            sum_duration_str = String.format("%d分",sum_duration);
        ans.setMean_speed(mean_speed);
        ans.setSum_duration(sum_duration_str);
        ans.setSum_distance(sum_distance);
        return ans;
    }
    @Override
    public Integer getRealTimeHeartRate(String userId) {
        //获取一个在70到100之间的随机数
        Random random = new Random();
        return random.nextInt(30)+70;
    }
    @Override
    public SportPlanDTO getSportPlan(String userId){
        int user_id = Integer.parseInt(userId);
        QueryWrapper<Scenario> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("patient_id", user_id);
        //根据patient_id可以找到唯一的运动方案
        Scenario last_scenario =scenarioMapper.selectOne(queryWrapper);
        String category = last_scenario.getCategory();
        int recommend_time = last_scenario.getDuration();
        int recommend_calorie = last_scenario.getCalories();
        //接下来要计算 当天这个用户这个运动类型的运动总时长和总卡路里
        QueryWrapper<Exercise> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.eq("patient_id", user_id).eq("category", category.toLowerCase());
        queryWrapper1.ge("start_time", LocalDate.now().atStartOfDay()).isNotNull("duration");
        List<Exercise> exercises = exerciseMapper.selectList(queryWrapper1);
        int total_time = 0;
        int total_calorie = 0;
        for(Exercise exercise:exercises)
        {
            total_time+=exercise.getDuration();
            total_calorie+=exercise.getCalorie();
        }
        Boolean is_finished = false;
        if(total_time>=recommend_time)
            is_finished=true;
        return new SportPlanDTO(total_time,total_calorie,category,recommend_time,recommend_calorie,is_finished);
    }
    @Override
    public RealTimeSportDTO getRealTimeSport(String userId){
        int user_id = Integer.parseInt(userId);
        //获取用户体重数据
        int weight=70;

        Profile profile = profileService.getByPatientId(userId);
        if (profile != null && profile.getWeight() != null&&profile.getWeight()>0) {
            weight = profile.getWeight();
            System.out.println("weight: " + weight);
        }
        RealTimeSportDTO ans = new RealTimeSportDTO();
        LocalDateTime now = LocalDateTime.now();
        //获取最近一次运动记录
        QueryWrapper<Exercise> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("patient_id", user_id);
        List<Exercise> exercises = exerciseMapper.selectList(queryWrapper);
        //将exercises按照时间由近到远排序
        if(exercises.isEmpty())
            return null;
        exercises.sort(new Comparator<Exercise>() {
            @Override
            public int compare(Exercise o1, Exercise o2) {
                return (o2.getExerciseId()>o1.getExerciseId())? 1:-1;
            }
        });
        Exercise last_exercise = exercises.get(0);
        System.out.println("最近一次运动的id为"+last_exercise.getExerciseId());
        //统一时区，把start_time转为当前时区
        ZoneId currentZoneId = ZoneId.systemDefault();
        ZonedDateTime start_time0 = last_exercise.getStartTime().atZone(ZoneId.of("UTC") );//默认是用UTC
        LocalDateTime start_time = start_time0.withZoneSameInstant(currentZoneId).toLocalDateTime();//转为SystemDefault
        String category = last_exercise.getCategory().toLowerCase();
        //获取两个时间的差值
        System.out.println("开始时间为"+start_time+"现在时间为"+now);
        int duration = (int) Duration.between(start_time,now).toSeconds();
        //duration是以秒为单位的，需要转为相应的字符串返回给前端
        if(duration<60)
            ans.setTime(String.format("%d秒",duration));
        else if(duration<3600)
            ans.setTime(String.format("%d分%d秒",duration/60,duration%60));
        else
            ans.setTime(String.format("%d小时%d分%d秒",duration/3600,duration%3600/60,duration%60));
        ans.setCategory(last_exercise.getCategory().toLowerCase());
        //获取运动数据
        if(category.equalsIgnoreCase("walking")||category.equalsIgnoreCase("jogging")) {
            Running now_running = runningService.updateRunning(last_exercise.getExerciseId());
            ans.setDistance(now_running.getDistance());
            int pace = now_running.getPace();//得到的是以秒为单位的
            if (pace < 60)
                ans.setSpeed(String.format("%d秒", pace));
            else
                ans.setSpeed(String.format("%d分%d秒", pace / 60, pace % 60));
        }
        //计算卡路里
        int calorie = CalorieCalculator.getCalorie(category, weight, duration);
        ans.setCalorie(calorie);
        return ans;
    }

}
