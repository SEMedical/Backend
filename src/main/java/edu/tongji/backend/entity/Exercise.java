package edu.tongji.backend.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.eclipse.persistence.jpa.jpql.parser.DateTime;

import java.sql.Timestamp;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Exercise {
    private Integer patientId;

    private LocalDateTime startTime;

    private int duration;//unit:minute

    private int calorie;

    private String category;
    @TableId
    private int exerciseId;
    private double distance;
}
