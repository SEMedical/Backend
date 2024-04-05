package edu.tongji.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.tongji.backend.entity.Running;
import edu.tongji.backend.mapper.RunningMapper;
import edu.tongji.backend.service.IRunningService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static edu.tongji.backend.util.RedisConstants.*;

@Service
public class RunningServiceImpl extends ServiceImpl<RunningMapper, Running> implements IRunningService {
    @Autowired
    RunningMapper runningMapper;
    @Resource
    RedisTemplate redisTemplate;
    @Resource
    StringRedisTemplate stringRedisTemplate;
    private static double EARTH_RADIUS = 6371000;//赤道半径(单位m)

    /**
     * 转化为弧度(rad)
     * */
    private static double rad(double d)
    {
        return d * Math.PI / 180.0;
    }
    public static double GetDistance(double lon1,double lat1,double lon2, double lat2) {
        double radLat1 = rad(lat1);
        double radLat2 = rad(lat2);
        double a = radLat1 - radLat2;
        double b = rad(lon1) - rad(lon2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2) + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * EARTH_RADIUS;
        s = Math.round(s * 10000) / 10000;
        return s;
    }
    @Override
    public void updateRunning(Integer exercise_id, Double Longitude, Double Latitude) {
        List<Point> position = stringRedisTemplate.opsForGeo().position(RUNNING_GEO_KEY, exercise_id.toString());
        Object odist = stringRedisTemplate.opsForHash().get(CACHE_RUNNING_KEY+exercise_id,"distance");
        //Object opace = stringRedisTemplate.opsForHash().get(CACHE_RUNNING_KEY+exercise_id.toString(),"pace");
        Double origin_distance=Double.valueOf(odist.toString());
        Double delta=GetDistance(Longitude,Latitude,position.get(0).getX(),
                position.get(0).getY());
        stringRedisTemplate.opsForGeo().add(RUNNING_GEO_KEY,new Point(Longitude,Latitude),exercise_id.toString());
        //redisTemplate.opsForHash().delete(CACHE_RUNNING_KEY+exercise_id,"distance");
        String newdis = String.valueOf(origin_distance + delta);
        stringRedisTemplate.opsForHash().put(CACHE_RUNNING_KEY+exercise_id,"distance",newdis);
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime parsed = LocalDateTime.parse(stringRedisTemplate.opsForHash().get(CACHE_EXERCISE_KEY + exercise_id, "startTime").toString(),
                formatter);
        Long seconds = Duration.between(parsed, now).toSeconds();
        String pace;
        Double paced=((origin_distance + delta) / seconds*60/1000);
        System.out.println("配速:"+paced+"经过:"+newdis+"m"+"耗时"+seconds+"秒");
        if(seconds!=0)
            pace = String.valueOf(paced.intValue());
        else
            pace="0";
        stringRedisTemplate.opsForHash().put(CACHE_RUNNING_KEY+exercise_id,"pace",pace);
        stringRedisTemplate.expire(CACHE_RUNNING_KEY+exercise_id,
                CACHE_RUNNING_TTL, TimeUnit.SECONDS);
    }
}
