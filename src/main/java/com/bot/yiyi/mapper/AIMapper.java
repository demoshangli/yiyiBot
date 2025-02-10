package com.bot.yiyi.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AIMapper {
    void updateRole(@Param("groupId") Long roleType, @Param("role") int role);

    int selectRole(Long groupId);
}
