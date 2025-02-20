package com.bot.yiyi.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AIMapper {
    void updateRole(@Param("groupId") Long id, @Param("role") int role);

    int selectRole(Long groupId);

    void updateUserRole(@Param("userId") Long id, @Param("role") int role);

    int selectUserRole(Long userId);
}
