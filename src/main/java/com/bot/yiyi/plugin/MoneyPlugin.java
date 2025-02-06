package com.bot.yiyi.plugin;

import com.bot.yiyi.Pojo.User;
import com.bot.yiyi.mapper.MoneyMapper;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;
import java.util.Random;

import static com.bot.yiyi.utils.OperationUtils.luckyDraw;

@Component
public class MoneyPlugin extends BotPlugin {

    @Autowired
    private MoneyMapper moneyMapper;

    @Override
    public int onGroupMessage(Bot bot, GroupMessageEvent event) {
        User user = moneyMapper.selectUser(event.getUserId());
        if (user == null) {
            moneyMapper.insertUser(event.getUserId());
            user = moneyMapper.selectUser(event.getUserId());
        }
        Random random = new Random();
        LocalDate today = LocalDate.now();
        if (event.getMessage().contains("打卡") || event.getMessage().contains("签到")) {
            if (user.getLastCheckTime() != null && user.getLastCheckTime().equals(today)) {
                String message = MsgUtils.builder().at(event.getUserId()).text("今天已经打卡过了!").build();
                bot.sendGroupMsg(event.getGroupId(), message, false);
                return MESSAGE_IGNORE;
            }
            int money = random.nextInt(100);
            moneyMapper.checkIn(event.getUserId(), money, today);
            String message = MsgUtils.builder().at(event.getUserId()).text("打卡成功，获得" + money + "点积分").build();
            bot.sendGroupMsg(event.getGroupId(), message, false);
        }
        if(event.getMessage().contains("我的积分")) {
            String message = MsgUtils.builder().at(event.getUserId()).text("你当前的积分为:" + user.getMoney()).build();
            bot.sendGroupMsg(event.getGroupId(), message, false);
        }
        if(event.getMessage().contains("积分抽奖") || event.getMessage().contains("积分赌狗")) {
            if (user.getMoney() < 0) {
                String message = MsgUtils.builder().at(event.getUserId()).text("负豪不允许参加!").build();
                bot.sendGroupMsg(event.getGroupId(), message, false);
                return MESSAGE_IGNORE;
            }
            Map<String, Integer> result = luckyDraw(user.getMoney());
            String key = result.keySet().iterator().next();
            moneyMapper.updateMoney(event.getUserId(), result.get(key));
            String message = MsgUtils.builder().at(event.getUserId()).text("恭喜你抽中了 积分" + key + ",你当前的积分为" + result.get(key)).build();
            bot.sendGroupMsg(event.getGroupId(), message, false);
        }
        return MESSAGE_IGNORE;
    }
}
