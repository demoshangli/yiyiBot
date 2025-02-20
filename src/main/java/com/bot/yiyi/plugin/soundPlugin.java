package com.bot.yiyi.plugin;

import com.bot.yiyi.Pojo.ReturnType;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

import static com.bot.yiyi.plugin.RegisterPlugin.atBot;

@Component
public class soundPlugin extends BotPlugin {

    @Override
    public int onAnyMessage(Bot bot, AnyMessageEvent event) {
        Random random = new Random();
        if (event.getMessage().contains("曼波")) {
            String msg;
            if (random.nextInt(2) == 0)
                msg = MsgUtils.builder().voice("https://wuyao.love/manbo.m4a").build();
            else
                msg = MsgUtils.builder().voice("https://wuyao.love/manbo1.m4a").build();
            if (event.getGroupId() == null) bot.sendPrivateMsg(event.getUserId(), msg, false);
            else bot.sendGroupMsg(event.getGroupId(), msg, false);
            return ReturnType.IGNORE_FALSE();
        }
        if (event.getMessage().contains("阿米诺斯")) {
            String msg;
            if (random.nextInt(2) == 0)
                msg = MsgUtils.builder().voice("https://wuyao.love/amns1.m4a").build();
            else
                msg = MsgUtils.builder().voice("https://wuyao.love/amns.m4a").build();
            if (event.getGroupId() == null) bot.sendPrivateMsg(event.getUserId(), msg, false);
            else bot.sendGroupMsg(event.getGroupId(), msg, false);
            return ReturnType.IGNORE_FALSE();
        }
        if (event.getMessage().contains("私人笑声")) {
            String msg = MsgUtils.builder().voice("https://wuyao.love/hehehe.m4a").build();
            if (event.getGroupId() == null) bot.sendPrivateMsg(event.getUserId(), msg, false);
            else bot.sendGroupMsg(event.getGroupId(), msg, false);
            return ReturnType.IGNORE_FALSE();
        }
        if (event.getMessage().contains("wow")) {
            String msg = MsgUtils.builder().voice("https://wuyao.love/wow.m4a").build();
            if (event.getGroupId() == null) bot.sendPrivateMsg(event.getUserId(), msg, false);
            else bot.sendGroupMsg(event.getGroupId(), msg, false);
            return ReturnType.IGNORE_FALSE();
        }
        if (event.getMessage().contains("哦耶")) {
            String msg = MsgUtils.builder().voice("https://wuyao.love/oy.m4a").build();
            if (event.getGroupId() == null) bot.sendPrivateMsg(event.getUserId(), msg, false);
            else bot.sendGroupMsg(event.getGroupId(), msg, false);
            return ReturnType.IGNORE_FALSE();
        }
        if (event.getMessage().contains("duang") || event.getMessage().contains("Duang")) {
            String msg = MsgUtils.builder().voice("https://wuyao.love/Duang.m4a").build();
            if (event.getGroupId() == null) bot.sendPrivateMsg(event.getUserId(), msg, false);
            else bot.sendGroupMsg(event.getGroupId(), msg, false);
            return ReturnType.IGNORE_FALSE();
        }
        return ReturnType.IGNORE_TRUE();
    }
}
