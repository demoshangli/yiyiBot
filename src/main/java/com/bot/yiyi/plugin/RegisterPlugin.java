package com.bot.yiyi.plugin;

import com.bot.yiyi.Pojo.ReturnType;
import com.bot.yiyi.Pojo.User;
import com.bot.yiyi.config.BotConfig;
import com.bot.yiyi.mapper.MoneyMapper;
import com.bot.yiyi.mapper.UserMapper;
import com.bot.yiyi.utils.LimitUtil;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.bot.yiyi.Pojo.AtBot.AT_BOT;

@Component
public class RegisterPlugin extends BotPlugin {

    @Autowired
    private UserMapper usersMapper;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private MoneyMapper moneyMapper;
    @Autowired
    private LimitUtil limitUtil;
    @Autowired
    private BotConfig botConfig;
    @Autowired
    private ReturnType returnType;

    // 预编译正则表达式
    private static final Pattern PATTERN_CONTACT_OWNER = Pattern.compile("联系主人[：:\\-]?\\s*[\\u4e00-\\u9fa5A-Za-z0-9]{0,100}");

    private static final Set<String> SERVER_SET = new HashSet<>(Arrays.asList("状态", AT_BOT + "状态"));

    @Override
    public int onAnyMessage(Bot bot, AnyMessageEvent event) {
        // 推荐用日志替代System.out.println
        System.out.println(event.getMessage());

        User user = usersMapper.selectUser(event.getUserId());
        if (user == null) {
            usersMapper.insertUser(event.getUserId());
        }

        String msg = event.getMessage();
        if (PATTERN_CONTACT_OWNER.matcher(msg).find()) {
            String feedback = msg.replaceFirst("联系主人", "").replaceFirst(AT_BOT, "");
            for (Long ownerQQ : botConfig.getOwnerQQ()) {
                String nick = bot.getStrangerInfo(event.getUserId(), true).getData().getNickname();
                bot.sendPrivateMsg(ownerQQ, "收到" + event.getUserId() + "(" + nick + ")" + "的反馈\n" + feedback, false);
            }
            bot.sendPrivateMsg(event.getUserId(), "反馈已经给到主人啦!", false);
            return returnType.IGNORE_FALSE(event.getMessageId());
        }

        if (SERVER_SET.contains(msg)) {
            limitUtil.isLimitMsg(bot, event);
            return returnType.IGNORE_FALSE(event.getMessageId());
        }

        if (limitUtil.isBlack(event.getUserId())) {
            return returnType.BLOCK(event.getMessageId());
        }
        return returnType.IGNORE_TRUE(event.getMessageId());
    }

    @Override
    public int onGroupMessage(Bot bot, GroupMessageEvent event) {
        long userId = event.getUserId();
        long groupId = event.getGroupId();

        // 先检查用户是否存在群用户关系，避免重复插入
        if (usersMapper.selectGroupUserIsHave(userId, groupId) == null) {
            usersMapper.addGroupUser(userId, groupId);
        }
        if (usersMapper.selectGroupIsHave(groupId) == null) {
            usersMapper.addGroup(groupId);
        }

        if (limitUtil.isBlack(userId)) {
            return returnType.BLOCK(event.getMessageId());
        }
        return MESSAGE_IGNORE;
    }

    // 每天0点执行，为每个用户计算银行积分利息
    @Scheduled(cron = "0 0 0 * * ?")
    public void updateDay() {
        List<User> users = usersMapper.selectAllUser();
        for (User user : users) {
            int interest = (int) (user.getBank() * 0.01);
            int money = (int) (user.getBank() + interest);
            usersMapper.updateDay(0, 0, money, user.getId());
        }
        // 清理Redis中的赠送积分缓存
        Set<String> giveMoneySet = redisTemplate.keys("giveMoney:*");
        if (giveMoneySet != null && !giveMoneySet.isEmpty()) {
            redisTemplate.delete(giveMoneySet);
        }
    }

    // 每分钟执行一次，为“缝纫机”用户增加积分
    @Scheduled(cron = "0 0/1 * * * ?")
    public void updateWeek() {
        Set<String> sewingMachineKeys = redisTemplate.keys("work:SewingMachine:*");
        if (sewingMachineKeys != null && !sewingMachineKeys.isEmpty()) {
            for (String key : sewingMachineKeys) {
                Object val = redisTemplate.opsForValue().get(key);
                if (val instanceof Long) {
                    moneyMapper.addMoney((Long) val, 10);
                } else if (val instanceof Integer) {
                    moneyMapper.addMoney(((Integer) val).longValue(), 10);
                } else if (val instanceof String) {
                    try {
                        moneyMapper.addMoney(Long.parseLong((String) val), 10);
                    } catch (NumberFormatException e) {
                        // 忽略无效数据
                    }
                }
            }
        }
    }
}
