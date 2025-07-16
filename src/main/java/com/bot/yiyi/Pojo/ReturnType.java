package com.bot.yiyi.Pojo;

import com.mikuac.shiro.core.BotPlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class ReturnType extends BotPlugin {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public Boolean getMatch(int msgId) {
        return redisTemplate.hasKey("AIMsg:" + msgId);
    }

    public int IGNORE_TRUE(int msgId) {
        redisTemplate.opsForValue().set("AIMsg:" + msgId, true);
        return MESSAGE_IGNORE;
    }

    public int IGNORE_FALSE(int msgId) {
        redisTemplate.opsForValue().set("AIMsg:" + msgId, false);
        return MESSAGE_IGNORE;
    }
}

