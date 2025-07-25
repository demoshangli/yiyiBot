package com.bot.yiyi.plugin;

import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 插件通用父类：用于统一状态判断
 */
@Component
public abstract class BasePlugin extends BotPlugin {

    @Autowired
    protected RedisTemplate<String, Object> redisTemplate;

    /**
     * 判断群聊消息是否应该跳过（群禁用 or 用户禁用）
     */
    protected boolean shouldIgnore(GroupMessageEvent event, String pluginName) {
        Long groupId = event.getGroupId();
        return Boolean.TRUE.equals(redisTemplate.hasKey("PluginSwitch:" + groupId + ":" + pluginName));
    }

    /**
     * 判断私聊消息是否应该跳过（仅用户状态）
     */
    protected boolean shouldIgnore(PrivateMessageEvent event) {
        Long userId = event.getUserId();

        Boolean userSwitch = (Boolean) redisTemplate.opsForValue().get("PluginSwitch:User:" + userId);
        return userSwitch != null && !userSwitch;
    }
}
