package com.bot.yiyi.mapper;

import com.bot.yiyi.Pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper {

    Integer selectGroupUserIsHave(@Param("userId") Long userId, @Param("groupId") Long groupId);

    void addGroupUser(@Param("userId") Long userId, @Param("groupId") Long groupId);

    User selectUser(Long userId);

    void insertUser(Long userId);

    Integer selectGroupIsHave(Long groupId);

    void addGroup(Long groupId);
}
