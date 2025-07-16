package com.bot.yiyi.mapper;

import com.bot.yiyi.Pojo.User;
import com.bot.yiyi.Pojo.Wife;
import org.apache.ibatis.annotations.Param;

public interface WifeMapper {
    void marry(@Param("userId") Long userId, @Param("wife") Long wifeUserId);

    Long isWife(Long userId);

    Long isHusband(Long userId);

    Wife selectWife(Long userId);

    Wife selectHusband(Long userId);

    String SelectConfession(int i);

    void deleteWife(long l);

    Wife selectInfo(Long userId);

    void updateWifeResponsive(@Param("userId") Long userId, @Param("responsive") int responsive);

    void updateHusbandResponsive(@Param("userId") Long userId, @Param("responsive") int responsive);

    void setHusbandResponsive(@Param("userId") Long userId, @Param("responsive") int responsive);

    void setWifeResponsive(@Param("userId") Long userId, @Param("responsive") int responsive);
}
