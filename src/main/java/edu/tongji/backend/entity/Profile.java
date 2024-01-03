package edu.tongji.backend.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Profile {
    @TableId
    private int patientId;

    private String gender;

    private String type;

    private Integer age;

    private String familyHistory;

    private String diagnosedYear;
    private String anamnesis;

    private String medicationPattern;

    private String allergy;

    private String medicationHistory;
    private Boolean dietaryTherapy;
    private Boolean exerciseTherapy;
    private Boolean oralTherapy;
    private Boolean insulinTherapy;
}
