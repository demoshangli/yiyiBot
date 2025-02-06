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
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.bot.yiyi.utils.OperationUtils.luckyDraw;

@Component
public class MoneyPlugin extends BotPlugin {

    @Autowired
    private MoneyMapper moneyMapper;

    @Override
    public int onGroupMessage(Bot bot, GroupMessageEvent event) {
        event.setMessage(event.getMessage().trim());
        User user = moneyMapper.selectUser(event.getUserId());
        Random random = new Random();
        LocalDate today = LocalDate.now();
        if (event.getMessage().equals("打卡") || event.getMessage().equals("签到") || event.getMessage().equals("[CQ:at,qq=" + bot.getSelfId() + "]" + " 打卡") || event.getMessage().equals("[CQ:at,qq=" + bot.getSelfId() + "]" + " 签到")) {
            if (user.getLastCheckTime() != null && user.getLastCheckTime().equals(today)) {
                String message = MsgUtils.builder().at(event.getUserId()).text("今天已经打卡过了!").build();
                bot.sendGroupMsg(event.getGroupId(), message, false);
                return MESSAGE_IGNORE;
            }
            int money = random.nextInt(100);
            moneyMapper.checkIn(event.getUserId(), money, today);
            String message = MsgUtils.builder().at(event.getUserId()).text("打卡成功，获得" + money + "点积分").build();
            bot.sendGroupMsg(event.getGroupId(), message, false);
            return MESSAGE_IGNORE;
        }
        if(event.getMessage().contains("我的积分") || event.getMessage().equals("积分")) {
            String message = MsgUtils.builder().at(event.getUserId()).text("你当前的积分为:" + user.getMoney() + "\n你银行中的积分为:" + user.getBank()).build();
            bot.sendGroupMsg(event.getGroupId(), message, false);
            return MESSAGE_IGNORE;
        }
        if(event.getMessage().contains("积分抽奖") || event.getMessage().contains("积分赌狗")) {
            if (user.getLotteryTime() != null && !user.getLotteryTime().isEmpty()) {
                String[] time = user.getLotteryTime().split(":");
                if (time[0].equals(String.valueOf(today)) && time[1].equals("11111")) {
                String message = MsgUtils.builder().at(event.getUserId()).text("小赌怡情大赌伤身,今天已经抽过5次了!").build();
                bot.sendGroupMsg(event.getGroupId(), message, false);
                return MESSAGE_IGNORE;
                }
                if (!time[0].equals(String.valueOf(today))) {
                    user.setLotteryTime(today + ":");
                }
            } else {
                user.setLotteryTime(today + ":");
            }
            if (user.getMoney() < 0) {
                String message = MsgUtils.builder().at(event.getUserId()).text("负豪不允许参加!").build();
                bot.sendGroupMsg(event.getGroupId(), message, false);
                return MESSAGE_IGNORE;
            }
            Map<String, Integer> result = luckyDraw(user.getMoney());
            String key = result.keySet().iterator().next();
            moneyMapper.updateMoney(event.getUserId(), result.get(key));
            moneyMapper.updateLotteryTime(event.getUserId(), user.getLotteryTime() + "1");
            String message = MsgUtils.builder().at(event.getUserId()).text("恭喜你抽中了 积分" + key + ",你当前的积分为" + result.get(key)).build();
            bot.sendGroupMsg(event.getGroupId(), message, false);
            return MESSAGE_IGNORE;
        }
        if (event.getMessage().equals("富豪榜") || event.getMessage().equals("[CQ:at,qq=" + bot.getSelfId() + "]" + " 富豪榜")) {
            List<User> userList = moneyMapper.selectMAX();
            StringBuilder list = new StringBuilder("富豪榜");
            int i = 1;
            for (User user1 : userList) {
                String name = bot.getStrangerInfo(Long.parseLong(user1.getId()), true).getData().getNickname();
                list.append("\n").append(i).append(". ").append(name).append(" ").append(user1.getMoney());
                i++;
            }
            bot.sendGroupMsg(event.getGroupId(), list.toString(), false);
            return MESSAGE_IGNORE;
        }
        if (event.getMessage().equals("负豪榜") || event.getMessage().equals("[CQ:at,qq=" + bot.getSelfId() + "]" + " 负豪榜")) {
            List<User> userList = moneyMapper.selectMIN();
            StringBuilder list = new StringBuilder("负豪榜");
            int i = 1;
            for (User user1 : userList) {
                String name = bot.getStrangerInfo(Long.parseLong(user1.getId()), true).getData().getNickname();
                list.append("\n").append(i).append(". ").append(name).append(" ").append(user1.getMoney());
                i++;
            }
            bot.sendGroupMsg(event.getGroupId(), list.toString(), false);
            return MESSAGE_IGNORE;
        }
        if (event.getMessage().equals("群富豪榜") || event.getMessage().equals("[CQ:at,qq=" + bot.getSelfId() + "]" + " 群富豪榜")) {
            List<User> userList = moneyMapper.selectGroupMAX(event.getGroupId());
            StringBuilder list = new StringBuilder("富豪榜");
            int i = 1;
            for (User user1 : userList) {
                String name = bot.getGroupMemberInfo(event.getGroupId(), Long.parseLong(user1.getId()), true).getData().getCard();
                list.append("\n").append(i).append(". ").append(name).append(" ").append(user1.getMoney());
                i++;
            }
            bot.sendGroupMsg(event.getGroupId(), list.toString(), false);
            return MESSAGE_IGNORE;
        }
        if (event.getMessage().equals("群负豪榜") || event.getMessage().equals("[CQ:at,qq=" + bot.getSelfId() + "]" + " 群负豪榜")) {
            List<User> userList = moneyMapper.selectGroupMIN(event.getGroupId());
            StringBuilder list = new StringBuilder("负豪榜");
            int i = 1;
            for (User user1 : userList) {
                String name = bot.getGroupMemberInfo(event.getGroupId(), Long.parseLong(user1.getId()), true).getData().getCard();
                list.append("\n").append(i).append(". ").append(name).append(" ").append(user1.getMoney());
                i++;
            }
            bot.sendGroupMsg(event.getGroupId(), list.toString(), false);
            return MESSAGE_IGNORE;
        }
        Pattern pattern = Pattern.compile("赠送积分(\\d+)\\[CQ:at,qq=(\\d+)]");
        Matcher matcher = pattern.matcher(event.getMessage());
        if (matcher.matches()) {
            String points = matcher.group(1);
            String qq = matcher.group(2);
            if (user.getMoney() < Integer.parseInt(points)) {
                String message = MsgUtils.builder().at(event.getUserId()).text("你没有这么多积分").build();
                bot.sendGroupMsg(event.getGroupId(), message, false);
                return MESSAGE_IGNORE;
            }
            moneyMapper.updateMoney(event.getUserId(), user.getMoney() - Integer.parseInt(points));
            moneyMapper.addMoney(Long.parseLong(qq), Integer.parseInt(points));
            String message = MsgUtils.builder().at(event.getUserId()).text("赠送积分成功").build();
            bot.sendGroupMsg(event.getGroupId(), message, false);
            return MESSAGE_IGNORE;
        }
        pattern = Pattern.compile("存储积分(\\d+)");
        matcher = pattern.matcher(event.getMessage());
        if (matcher.matches()) {
            String points = matcher.group(1);
            if (user.getMoney() < Integer.parseInt(points)) {
                String message = MsgUtils.builder().at(event.getUserId()).text("你没有这么多积分").build();
                bot.sendGroupMsg(event.getGroupId(), message, false);
                return MESSAGE_IGNORE;
            }
            moneyMapper.storage(event.getUserId(), Integer.parseInt(points));
            String message = MsgUtils.builder().at(event.getUserId()).text("存储积分成功,银行中的积分不会用来抽奖哦").build();
            bot.sendGroupMsg(event.getGroupId(), message, false);
            return MESSAGE_IGNORE;
        }
        pattern = Pattern.compile("取出积分(\\d+)");
        matcher = pattern.matcher(event.getMessage());
        if (matcher.matches()) {
            String points = matcher.group(1);
            if (user.getBank() < Integer.parseInt(points)) {
                String message = MsgUtils.builder().at(event.getUserId()).text("你没有这么多积分").build();
                bot.sendGroupMsg(event.getGroupId(), message, false);
                return MESSAGE_IGNORE;
            }
            moneyMapper.withdrawal(event.getUserId(), Integer.parseInt(points));
            String message = MsgUtils.builder().at(event.getUserId()).text("取出积分成功").build();
            bot.sendGroupMsg(event.getGroupId(), message, false);
        }
        return MESSAGE_IGNORE;
    }
}
