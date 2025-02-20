package com.bot.yiyi.plugin;

import com.bot.yiyi.Pojo.ReturnType;
import com.bot.yiyi.Pojo.User;
import com.bot.yiyi.mapper.UserMapper;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class RegisterPlugin extends BotPlugin {

    public static String atBot;

    @Autowired
    private UserMapper usersMapper;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public int onAnyMessage(Bot bot, AnyMessageEvent event) {
        System.out.println(event.getMessage());
        atBot = "[CQ:at,qq=" + bot.getSelfId() + "]";
        User user = usersMapper.selectUser(event.getUserId());
        if (user == null) {
            usersMapper.insertUser(event.getUserId());
        }
        return ReturnType.IGNORE_TRUE();
    }

    @Override
    public int onGroupMessage(Bot bot, GroupMessageEvent event) {
       if (usersMapper.selectGroupUserIsHave(event.getUserId(), event.getGroupId()) == null) {
           usersMapper.addGroupUser(event.getUserId(), event.getGroupId());
       }
       if (usersMapper.selectGroupIsHave(event.getGroupId()) == null) {
           usersMapper.addGroup(event.getGroupId());
       }
       return ReturnType.IGNORE_TRUE();
    }

    @Scheduled(cron = "0 0 0 * * ?")
    public void updateDay() {
        List<User> users = usersMapper.selectAllUser();
        for (User user : users) {
            int money = (int) (user.getBank() + (user.getBank() * 0.01));
            usersMapper.updateDay(0, 0, money, user.getId());
        }
        Set<String> giveMoneySet = redisTemplate.keys("giveMoney*");
        if (giveMoneySet != null && !giveMoneySet.isEmpty()) {
            redisTemplate.delete(giveMoneySet);
        }
    }
}
