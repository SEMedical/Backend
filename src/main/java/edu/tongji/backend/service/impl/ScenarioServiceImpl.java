package edu.tongji.backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import edu.tongji.backend.dto.Sport;
import edu.tongji.backend.entity.Profile;
import edu.tongji.backend.entity.Scenario;
import edu.tongji.backend.mapper.ScenarioMapper;
import edu.tongji.backend.service.IProfileService;
import edu.tongji.backend.service.IScenarioService;
import edu.tongji.backend.util.CalorieCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ScenarioServiceImpl extends ServiceImpl<ScenarioMapper, Scenario> implements IScenarioService {
    @Autowired
    ScenarioMapper scenarioMapper;

    @Autowired
    IProfileService profileService;

    @Override
    public boolean setScenario(Integer patientId, Sport sport) {
        Scenario scenario = new Scenario();
        scenario.setPatientId(patientId);
        scenario.setCategory(sport.getCategory());
        scenario.setDuration(sport.getDuration());
        int weight=70;

        Profile profile = profileService.getByPatientId(patientId.toString());
        if (profile != null && profile.getWeight() != null&&profile.getWeight()>0) {
            weight = profile.getWeight();
            System.out.println("weight: " + weight);
        }

        Integer calories = CalorieCalculator.getCalorie(sport.getCategory(), sport.getDuration(), weight);

        scenario.setCalories(calories);

        if (scenarioMapper.getByPatientId(patientId) != null) {
            return scenarioMapper.update(scenario);
        } else {
            return scenarioMapper.insert(scenario) == 1;
        }
    }
}
