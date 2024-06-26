package edu.tongji.backend.service;

/*-
 * #%L
 * Tangxiaozhi
 * %%
 * Copyright (C) 2024 Victor Hu,rmEleven
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





import edu.tongji.backend.dto.DoctorInfoDTO;
import edu.tongji.backend.entity.Doctor;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.security.NoSuchAlgorithmException;
import java.util.List;

public interface IAccountService {
    public List<DoctorInfoDTO> getAccountList();
    public Integer addAccount(Doctor doctor, String contact, String address) throws NoSuchAlgorithmException;
    public void deleteAccount(int doctorId);

    Boolean repeatedIdCard(String idCard);
    Boolean repeatedIdCard(String idCard, StringRedisTemplate stringRedisTemplate);
    Boolean updateAccount(Doctor doctor);
}
