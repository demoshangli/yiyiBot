package com.bot.yiyi.mapper;

import com.bot.yiyi.Pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

@Mapper
@Repository
public interface MoneyMapper {

    User selectUser(Long userId);

    void insertUser(Long userId);

    void updateMoney(@Param("userId") Long userId, @Param("money") int money);

    void checkIn(@Param("userId") Long userId, @Param("money") int money, @Param("today") LocalDate today);
}
