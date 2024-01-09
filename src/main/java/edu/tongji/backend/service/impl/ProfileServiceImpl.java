package edu.tongji.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.tongji.backend.dto.ProfileDTO;
import edu.tongji.backend.entity.Complication;
import edu.tongji.backend.entity.Profile;
import edu.tongji.backend.mapper.ComplicationMapper;
import edu.tongji.backend.mapper.ProfileMapper;
import edu.tongji.backend.service.IComplicationService;
import edu.tongji.backend.service.IProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
public class ProfileServiceImpl extends ServiceImpl<ProfileMapper, Profile> implements IProfileService {
    @Autowired
    ProfileMapper profileMapper;
    //TODO: shorten the profile

    @Autowired
    ComplicationMapper complicationMapper;

    @Override
    public Profile getByPatientId(String patient_id) {
        return profileMapper.getByPatientIdProfile(Integer.valueOf(patient_id));
    }

    @Override
    public ProfileDTO getCompleteProfile(Integer patientId) throws ParseException {
        ProfileDTO profileDTO = new ProfileDTO();

        Profile profile = profileMapper.getByPatientIdProfile(patientId);
        if (profile.getGender().equals("Male")) {
            profileDTO.setGender("男");
        } else if (profile.getGender().equals("Female")) {
            profileDTO.setGender("女");
        }
        profileDTO.setAge(profile.getAge());
        profileDTO.setWeight(profile.getWeight().toString() + "kg");
        profileDTO.setHeight(profile.getHeight().toString() + "cm");
        if (Objects.equals(profile.getType(), "I")) {
            profileDTO.setDiabetesType("I型糖尿病");
        } else if (Objects.equals(profile.getType(), "II")) {
            profileDTO.setDiabetesType("II型糖尿病");
        } else if (Objects.equals(profile.getType(), "gestational")) {
            profileDTO.setDiabetesType("妊娠期糖尿病");
        } else {
            profileDTO.setDiabetesType("");
        }
        String date = profile.getDiagnosedYear();
        if (date != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date d = sdf.parse(date);
            Calendar c = Calendar.getInstance();
            c.setTime(d);
            int year = c.get(Calendar.YEAR);
            profileDTO.setDiagnosisYear(year);
        } else {
            profileDTO.setDiagnosisYear(null);
        }
        profileDTO.setFamilyHistory(profile.getFamilyHistory() != null ? profile.getFamilyHistory() : "");

        List<String> complications = complicationMapper.getByPatientId(patientId);
        StringBuilder complicationStr = IComplicationService.getComplicationStr(complications);
        if (!complicationStr.isEmpty()) {
            complicationStr.deleteCharAt(complicationStr.length() - 1);
        }
        profileDTO.setComplications(complicationStr.toString());

        return profileDTO;
    }

    @Override
    public boolean updateProfile(Integer patientId, ProfileDTO profileDTO) throws ParseException {
        Profile profile = new Profile();
        profile.setPatientId(patientId);
        if (profileDTO.getGender().equals("男")) {
            profile.setGender("Male");
        } else if (profileDTO.getGender().equals("女")) {
            profile.setGender("Female");
        }
        profile.setAge(profileDTO.getAge());
        //身高体重前端传来的字符串形式为"xxkg"或"xxcm"，需要去掉单位
        profile.setWeight(Integer.valueOf(profileDTO.getWeight().substring(0, profileDTO.getWeight().length() - 2)));
        profile.setHeight(Integer.valueOf(profileDTO.getHeight().substring(0, profileDTO.getHeight().length() - 2)));
        if (Objects.equals(profileDTO.getDiabetesType(), "I型糖尿病")) {
            profile.setType("I");
        } else if (Objects.equals(profileDTO.getDiabetesType(), "II型糖尿病")) {
            profile.setType("II");
        } else if (Objects.equals(profileDTO.getDiabetesType(), "妊娠期糖尿病")) {
            profile.setType("gestational");
        } else {
            profile.setType("");
        }
        if (profileDTO.getDiagnosisYear() != null) {
            profile.setDiagnosedYear(profileDTO.getDiagnosisYear().toString());
        } else {
            profile.setDiagnosedYear(null);
        }
        profile.setFamilyHistory(profileDTO.getFamilyHistory());
//前端传来的并发症字符串可以用任意一个字符分割
        List<String> complications = IComplicationService.parseComplicationStr(profileDTO.getComplications());

        System.out.println(complications);
        //首先在complication表中删除该病人的所有并发症
        QueryWrapper<Complication> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("patient_id", patientId);
        complicationMapper.delete(queryWrapper);

        for (String complication : complications) {
            complicationMapper.insert(patientId, complication);
        }

        Profile dummy = profileMapper.getByPatientIdProfile(patientId);
        if (dummy == null) {

           System.out.println("insert");

            return profileMapper.insert(profile) == 1;
        }

//        System.out.println("update");
//        System.out.println(profile);

        return profileMapper.update(profile);
    }
}
