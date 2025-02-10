package com.bot.yiyi.mapper;

import com.bot.yiyi.Pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Mapper
@Repository
public interface MoneyMapper {

    void updateMoney(@Param("userId") Long userId, @Param("money") int money);

    void checkIn(@Param("userId") Long userId, @Param("money") int money, @Param("today") LocalDate today);

    void updateLotteryTime(@Param("userId") Long userId, @Param("lotteryTime") String s);

    List<User> selectMAX();

    List<User> selectMIN();

    List<User> selectGroupMAX(Long groupId);

    List<User> selectGroupMIN(Long groupId);

    void deductionMoney(@Param("userId") Long userId, @Param("money") int i);

    void addMoney(@Param("userId") Long userId, @Param("money") int i);

    void storage(@Param("userId") Long userId, @Param("money") int i);

    void withdrawal(@Param("userId") Long userId, @Param("money") int i);
}
