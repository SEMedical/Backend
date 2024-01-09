package edu.tongji.backend.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Running {
    @TableId
    private int exerciseId;
    private int pace;
    private double distance;
}
