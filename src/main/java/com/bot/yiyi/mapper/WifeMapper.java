package com.bot.yiyi.mapper;

import com.bot.yiyi.Pojo.Wife;
import org.apache.ibatis.annotations.Param;

public interface WifeMapper {
    void marry(@Param("userId") Long userId, @Param("wife") Long wifeUserId);

    String isWife(Long userId);

    String isHusband(Long userId);

    Wife selectWife(Long userId);

    Wife selectHusband(Long userId);

    String SelectConfession(int i);

    void deleteWife(long l);
}
