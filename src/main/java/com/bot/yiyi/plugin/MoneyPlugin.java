package com.bot.yiyi.plugin;

import com.bot.yiyi.Pojo.ReturnType;
import com.bot.yiyi.Pojo.User;
import com.bot.yiyi.mapper.MoneyMapper;
import com.bot.yiyi.mapper.UserMapper;
import com.bot.yiyi.utils.LimitUtil;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.response.GroupMemberInfoResp;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.bot.yiyi.Pojo.AtBot.AT_BOT;
import static com.bot.yiyi.utils.OperationUtils.luckyDraw;

@Component
public class MoneyPlugin extends BasePlugin {

    private final String PLUGIN_NAME = "MoneyPlugin";
    @Autowired
    private MoneyMapper moneyMapper;
    @Autowired
    private UserMapper usersMapper;
    @Autowired
    private LimitUtil limitUtil;
    @Autowired
    private ReturnType returnType;

    private static final Set<String> CHECK_IN_CMDS = Set.of("打卡", "签到", AT_BOT + "打卡", AT_BOT + "签到");
    private static final Set<String> POINTS_CMDS = Set.of("我的积分", "积分", AT_BOT + "我的积分", AT_BOT + "积分");
    private static final Set<String> LOTTERY_CMDS = Set.of("积分抽奖", "积分赌狗", AT_BOT + "积分抽奖", AT_BOT + "积分赌狗");
    private static final Set<String> RICH_LIST_CMDS = Set.of("富豪榜", AT_BOT + "富豪榜");
    private static final Set<String> POOR_LIST_CMDS = Set.of("负豪榜", AT_BOT + "负豪榜");
    private static final Set<String> GROUP_RICH_LIST_CMDS = Set.of("群富豪榜", AT_BOT + "群富豪榜");
    private static final Set<String> GROUP_POOR_LIST_CMDS = Set.of("群负豪榜", AT_BOT + "群负豪榜");

    private static final Pattern PATTERN_GIFT = Pattern.compile("赠送积分(\\d+)\\[CQ:at,qq=(\\d+)]");
    private static final Pattern PATTERN_STORE = Pattern.compile("存储积分(\\d+)");
    private static final Pattern PATTERN_WITHDRAW = Pattern.compile("取出积分(\\d+)");

    @Override
    public int onGroupMessage(Bot bot, GroupMessageEvent event) {

        if (shouldIgnore(event, PLUGIN_NAME)) return MESSAGE_IGNORE;

        long groupId = event.getGroupId();
        long userId = event.getUserId();
        int messageId = event.getMessageId();
        String msg = event.getMessage().trim();

        User user = usersMapper.selectUser(userId);

        if (CHECK_IN_CMDS.contains(msg)) {
            return handleCheckIn(bot, event, user);
        }
        if (POINTS_CMDS.contains(msg)) {
            return sendMsg(bot, groupId, userId, "你当前的积分为:" + user.getMoney() + "\n你银行中的积分为:" + user.getBank(), messageId);
        }
        if (LOTTERY_CMDS.contains(msg)) {
            return handleLottery(bot, event, user);
        }
        if (RICH_LIST_CMDS.contains(msg)) {
            return sendRichOrPoorList(bot, groupId, userId, moneyMapper.selectMAX(), messageId, false, true);
        }
        if (POOR_LIST_CMDS.contains(msg)) {
            return sendRichOrPoorList(bot, groupId, userId, moneyMapper.selectMIN(), messageId, false, false);
        }
        if (GROUP_RICH_LIST_CMDS.contains(msg)) {
            return sendRichOrPoorList(bot, groupId, userId, moneyMapper.selectGroupMAX(groupId), messageId, true, true);
        }
        if (GROUP_POOR_LIST_CMDS.contains(msg)) {
            return sendRichOrPoorList(bot, groupId, userId, moneyMapper.selectGroupMIN(groupId), messageId, true, false);
        }

        Matcher mGift = PATTERN_GIFT.matcher(msg);
        if (mGift.matches()) {
            return handleGift(bot, event, user, mGift, messageId);
        }

        Matcher mStore = PATTERN_STORE.matcher(msg);
        if (mStore.matches()) {
            return handleStore(bot, event, user, mStore, messageId);
        }

        Matcher mWithdraw = PATTERN_WITHDRAW.matcher(msg);
        if (mWithdraw.matches()) {
            return handleWithdraw(bot, event, user, mWithdraw, messageId);
        }

        return MESSAGE_IGNORE;
    }

    private int handleCheckIn(Bot bot, GroupMessageEvent event, User user) {
        long userId = event.getUserId();
        int messageId = event.getMessageId();

        if (user.getIsCheckIn() == 1) {
            return sendMsg(bot, event.getGroupId(), userId, "今天已经打卡过了!", messageId);
        }
        int money = new Random().nextInt(100);
        moneyMapper.checkIn(userId, money, 1);
        return sendMsg(bot, event.getGroupId(), userId, "打卡成功，获得" + money + "点积分", messageId);
    }

    private int handleLottery(Bot bot, GroupMessageEvent event, User user) {
        long userId = event.getUserId();
        int messageId = event.getMessageId();
        int time = usersMapper.selectTime(userId);

        if (time >= 5) {
            return sendMsg(bot, event.getGroupId(), userId, "小赌怡情大赌伤身,今天已经抽过5次了!", messageId);
        }
        if (user.getMoney() < 0) {
            return sendMsg(bot, event.getGroupId(), userId, "负豪不允许参加!", messageId);
        }
        Map<String, Integer> result = luckyDraw(user.getMoney());
        String key = result.keySet().iterator().next();
        moneyMapper.updateMoney(userId, result.get(key));
        moneyMapper.updateLotteryTime(userId, time + 1);

        return sendMsg(bot, event.getGroupId(), userId, "恭喜你抽中了 积分" + key + ",你当前的积分为" + result.get(key), messageId);
    }

    private int sendRichOrPoorList(Bot bot, long groupId, long userId, List<User> userList, int messageId, boolean isGroupList, boolean isRichList) {
        StringBuilder sb = new StringBuilder(isRichList ? "富豪榜" : "负豪榜");
        int i = 1;
        for (User u : userList) {
            String name = isGroupList
                    ? bot.getGroupMemberInfo(groupId, u.getId(), true).getData().getCard()
                    : bot.getStrangerInfo(u.getId(), true).getData().getNickname();
            if (name == null || name.isEmpty()) name = bot.getStrangerInfo(u.getId(), true).getData().getNickname();;
            if (name == null || name.isEmpty()) name = "匿名";
            sb.append("\n").append(i++).append(". ").append(name).append(" ").append(u.getMoney());
        }
        return sendMsg(bot, groupId, userId, sb.toString(), messageId);
    }

    private int handleGift(Bot bot, GroupMessageEvent event, User user, Matcher matcher, int messageId) {
        int points = Integer.parseInt(matcher.group(1));
        long targetQQ = Long.parseLong(matcher.group(2));
        if (user.getMoney() < points) {
            return sendMsg(bot, event.getGroupId(), event.getUserId(), "你没有这么多积分", messageId);
        }
        moneyMapper.updateMoney(user.getId(), user.getMoney() - points);
        moneyMapper.addMoney(targetQQ, points);
        return sendMsg(bot, event.getGroupId(), event.getUserId(), "赠送积分成功", messageId);
    }

    private int handleStore(Bot bot, GroupMessageEvent event, User user, Matcher matcher, int messageId) {
        int points = Integer.parseInt(matcher.group(1));
        if (user.getMoney() < points) {
            return sendMsg(bot, event.getGroupId(), event.getUserId(), "你没有这么多积分", messageId);
        }
        moneyMapper.storage(user.getId(), points);
        return sendMsg(bot, event.getGroupId(), event.getUserId(), "存储积分成功,银行中的积分不会用来抽奖哦", messageId);
    }

    private int handleWithdraw(Bot bot, GroupMessageEvent event, User user, Matcher matcher, int messageId) {
        int points = Integer.parseInt(matcher.group(1));
        if (user.getBank() < points) {
            return sendMsg(bot, event.getGroupId(), event.getUserId(), "你没有这么多积分", messageId);
        }
        moneyMapper.withdrawal(user.getId(), points);
        return sendMsg(bot, event.getGroupId(), event.getUserId(), "取出积分成功", messageId);
    }

    private int sendMsg(Bot bot, long groupId, long userId, String text, int messageId) {
        String msg = MsgUtils.builder().at(userId).text(text).build();
        bot.sendGroupMsg(groupId, msg, false);
        return returnType.IGNORE_FALSE(messageId);
    }
}
