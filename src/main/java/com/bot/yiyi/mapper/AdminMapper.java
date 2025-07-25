package com.bot.yiyi.mapper;

import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

public interface AdminMapper {

    @Select("select id, role from admin_list")
    List<Map<String, Object>> getAllAdmins();

    @Insert("insert into admin_list (id, role) values (#{id}, #{role})")
    int addAdmin(@Param("id") Long id, @Param("role") String role);

    @Delete("delete from admin_list where id = #{id}")
    int deleteAdmin(Long id);

    @Update("update group_info set bot_state = #{state} where group_id = #{id}")
    void updateBotState(@Param("id") Long groupId, @Param("state") int i);

    @Update("update user set is_black = #{state} where id = #{id}")
    void updateUserState(@Param("id") Long userId, @Param("state") int i);
}
