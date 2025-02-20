package com.bot.yiyi.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.mikuac.shiro.enums.MsgTypeEnum.json;

@Component
public class RedisConversationService {
    private static final int MAX_HISTORY = 10;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    // 存储用户消息
    public void addUserMessage(Long sessionId, String content) {
        addMessage(sessionId, new UserMessage(content));
    }

    // 存储AI回复
    public void addAssistantMessage(Long sessionId, String content) {
        addMessage(sessionId, new AssistantMessage(content));
    }

    private void addMessage(Long sessionId, Message message) {
        String key = "conversation:" + sessionId;

        // 将消息对象序列化为 JSON
        String jsonMessage = JSON.toJSONString(message);

        // 从右侧插入新消息
        redisTemplate.opsForList().rightPush(key, jsonMessage);
        // 保持最多10条记录
        redisTemplate.opsForList().trim(key, -MAX_HISTORY, -1);
    }

    private String getKey(Long sessionId) {
        return "conversation:" + sessionId;
    }

    // 获取格式化历史记录
    public String getFormattedHistory(Long sessionId) {
        String key = getKey(sessionId);
        List<String> messages = redisTemplate.opsForList().range(key, 0, -1);

        StringBuilder history = new StringBuilder();
        if (messages != null) {
            for (String Json : messages) {
                Message msg = JSON.parseObject(Json, Message.class);
                if (msg.getMessageType().equals(MessageType.USER)) {
                    history.append("我: ").append(msg.getContent()).append("\n");
                } else if (msg.getMessageType().equals(MessageType.ASSISTANT)) {
                    history.append("你: ").append(msg.getContent()).append("\n");
                }
            }
        }
        return history.toString();
    }

    public void clearHistory(Long sessionId) {
        String key = getKey(sessionId);
        redisTemplate.delete(key);
    }

}