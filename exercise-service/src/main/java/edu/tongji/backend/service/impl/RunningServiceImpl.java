package edu.tongji.backend.service.impl;

/*-
 * #%L
 * Tangxiaozhi
 * %%
 * Copyright (C) 2024 Victor Hu
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */




import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.tongji.backend.entity.Running;
import edu.tongji.backend.mapper.RunningMapper;
import edu.tongji.backend.service.IRunningService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static edu.tongji.backend.util.RedisConstants.*;
@Slf4j
@Service
public class RunningServiceImpl extends ServiceImpl<RunningMapper, Running> implements IRunningService {
    @Autowired
    RunningMapper runningMapper;
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
    public void updateRunning(Integer exercise_id, Double Longitude, Double Latitude) throws Exception {
        List<Point> position = stringRedisTemplate.opsForGeo().position(RUNNING_GEO_KEY+exercise_id, exercise_id.toString());
        if(position.isEmpty()) {
            throw new Exception("time expired!");
        }
        Object odist = stringRedisTemplate.opsForHash().get(CACHE_RUNNING_KEY+exercise_id,"distance");
        //Object opace = stringRedisTemplate.opsForHash().get(CACHE_RUNNING_KEY+exercise_id.toString(),"pace");
        Double origin_distance=Double.valueOf(odist.toString());
        Double delta=GetDistance(Longitude,Latitude,position.get(0).getX(),
                position.get(0).getY());
        stringRedisTemplate.opsForGeo().add(RUNNING_GEO_KEY+exercise_id,new Point(Longitude,Latitude),exercise_id.toString());
        //redisTemplate.opsForHash().delete(CACHE_RUNNING_KEY+exercise_id,"distance");
        String newdis = String.valueOf(origin_distance + delta);
        stringRedisTemplate.opsForHash().put(CACHE_RUNNING_KEY+exercise_id,"distance",newdis);
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime parsed = LocalDateTime.parse(stringRedisTemplate.opsForHash().get(CACHE_EXERCISE_KEY + exercise_id, "startTime").toString(),
                formatter);
        Long seconds = Duration.between(parsed, now).getSeconds();
        String pace;
        Double paced=((origin_distance + delta) / seconds*60/1000);
        log.info("配速:"+paced+"经过:"+newdis+"m"+"耗时"+seconds+"秒");
        if(seconds!=0)
            pace = String.valueOf(paced.intValue());
        else
            pace="0";
        stringRedisTemplate.opsForHash().put(CACHE_RUNNING_KEY+exercise_id,"pace",pace);
        stringRedisTemplate.expire(CACHE_RUNNING_KEY+exercise_id,
                CACHE_RUNNING_TTL, TimeUnit.SECONDS);
    }
}
