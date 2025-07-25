package com.bot.yiyi.mapper;

import com.bot.yiyi.Pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {

    Integer selectGroupUserIsHave(@Param("userId") Long userId, @Param("groupId") Long groupId);

    void addGroupUser(@Param("userId") Long userId, @Param("groupId") Long groupId);

    User selectUser(Long userId);

    void insertUser(Long userId);

    Integer selectGroupIsHave(Long groupId);

    void addGroup(Long groupId);

    int selectTime(Long userId);

    List<User> selectAllUser();

    void updateDay(@Param("Check") int checkIn, @Param("lotteryTime") int lotteryTime, @Param("money") int money, @Param("userId") Long userId);

    int selectUserBlack(Long userId);

    int isShutDown(Long groupId);
}
