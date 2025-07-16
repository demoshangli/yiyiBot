package com.bot.yiyi.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AIMapper {
    void updateRole(@Param("groupId") Long id, @Param("role") int role);

    int selectRole(Long groupId);

    void updateUserRole(@Param("userId") Long id, @Param("role") int role);

    int selectUserRole(Long userId);

    int selectAiType(Long groupId);

    void updateAiType(@Param("type") int aiType, @Param("groupId") Long groupId);

    int selectPro(Long groupId);

    int selectHumanState(Long groupId);

    void updateHumanState(@Param("state") int state, @Param("groupId") Long groupId);

    void updatePro(@Param("pro") int pro, @Param("groupId") Long groupId);
}
