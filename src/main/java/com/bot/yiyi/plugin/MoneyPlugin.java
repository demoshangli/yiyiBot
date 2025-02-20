package com.bot.yiyi.plugin;

import com.bot.yiyi.Pojo.ReturnType;
import com.bot.yiyi.Pojo.User;
import com.bot.yiyi.mapper.MoneyMapper;
import com.bot.yiyi.mapper.UserMapper;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.bot.yiyi.plugin.RegisterPlugin.atBot;
import static com.bot.yiyi.utils.OperationUtils.luckyDraw;

@Component
public class MoneyPlugin extends BotPlugin {

    @Autowired
    private MoneyMapper moneyMapper;
    @Autowired
    private UserMapper usersMapper;

    @Override
    public int onGroupMessage(Bot bot, GroupMessageEvent event) {
        if (event.getGroupId() == 176282339L) {
            return ReturnType.IGNORE_TRUE();
        }
        event.setMessage(event.getMessage().trim());
        User user = usersMapper.selectUser(event.getUserId());
        Random random = new Random();
        Set<String> moneySet = new HashSet<>(Arrays.asList("打卡", "签到", atBot + "打卡", atBot + "签到"));
        if (moneySet.contains(event.getMessage())) {
            if (user.getIsCheckIn() == 1) {
                String message = MsgUtils.builder().at(event.getUserId()).text("今天已经打卡过了!").build();
                bot.sendGroupMsg(event.getGroupId(), message, false);
                return ReturnType.IGNORE_FALSE();
            }
            int money = random.nextInt(100);
            moneyMapper.checkIn(event.getUserId(), money, 1);
            String message = MsgUtils.builder().at(event.getUserId()).text("打卡成功，获得" + money + "点积分").build();
            bot.sendGroupMsg(event.getGroupId(), message, false);
            return ReturnType.IGNORE_FALSE();
        }
        moneySet = new HashSet<>(Arrays.asList("我的积分", "积分", atBot + "我的积分", atBot + "积分"));
        if(moneySet.contains(event.getMessage())) {
            String message = MsgUtils.builder().at(event.getUserId()).text("你当前的积分为:" + user.getMoney() + "\n你银行中的积分为:" + user.getBank()).build();
            bot.sendGroupMsg(event.getGroupId(), message, false);
            return ReturnType.IGNORE_FALSE();
        }
        moneySet = new HashSet<>(Arrays.asList("积分抽奖", "积分赌狗", atBot + "积分抽奖", atBot + "积分赌狗"));
        if(moneySet.contains(event.getMessage())) {
            int time = usersMapper.selectTime(event.getUserId());
                if (time == 5) {
                String message = MsgUtils.builder().at(event.getUserId()).text("小赌怡情大赌伤身,今天已经抽过5次了!").build();
                bot.sendGroupMsg(event.getGroupId(), message, false);
                    return ReturnType.IGNORE_FALSE();
                }
            if (user.getMoney() < 0) {
                String message = MsgUtils.builder().at(event.getUserId()).text("负豪不允许参加!").build();
                bot.sendGroupMsg(event.getGroupId(), message, false);
                return ReturnType.IGNORE_FALSE();
            }
            Map<String, Integer> result = luckyDraw(user.getMoney());
            String key = result.keySet().iterator().next();
            moneyMapper.updateMoney(event.getUserId(), result.get(key));
            moneyMapper.updateLotteryTime(event.getUserId(), time + 1);
            String message = MsgUtils.builder().at(event.getUserId()).text("恭喜你抽中了 积分" + key + ",你当前的积分为" + result.get(key)).build();
            bot.sendGroupMsg(event.getGroupId(), message, false);
            return ReturnType.IGNORE_FALSE();
        }
        moneySet = new HashSet<>(Arrays.asList("富豪榜", atBot + "富豪榜"));
        if (moneySet.contains(event.getMessage())) {
            List<User> userList = moneyMapper.selectMAX();
            StringBuilder list = new StringBuilder("富豪榜");
            int i = 1;
            for (User user1 : userList) {
                String name = bot.getStrangerInfo(user1.getId(), true).getData().getNickname();
                list.append("\n").append(i).append(". ").append(name).append(" ").append(user1.getMoney());
                i++;
            }
            bot.sendGroupMsg(event.getGroupId(), list.toString(), false);
            return ReturnType.IGNORE_FALSE();
        }
        moneySet = new HashSet<>(Arrays.asList("负豪榜", atBot + "负豪榜"));
        if (moneySet.contains(event.getMessage())) {
            List<User> userList = moneyMapper.selectMIN();
            StringBuilder list = new StringBuilder("负豪榜");
            int i = 1;
            for (User user1 : userList) {
                String name = bot.getStrangerInfo(user1.getId(), true).getData().getNickname();
                list.append("\n").append(i).append(". ").append(name).append(" ").append(user1.getMoney());
                i++;
            }
            bot.sendGroupMsg(event.getGroupId(), list.toString(), false);
            return ReturnType.IGNORE_FALSE();
        }
        moneySet = new HashSet<>(Arrays.asList("群富豪榜", atBot + "群富豪榜"));
        if (moneySet.contains(event.getMessage())) {
            List<User> userList = moneyMapper.selectGroupMAX(event.getGroupId());
            StringBuilder list = new StringBuilder("富豪榜");
            int i = 1;
            for (User user1 : userList) {
                String name = bot.getGroupMemberInfo(event.getGroupId(), user1.getId(), true).getData().getCard();
                list.append("\n").append(i).append(". ").append(name).append(" ").append(user1.getMoney());
                i++;
            }
            bot.sendGroupMsg(event.getGroupId(), list.toString(), false);
            return ReturnType.IGNORE_FALSE();
        }
        moneySet = new HashSet<>(Arrays.asList("群负豪榜", atBot + "群负豪榜"));
        if (moneySet.contains(event.getMessage())) {
            List<User> userList = moneyMapper.selectGroupMIN(event.getGroupId());
            StringBuilder list = new StringBuilder("负豪榜");
            int i = 1;
            for (User user1 : userList) {
                String name = bot.getGroupMemberInfo(event.getGroupId(), user1.getId(), true).getData().getCard();
                list.append("\n").append(i).append(". ").append(name).append(" ").append(user1.getMoney());
                i++;
            }
            bot.sendGroupMsg(event.getGroupId(), list.toString(), false);
            return ReturnType.IGNORE_FALSE();
        }
        Pattern pattern = Pattern.compile("赠送积分(\\d+)\\[CQ:at,qq=(\\d+)]");
        Matcher matcher = pattern.matcher(event.getMessage());
        if (matcher.matches()) {
            int points = Integer.parseInt(matcher.group(1));
            long qq = Long.parseLong(matcher.group(2));
            if (user.getMoney() < points) {
                String message = MsgUtils.builder().at(event.getUserId()).text("你没有这么多积分").build();
                bot.sendGroupMsg(event.getGroupId(), message, false);
                return ReturnType.IGNORE_FALSE();
            }
            moneyMapper.updateMoney(event.getUserId(), user.getMoney() - points);
            moneyMapper.addMoney(qq, points);
            String message = MsgUtils.builder().at(event.getUserId()).text("赠送积分成功").build();
            bot.sendGroupMsg(event.getGroupId(), message, false);
            return ReturnType.IGNORE_FALSE();
        }
        pattern = Pattern.compile("存储积分(\\d+)");
        matcher = pattern.matcher(event.getMessage());
        if (matcher.matches()) {
            int points = Integer.parseInt(matcher.group(1));
            if (user.getMoney() < points) {
                String message = MsgUtils.builder().at(event.getUserId()).text("你没有这么多积分").build();
                bot.sendGroupMsg(event.getGroupId(), message, false);
                return ReturnType.IGNORE_FALSE();
            }
            moneyMapper.storage(event.getUserId(), points);
            String message = MsgUtils.builder().at(event.getUserId()).text("存储积分成功,银行中的积分不会用来抽奖哦").build();
            bot.sendGroupMsg(event.getGroupId(), message, false);
            return ReturnType.IGNORE_FALSE();
        }
        pattern = Pattern.compile("取出积分(\\d+)");
        matcher = pattern.matcher(event.getMessage());
        if (matcher.matches()) {
            int points = Integer.parseInt(matcher.group(1));
            if (user.getBank() < points) {
                String message = MsgUtils.builder().at(event.getUserId()).text("你没有这么多积分").build();
                bot.sendGroupMsg(event.getGroupId(), message, false);
                return ReturnType.IGNORE_FALSE();
            }
            moneyMapper.withdrawal(event.getUserId(), points);
            String message = MsgUtils.builder().at(event.getUserId()).text("取出积分成功").build();
            bot.sendGroupMsg(event.getGroupId(), message, false);
        }
        return ReturnType.IGNORE_TRUE();
    }
}
