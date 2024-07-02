package edu.tongji.backend.mapper;

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




import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import edu.tongji.backend.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    @Select("select * from user where name= #{name};")
    User select1(String name);//自定义函数，通过SQL语句进行查询。本函数仅做示例，可能没有用gi
    User selectById(int id);
    @Update("UPDATE user SET password=#{new_password} WHERE user_id=#{user_id} and password =#{old_password}")
    int changePassword(@Param("userId") int user_id, @Param("new_password") String new_password, @Param("old_password") String old_password);
    @Select("select name from user where user_id= #{id};")
    String getUserName(int id);
}
