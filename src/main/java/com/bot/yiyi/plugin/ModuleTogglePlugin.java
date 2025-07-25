package com.bot.yiyi.plugin;

import com.bot.yiyi.Pojo.ReturnType;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.bot.yiyi.Pojo.AtBot.AT_BOT;
import static com.bot.yiyi.Pojo.AtBot.PATTERN_AT_BOT;

@Component
public class ModuleTogglePlugin extends BasePlugin {

    @Autowired
    private ReturnType returnType;

    @Autowired
    private RedisTemplate redisTemplate;

    // 中文模块名 -> 英文插件名映射
    private static final Map<String, String> MODULE_MAP = Map.of(
            "游戏模块", "GamePlugin",
            "积分模块", "MoneyPlugin",
            "结婚模块", "WifePlugin",
            "好感度模块", "ResponsivePlugin",
            "AI对话模块", "AiPlugin",
            "伪人模块", "HumanPlugin"
    );

    // 指令格式：@依依 开启模块名 或 关闭模块名（注意“模块”二字）
    private static final Pattern PATTERN_MODULE_SWITCH =
            Pattern.compile(PATTERN_AT_BOT + "(开启|关闭)\\s*([\\u4e00-\\u9fa5A-Za-z0-9]+模块)");

    @Override
    public int onGroupMessage(Bot bot, GroupMessageEvent event) {
        String msg = event.getMessage();
        Matcher matcher = PATTERN_MODULE_SWITCH.matcher(msg);
        if (!matcher.matches() && !msg.contains(AT_BOT)) {
            return MESSAGE_IGNORE;
        }
        if (!matcher.find()) {
            return MESSAGE_IGNORE;
        }
        String action = matcher.group(1);       // 开启 / 关闭
        String moduleCN = matcher.group(2).trim(); // 中文模块名，如 "结婚模块"
        String pluginName = MODULE_MAP.get(moduleCN);
        if (pluginName == null) {
            return MESSAGE_IGNORE;
        }
        String key = "PluginSwitch:" + event.getGroupId() + ":" + pluginName;
        boolean enable = "开启".equals(action);
        if (enable) redisTemplate.delete(key);
        else redisTemplate.opsForValue().set(key, enable);
        bot.sendGroupMsg(event.getGroupId(), "已" + action + " " + moduleCN, false);
        return returnType.IGNORE_FALSE(event.getMessageId());
    }

    /**
     * 供其他插件调用：判断模块是否启用（默认 true）
     */
    public static boolean isModuleEnabled(String pluginName, GroupMessageEvent event, RedisTemplate redisTemplate) {
        String key = "PluginSwitch:" + event.getGroupId() + ":" + pluginName;
        return Boolean.FALSE.equals(redisTemplate.hasKey(key));
    }
}
