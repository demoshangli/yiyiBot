package com.bot.yiyi.utils;

import com.bot.yiyi.config.BotConfig;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class LimitUtil {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private BotConfig botConfig;

    public Boolean isBlack(Long userId) {
        if (botConfig.isOwnerQQ(userId)) {
            return false;
        }
        return redisTemplate.hasKey("black:" + userId);
    }

    public void isLimitMsg(Bot bot, GroupMessageEvent event) {
        if (redisTemplate.hasKey("black:" + event.getUserId())) {
            String msg = MsgUtils.builder().at(event.getUserId()).text("你被拉黑了").build();
            bot.sendGroupMsg(event.getGroupId(), msg, false);
        } else
        if (redisTemplate.hasKey("work:SewingMachine:" + event.getUserId())) {
            String msg = MsgUtils.builder().at(event.getUserId()).text("你还在踩缝纫机呢").build();
            bot.sendGroupMsg(event.getGroupId(), msg, false);
        } else
        if (redisTemplate.hasKey("join:" + event.getUserId())) {
            String msg = MsgUtils.builder().at(event.getUserId()).text("你还在大牢里呢").build();
            bot.sendGroupMsg(event.getGroupId(), msg, false);
        } else {
            String msg = MsgUtils.builder().at(event.getUserId()).text("你状态正常，是一个好群友").build();
            bot.sendGroupMsg(event.getGroupId(), msg, false);
        }
    }

    public Boolean isLimit(Long userId) {
        if (botConfig.isOwnerQQ(userId)) {
            return false;
        }
        if (redisTemplate.hasKey("work:SewingMachine" + userId)) {
            return true;
        }
        if (redisTemplate.hasKey("join:" + userId)) {
            return true;
        }
        return false;
    }
}
