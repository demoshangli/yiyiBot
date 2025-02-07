package com.bot.yiyi.plugin;

import com.bot.yiyi.Pojo.User;
import com.bot.yiyi.mapper.MoneyMapper;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RegisterPlugin extends BotPlugin {

    public static String atBot;

    @Autowired
    private MoneyMapper moneyMapper;

    @Override
    public int onAnyMessage(Bot bot, AnyMessageEvent event) {
        atBot = "[CQ:at,qq=" + bot.getSelfId() + "]";
        User user = moneyMapper.selectUser(event.getUserId());
        if (user == null) {
            moneyMapper.insertUser(event.getUserId());
        }
        return MESSAGE_IGNORE;
    }

    @Override
    public int onGroupMessage(Bot bot, GroupMessageEvent event) {
       if (moneyMapper.selectGroupIsHave(event.getUserId(), event.getGroupId()) == null) {
            moneyMapper.addGroupUser(event.getUserId(), event.getGroupId());
       }
       return MESSAGE_IGNORE;
    }
}
