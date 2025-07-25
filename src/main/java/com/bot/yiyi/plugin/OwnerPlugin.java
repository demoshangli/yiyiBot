package com.bot.yiyi.plugin;

import com.bot.yiyi.Pojo.ReturnType;
import com.bot.yiyi.Pojo.Wife;
import com.bot.yiyi.config.BotConfig;
import com.bot.yiyi.mapper.AdminMapper;
import com.bot.yiyi.mapper.MoneyMapper;
import com.bot.yiyi.mapper.WifeMapper;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.bot.yiyi.Pojo.AtBot.AT_BOT;
import static com.bot.yiyi.Pojo.AtBot.PATTERN_AT_BOT;

@Component
public class OwnerPlugin extends BasePlugin {

    @Autowired
    private BotConfig botConfig;
    @Autowired
    private MoneyMapper moneyMapper;
    @Autowired
    private WifeMapper wifeMapper;
    @Autowired
    private AdminMapper adminMapper;
    @Autowired
    private ReturnType returnType;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 预编译正则表达式
    private static final Pattern PATTERN_SET_ADMIN = Pattern.compile("设置管理员\\[CQ:at,qq=(\\d+)]");
    private static final Pattern PATTERN_SET_OWNER = Pattern.compile("设置主人\\[CQ:at,qq=(\\d+)]");
    private static final Pattern PATTERN_REMOVE_ADMIN = Pattern.compile("移除管理员\\[CQ:at,qq=(\\d+)]");
    private static final Pattern PATTERN_REMOVE_OWNER = Pattern.compile("移除主人\\[CQ:at,qq=(\\d+)]");
    private static final Pattern PATTERN_RUN_SQL = Pattern.compile("^" + PATTERN_AT_BOT + "((?i)(SELECT|INSERT|UPDATE|DELETE|CREATE|DROP|ALTER|TRUNCATE|REPLACE)[\\s\\S]*?;?)");
    private static final Pattern PATTERN_REPLY = Pattern.compile("回复\\s*(\\d+)\\s*(.+)");
    private static final Pattern PATTERN_SET_MONEY = Pattern.compile("设置积分(-?\\d+)\\s*(?:\\[CQ:at,qq=(\\d+)])?");
    private static final Pattern PATTERN_SET_BANK_MONEY = Pattern.compile("设置银行积分(-?\\d+)\\s*(?:\\[CQ:at,qq=(\\d+)])?");
    private static final Pattern PATTERN_RESET_MONEY = Pattern.compile("重置积分\\s*(?:\\[CQ:at,qq=(\\d+)])?");
    private static final Pattern PATTERN_RESET_LOTTERY = Pattern.compile("重置抽奖次数\\s*(?:\\[CQ:at,qq=(\\d+)])?");
    private static final Pattern PATTERN_SET_HUSBAND_LOVE = Pattern.compile("设置老公好感度(-?\\d+)\\s*(?:\\[CQ:at,qq=(\\d+)])?");
    private static final Pattern PATTERN_SET_WIFE_LOVE = Pattern.compile("设置老婆好感度(-?\\d+)\\s*(?:\\[CQ:at,qq=(\\d+)])?");
    private static final Pattern PATTERN_RESET_LOVE = Pattern.compile("重置好感\\s*(?:\\[CQ:at,qq=(\\d+)])?");
    private static final Pattern PATTERN_SET_WIFE = Pattern.compile("设置老婆\\[CQ:at,qq=(\\d+)]");
    private static final Pattern PATTERN_SET_HUSBAND = Pattern.compile("设置老公\\[CQ:at,qq=(\\d+)]");
    private static final Pattern PATTERN_SET_TO_HUSBAND = Pattern.compile("设置\\[CQ:at,qq=(\\d+)]\\s*为\\[CQ:at,qq=(\\d+)]\\s*老公");
    private static final Pattern PATTERN_SET_TO_WIFE = Pattern.compile("设置\\[CQ:at,qq=(\\d+)]\\s*为\\[CQ:at,qq=(\\d+)]\\s*老婆");
    private static final Pattern PATTERN_START = Pattern.compile("(" + PATTERN_AT_BOT + "开机)|(^依依开机$)");
    private static final Pattern PATTERN_SHUTDOWN = Pattern.compile("(" + PATTERN_AT_BOT + "关机)|(^依依关机$)");
    private static final Pattern PATTERN_SET_BLACK = Pattern.compile("拉黑\\[CQ:at,qq=(\\d+)]");
    private static final Pattern PATTERN_REMOVE_BLACK = Pattern.compile("取消拉黑\\[CQ:at,qq=(\\d+)]");
    private static final Pattern PATTERN_RESTART = Pattern.compile("\\[CQ:at,qq=(\\d+)]\\s*重启");


    @Override
    public int onPrivateMessage(Bot bot, PrivateMessageEvent event) {
        String msg = event.getMessage();
        Matcher matcher;

        matcher = PATTERN_REPLY.matcher(msg);
        if (matcher.find()) {
            return handleReply(bot, event, matcher);
        }

        return MESSAGE_IGNORE;
    }

    @Override
    public int onGroupMessage(Bot bot, GroupMessageEvent event) {
        if (!botConfig.isAdmin(event.getUserId())) {
            return MESSAGE_IGNORE;
        }
        String msg = event.getMessage();

        Matcher matcher;

        matcher = PATTERN_RESTART.matcher(msg);
        if (matcher.matches()) {
            return handleRestart(bot, event, matcher);
        }

        matcher = PATTERN_SET_OWNER.matcher(msg);
        if (matcher.matches() && botConfig.isOwner(event.getUserId())) {
            return handleSetOwner(bot, event, matcher);
        }

        matcher = PATTERN_REMOVE_OWNER.matcher(msg);
        if (matcher.matches() && botConfig.isOwner(event.getUserId())) {
            return handleRemoveOwner(bot, event, matcher);
        }

        matcher = PATTERN_SET_ADMIN.matcher(msg);
        if (matcher.matches() && botConfig.isOwner(event.getUserId())) {
            return handleSetAdmin(bot, event, matcher);
        }

        matcher = PATTERN_REMOVE_ADMIN.matcher(msg);
        if (matcher.matches() && botConfig.isOwner(event.getUserId())) {
            return handleRemoveAdmin(bot, event, matcher);
        }

        matcher = PATTERN_START.matcher(msg);
        if (matcher.matches()) {
            return handleStart(bot, event);
        }

        matcher = PATTERN_SHUTDOWN.matcher(msg);
        if (matcher.matches()) {
            return handleShutdown(bot, event);
        }

        matcher = PATTERN_SET_BLACK.matcher(msg);
        if (matcher.matches() && botConfig.isOwner(event.getUserId())) {
            return handleSetBlack(bot, event, matcher);
        }

        matcher = PATTERN_REMOVE_BLACK.matcher(msg);
        if (matcher.matches() && botConfig.isOwner(event.getUserId())) {
            return handleRemoveBlack(bot, event, matcher);
        }

        matcher = PATTERN_RUN_SQL.matcher(msg);
        if (matcher.matches() && botConfig.isOwner(event.getUserId())) {
            return handleRunSql(bot, event, matcher);
        }

        matcher = PATTERN_SET_MONEY.matcher(msg);
        if (matcher.matches()) {
            return handleSetMoney(bot, event, matcher, false);
        }

        matcher = PATTERN_SET_BANK_MONEY.matcher(msg);
        if (matcher.matches()) {
            return handleSetMoney(bot, event, matcher, true);
        }

        matcher = PATTERN_RESET_MONEY.matcher(msg);
        if (matcher.matches()) {
            return handleResetMoney(bot, event, matcher);
        }

        matcher = PATTERN_RESET_LOTTERY.matcher(msg);
        if (matcher.matches()) {
            return handleResetLottery(bot, event, matcher);
        }

        matcher = PATTERN_SET_HUSBAND_LOVE.matcher(msg);
        if (matcher.matches()) {
            return handleSetLove(bot, event, matcher, true);
        }

        matcher = PATTERN_SET_WIFE_LOVE.matcher(msg);
        if (matcher.matches()) {
            return handleSetLove(bot, event, matcher, false);
        }

        matcher = PATTERN_RESET_LOVE.matcher(msg);
        if (matcher.matches()) {
            return handleResetLove(bot, event, matcher);
        }

        matcher = PATTERN_SET_WIFE.matcher(msg);
        if (matcher.matches()) {
            return handleSetSpouse(bot, event, matcher, false);
        }

        matcher = PATTERN_SET_HUSBAND.matcher(msg);
        if (matcher.matches()) {
            return handleSetSpouse(bot, event, matcher, true);
        }

        matcher = PATTERN_SET_TO_HUSBAND.matcher(msg);
        if (matcher.matches()) {
            return handleSetToSpouse(bot, event, matcher, true);
        }

        matcher = PATTERN_SET_TO_WIFE.matcher(msg);
        if (matcher.matches()) {
            return handleSetToSpouse(bot, event, matcher, false);
        }

        return MESSAGE_IGNORE;
    }

    private int handleRestart(Bot bot, GroupMessageEvent event, Matcher matcher) {
        sendGroupMsg(bot, event, "正在重启");
        System.exit(0);
        return MESSAGE_BLOCK;
    }

    private int handleRemoveBlack(Bot bot, GroupMessageEvent event, Matcher matcher) {
        adminMapper.updateUserState(Long.parseLong(matcher.group(1)), 0);
        return sendGroupMsg(bot, event, "用户:" + matcher.group(1) + "\n取消拉黑成功");
    }

    private int handleSetBlack(Bot bot, GroupMessageEvent event, Matcher matcher) {
        long qq = Long.parseLong(matcher.group(1));
        adminMapper.updateUserState(qq, 1);
        return sendGroupMsg(bot, event, "用户:" + qq + "\n拉黑成功");
    }

    private int handleStart(Bot bot, GroupMessageEvent event) {
        adminMapper.updateBotState(event.getGroupId(), 1);
        return sendGroupMsg(bot, event, "依依出来啦!");
    }

    private int handleShutdown(Bot bot, GroupMessageEvent event) {
        adminMapper.updateBotState(event.getGroupId(), 0);
        return sendGroupMsg(bot, event, "那依依再也不会嘻嘻了...");
    }

    private int handleSetOwner(Bot bot, GroupMessageEvent event, Matcher matcher) {
        long qq = Long.parseLong(matcher.group(1));
        if (adminMapper.addAdmin(qq, "owner") == 1) {
            botConfig.init();
            return sendGroupMsg(bot, event, "设置主人成功");
        } else {
            return sendGroupMsg(bot, event, "设置主人失败");
        }
    }

    private int handleRemoveOwner(Bot bot, GroupMessageEvent event, Matcher matcher) {
        long qq = Long.parseLong(matcher.group(1));
        if (adminMapper.deleteAdmin(qq) == 1) {
            botConfig.init();
            return sendGroupMsg(bot, event, "移除主人成功");
        } else {
            return sendGroupMsg(bot, event, "移除主人失败");
        }
    }

    private int handleRunSql(Bot bot, GroupMessageEvent event, Matcher matcher) {
        String sql = matcher.group(1).trim();
        long groupId = event.getGroupId();

        try {
            String lower = sql.toLowerCase();
            if (lower.startsWith("select") || lower.startsWith("show") || lower.startsWith("desc")) {
                var result = jdbcTemplate.queryForList(sql);
                bot.sendGroupMsg(groupId, result.isEmpty() ? "查询成功，无数据" : result.toString(), false);
            } else {
                int affected = jdbcTemplate.update(sql);
                bot.sendGroupMsg(groupId, "执行成功，影响行数: " + affected, false);
            }
        } catch (Exception e) {
            bot.sendGroupMsg(groupId, "执行失败: " + e.getMessage(), false);
        }

        return returnType.IGNORE_FALSE(event.getMessageId());
    }

    private int handleRemoveAdmin(Bot bot, GroupMessageEvent event, Matcher matcher) {
        long qq = Long.parseLong(matcher.group(1));
        if (adminMapper.deleteAdmin(qq) == 1) {
            botConfig.init();
            return sendGroupMsg(bot, event, "移除管理员成功");
        } else {
            return sendGroupMsg(bot, event, "移除管理员失败");
        }
    }

    private int handleSetAdmin(Bot bot, GroupMessageEvent event, Matcher matcher) {
        long qq = Long.parseLong(matcher.group(1));
        if (adminMapper.addAdmin(qq, "admin") == 1) {
            botConfig.init();
            return sendGroupMsg(bot, event, "添加管理员成功");
        } else {
            return sendGroupMsg(bot, event, "添加管理员失败");
        }
    }

    private int handleReply(Bot bot, PrivateMessageEvent event, Matcher matcher) {
        long qq = Long.parseLong(matcher.group(1));
        String content = matcher.group(2).replaceFirst("回复", "").replaceFirst(AT_BOT, "");
        String fromNick = bot.getStrangerInfo(event.getUserId(), true).getData().getNickname();
        bot.sendPrivateMsg(qq, "收到主人" + event.getUserId() + "(" + fromNick + ")" + "的回复\n" + content, false);
        bot.sendPrivateMsg(event.getUserId(), "回复成功啦!", false);
        return returnType.IGNORE_FALSE(event.getMessageId());
    }

    private int handleSetMoney(Bot bot, GroupMessageEvent event, Matcher matcher, boolean bank) {
        int money = Integer.parseInt(matcher.group(1));
        Long qq = matcher.group(2) == null ? event.getUserId() : Long.parseLong(matcher.group(2));
        if (bank) {
            moneyMapper.setBankMoney(qq, money);
            return sendGroupMsg(bot, event, "设置银行积分成功");
        } else {
            moneyMapper.setMoney(qq, money);
            return sendGroupMsg(bot, event, "设置积分成功");
        }
    }

    private int handleResetMoney(Bot bot, GroupMessageEvent event, Matcher matcher) {
        long qq = matcher.group(1) == null ? event.getUserId() : Long.parseLong(matcher.group(1));
        moneyMapper.resetMoney(qq);
        return sendGroupMsg(bot, event, "重置积分成功");
    }

    private int handleResetLottery(Bot bot, GroupMessageEvent event, Matcher matcher) {
        long qq = matcher.group(1) == null ? event.getUserId() : Long.parseLong(matcher.group(1));
        moneyMapper.updateLotteryTime(qq, 0);
        return sendGroupMsg(bot, event, "重置抽奖次数成功");
    }

    private int handleSetLove(Bot bot, GroupMessageEvent event, Matcher matcher, boolean isHusband) {
        int love = Integer.parseInt(matcher.group(1));
        long qq = matcher.group(2) == null ? event.getUserId() : Long.parseLong(matcher.group(2));
        Wife wife = wifeMapper.selectInfo(qq);
        if (isHusband) {
            wifeMapper.setHusbandResponsive(wife.getHusband(), love);
        } else {
            wifeMapper.setWifeResponsive(wife.getWife(), love);
        }
        return sendGroupMsg(bot, event, "设置好感度成功");
    }

    private int handleResetLove(Bot bot, GroupMessageEvent event, Matcher matcher) {
        long qq = matcher.group(1) == null ? event.getUserId() : Long.parseLong(matcher.group(1));
        Wife wife = wifeMapper.selectInfo(qq);
        wifeMapper.setHusbandResponsive(wife.getHusband(), 0);
        wifeMapper.setWifeResponsive(wife.getWife(), 0);
        return sendGroupMsg(bot, event, "重置好感度成功");
    }

    private int handleSetSpouse(Bot bot, GroupMessageEvent event, Matcher matcher, boolean husbandIsFirst) {
        long qq = Long.parseLong(matcher.group(1));
        // 删除之前绑定的关系
        wifeMapper.deleteWife(event.getUserId());
        wifeMapper.deleteWife(qq);
        if (husbandIsFirst) {
            wifeMapper.marry(qq, event.getUserId());
        } else {
            wifeMapper.marry(event.getUserId(), qq);
        }
        String msg = MsgUtils.builder()
                .at(qq)
                .img("https://q1.qlogo.cn/g?b=qq&nk=" + qq + "&s=640")
                .at(event.getUserId())
                .img("https://q1.qlogo.cn/g?b=qq&nk=" + event.getUserId() + "&s=640")
                .text("恭喜你们成功结为夫妻,祝你们百年好合。")
                .build();
        bot.sendGroupMsg(event.getGroupId(), msg, false);
        return returnType.IGNORE_FALSE(event.getMessageId());
    }

    private int handleSetToSpouse(Bot bot, GroupMessageEvent event, Matcher matcher, boolean husbandIsFirst) {
        long qq1 = Long.parseLong(matcher.group(1));
        long qq2 = Long.parseLong(matcher.group(2));
        // 删除之前绑定的关系
        wifeMapper.deleteWife(qq1);
        wifeMapper.deleteWife(qq2);
        if (husbandIsFirst) {
            wifeMapper.marry(qq1, qq2);
        } else {
            wifeMapper.marry(qq2, qq1);
        }
        String msg = MsgUtils.builder()
                .at(qq1)
                .img("https://q1.qlogo.cn/g?b=qq&nk=" + qq1 + "&s=640")
                .at(qq2)
                .img("https://q1.qlogo.cn/g?b=qq&nk=" + qq2 + "&s=640")
                .text("恭喜你们成功结为夫妻,祝你们百年好合。")
                .build();
        bot.sendGroupMsg(event.getGroupId(), msg, false);
        return returnType.IGNORE_FALSE(event.getMessageId());
    }

    private int sendGroupMsg(Bot bot, GroupMessageEvent event, String text) {
        String message = MsgUtils.builder().at(event.getUserId()).text(text).build();
        bot.sendGroupMsg(event.getGroupId(), message, false);
        return returnType.IGNORE_FALSE(event.getMessageId());
    }
}
