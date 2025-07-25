package com.bot.yiyi.plugin;

import com.bot.yiyi.Pojo.ReturnType;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class SoundPlugin extends BasePlugin {

    private final String PLUGIN_NAME = "GamePlugin";

    @Autowired
    private ReturnType returnType;

    private final Random random = new Random();

    // 关键词对应的语音文件列表，支持随机选一个
    private static final Map<String, List<String>> VOICE_MAP = new LinkedHashMap<>();

    static {
        VOICE_MAP.put("曼波", Arrays.asList(
                "https://wuyao.love/manbo.m4a",
                "https://wuyao.love/manbo1.m4a"
        ));
        VOICE_MAP.put("阿米诺斯", Arrays.asList(
                "https://wuyao.love/amns1.m4a",
                "https://wuyao.love/amns.m4a"
        ));
        VOICE_MAP.put("私人笑声", Collections.singletonList("https://wuyao.love/hehehe.m4a"));
        VOICE_MAP.put("wow", Collections.singletonList("https://wuyao.love/wow.m4a"));
        VOICE_MAP.put("哦耶", Collections.singletonList("https://wuyao.love/oy.m4a"));
        VOICE_MAP.put("duang", Collections.singletonList("https://wuyao.love/Duang.m4a"));
        VOICE_MAP.put("Duang", Collections.singletonList("https://wuyao.love/Duang.m4a"));
    }

    @Override
    public int onAnyMessage(Bot bot, AnyMessageEvent event) {

        if (shouldIgnore(event, PLUGIN_NAME)) return MESSAGE_IGNORE;

        String msg = event.getMessage();
        if (msg == null || msg.isEmpty()) {
            return MESSAGE_IGNORE;
        }

        for (Map.Entry<String, List<String>> entry : VOICE_MAP.entrySet()) {
            if (msg.contains(entry.getKey())) {
                List<String> voices = entry.getValue();
                String voiceUrl = voices.size() == 1 ? voices.get(0) : voices.get(random.nextInt(voices.size()));
                sendVoice(bot, event, voiceUrl);
                return returnType.IGNORE_FALSE(event.getMessageId());
            }
        }

        return MESSAGE_IGNORE;
    }

    private void sendVoice(Bot bot, AnyMessageEvent event, String voiceUrl) {
        String voiceMsg = MsgUtils.builder().voice(voiceUrl).build();
        if (event.getGroupId() == null) {
            bot.sendPrivateMsg(event.getUserId(), voiceMsg, false);
        } else {
            bot.sendGroupMsg(event.getGroupId(), voiceMsg, false);
        }
    }
}
