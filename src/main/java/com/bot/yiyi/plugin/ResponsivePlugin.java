package com.bot.yiyi.plugin;

import com.bot.yiyi.Pojo.ReturnType;
import com.bot.yiyi.Pojo.Wife;
import com.bot.yiyi.mapper.MoneyMapper;
import com.bot.yiyi.mapper.WifeMapper;
import com.bot.yiyi.utils.AtUtil;
import com.bot.yiyi.utils.LimitUtil;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ResponsivePlugin extends BotPlugin {

    private static final Pattern EMBRACE_PATTERN = Pattern.compile("抱抱\\[CQ:at,qq=(\\d+)]|\\[CQ:at,qq=(\\d+)]\\s*抱抱");
    private static final Pattern KISS_PATTERN = Pattern.compile("亲亲\\[CQ:at,qq=(\\d+)]|\\[CQ:at,qq=(\\d+)]\\s*亲亲");

    @Autowired
    private WifeMapper wifeMapper;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private MoneyMapper moneyMapper;
    @Autowired
    private LimitUtil limitUtil;
    @Autowired
    private ReturnType returnType;

    private final Random random = new Random();

    @Override
    public int onGroupMessage(Bot bot, GroupMessageEvent event) {
        if (event.getGroupId() == 176282339L) {
            return returnType.IGNORE_TRUE(event.getMessageId());
        }
        if (limitUtil.isLimit(event.getUserId())) {
            return returnType.IGNORE_TRUE(event.getMessageId());
        }

        event.setMessage(AtUtil.parseCQCode(event.getMessage()));

        Long wifeQQ = wifeMapper.isWife(event.getUserId());
        Long husbandQQ = wifeMapper.isHusband(event.getUserId());

        String msg = event.getMessage();

        // 处理抱抱命令
        Matcher embraceMatcher = EMBRACE_PATTERN.matcher(msg);
        if (embraceMatcher.matches()) {
            long targetQQ = Long.parseLong(embraceMatcher.group(1) != null ? embraceMatcher.group(1) : embraceMatcher.group(2));
            return handleEmbrace(bot, event, wifeQQ, husbandQQ, targetQQ);
        }

        // 处理亲亲命令
        Matcher kissMatcher = KISS_PATTERN.matcher(msg);
        if (kissMatcher.matches()) {
            long targetQQ = Long.parseLong(kissMatcher.group(1) != null ? kissMatcher.group(1) : kissMatcher.group(2));
            return handleKiss(bot, event, wifeQQ, husbandQQ, targetQQ);
        }

        // 处理给零花钱命令
        if ("给零花钱".equals(msg)) {
            return handleGivePocketMoney(bot, event, wifeQQ, husbandQQ);
        }

        return MESSAGE_IGNORE;
    }

    private int handleEmbrace(Bot bot, GroupMessageEvent event, Long wifeQQ, Long husbandQQ, long targetQQ) {
        int responsive = random.nextInt(20) + 1;
        Long cachedQQ = getCachedQQ("Embrace:" + event.getUserId());

        // 用户抱的是自己的老婆
        if (Objects.equals(wifeQQ, targetQQ)) {
            if (cachedQQ != null) {
                if (cachedQQ.equals(targetQQ)) {
                    sendMsg(bot, event.getGroupId(), event.getUserId(), "你已经抱过你的老公了。\n过一会再来吧。");
                    return returnType.IGNORE_FALSE(event.getMessageId());
                } else {
                    wifeMapper.updateHusbandResponsive(event.getUserId(), -responsive);
                    sendMsg(bot, event.getGroupId(), event.getUserId(), "你已经抱过别人了!\n你的老公给了你一拳!\n你老公对你的好感度-" + responsive);
                    return returnType.IGNORE_FALSE(event.getMessageId());
                }
            }
            wifeMapper.updateHusbandResponsive(event.getUserId(), responsive);
            setCache("Embrace:" + event.getUserId(), targetQQ, 120);
            sendMsg(bot, event.getGroupId(), event.getUserId(), "你抱了抱你的老公。\n你老公对你的好感度+" + responsive);
            return returnType.IGNORE_FALSE(event.getMessageId());
        }

        // 用户抱的是自己的老公
        if (Objects.equals(husbandQQ, targetQQ)) {
            if (cachedQQ != null) {
                if (cachedQQ.equals(targetQQ)) {
                    sendMsg(bot, event.getGroupId(), event.getUserId(), "你已经抱过你的老婆了。\n过一会再来吧。");
                    return returnType.IGNORE_FALSE(event.getMessageId());
                } else {
                    wifeMapper.updateWifeResponsive(event.getUserId(), -responsive);
                    sendMsg(bot, event.getGroupId(), event.getUserId(), "你已经抱过别人了!\n你的老婆给了你一拳!\n你老婆对你的好感度-" + responsive);
                    return returnType.IGNORE_FALSE(event.getMessageId());
                }
            }
            wifeMapper.updateWifeResponsive(event.getUserId(), responsive);
            setCache("Embrace:" + event.getUserId(), targetQQ, 120);
            sendMsg(bot, event.getGroupId(), event.getUserId(), "你抱了抱你的老婆。\n你老婆对你的好感度+" + responsive);
            return returnType.IGNORE_FALSE(event.getMessageId());
        }

        // 用户已婚且抱别人，惩罚力度加倍
        if (wifeQQ != null) {
            if (cachedQQ != null) {
                wifeMapper.updateHusbandResponsive(event.getUserId(), -(responsive * 2));
                sendMsg(bot, event.getGroupId(), event.getUserId(), "你还想抱别人!\n你老公对你的好感度-" + (responsive * 2));
                return returnType.IGNORE_FALSE(event.getMessageId());
            }
            wifeMapper.updateHusbandResponsive(event.getUserId(), -responsive);
            setCache("Embrace:" + event.getUserId(), targetQQ, 120);
            sendMsg(bot, event.getGroupId(), event.getUserId(), "你抱了抱" + targetQQ + "。\n你老公对你的好感度-" + responsive);
            return returnType.IGNORE_FALSE(event.getMessageId());
        }

        if (husbandQQ != null) {
            if (cachedQQ != null) {
                wifeMapper.updateWifeResponsive(event.getUserId(), -(responsive * 2));
                sendMsg(bot, event.getGroupId(), event.getUserId(), "你还想抱别人!\n你老婆对你的好感度-" + (responsive * 2));
                return returnType.IGNORE_FALSE(event.getMessageId());
            }
            wifeMapper.updateWifeResponsive(event.getUserId(), -responsive);
            setCache("Embrace:" + event.getUserId(), targetQQ, 120);
            sendMsg(bot, event.getGroupId(), event.getUserId(), "你抱了抱" + targetQQ + "。\n你老婆对你的好感度-" + responsive);
            return returnType.IGNORE_FALSE(event.getMessageId());
        }

        // 其他情况，什么都没发生
        sendMsg(bot, event.getGroupId(), event.getUserId(), "你抱了抱" + targetQQ + "。\n什么都没有发生。");
        return returnType.IGNORE_FALSE(event.getMessageId());
    }

    private int handleKiss(Bot bot, GroupMessageEvent event, Long wifeQQ, Long husbandQQ, long targetQQ) {
        int responsive = random.nextInt(100) + 1;
        Long cachedQQ = getCachedQQ("Kiss:" + event.getUserId());

        // 查询用户夫妻关系详细信息
        Wife wifeInfo = wifeMapper.selectInfo(event.getUserId());

        // 用户亲的是自己的老婆
        if (Objects.equals(wifeQQ, targetQQ)) {
            if (wifeInfo != null && wifeInfo.getHusbandFavorAbility() <= 1000) {
                sendMsg(bot, event.getGroupId(), event.getUserId(), "你的老公拒绝了你的亲亲。\n他对你的好感度到1000之后再来吧。");
                return returnType.IGNORE_FALSE(event.getMessageId());
            }
            if (cachedQQ != null) {
                if (cachedQQ.equals(targetQQ)) {
                    sendMsg(bot, event.getGroupId(), event.getUserId(), "你已经亲过你的老公了。\n过一会再来吧。");
                    return returnType.IGNORE_FALSE(event.getMessageId());
                } else {
                    wifeMapper.updateHusbandResponsive(event.getUserId(), -responsive);
                    sendMsg(bot, event.getGroupId(), event.getUserId(), "你已经亲过别人了!\n你的老公给了你一拳!\n你老公对你的好感度-" + responsive);
                    return returnType.IGNORE_FALSE(event.getMessageId());
                }
            }
            wifeMapper.updateHusbandResponsive(event.getUserId(), responsive);
            setCache("Kiss:" + event.getUserId(), targetQQ, 360);
            sendMsg(bot, event.getGroupId(), event.getUserId(), "你亲了亲你的老公。❤\n你老公对你的好感度+" + responsive);
            return returnType.IGNORE_FALSE(event.getMessageId());
        }

        // 用户亲的是自己的老公
        if (Objects.equals(husbandQQ, targetQQ)) {
            if (wifeInfo != null && wifeInfo.getWifeFavorAbility() <= 1000) {
                sendMsg(bot, event.getGroupId(), event.getUserId(), "你的老婆拒绝了你的亲亲。\n她对你的好感度到1000之后再来吧。");
                return returnType.IGNORE_FALSE(event.getMessageId());
            }
            if (cachedQQ != null) {
                if (cachedQQ.equals(targetQQ)) {
                    sendMsg(bot, event.getGroupId(), event.getUserId(), "你已经亲过你的老婆了。\n过一会再来吧。");
                    return returnType.IGNORE_FALSE(event.getMessageId());
                } else {
                    wifeMapper.updateWifeResponsive(event.getUserId(), -responsive);
                    sendMsg(bot, event.getGroupId(), event.getUserId(), "你已经亲过别人了!\n你的老婆给了你一拳!\n你老婆对你的好感度-" + responsive);
                    return returnType.IGNORE_FALSE(event.getMessageId());
                }
            }
            wifeMapper.updateWifeResponsive(event.getUserId(), responsive);
            setCache("Kiss:" + event.getUserId(), targetQQ, 360);
            sendMsg(bot, event.getGroupId(), event.getUserId(), "你亲了亲你的老婆。❤\n你老婆对你的好感度+" + responsive);
            return returnType.IGNORE_FALSE(event.getMessageId());
        }

        // 已婚用户亲别人，惩罚力度加倍
        if (wifeQQ != null) {
            if (cachedQQ != null) {
                wifeMapper.updateHusbandResponsive(event.getUserId(), -(responsive * 2));
                sendMsg(bot, event.getGroupId(), event.getUserId(), "你还想亲别人!\n你老公对你的好感度-" + (responsive * 2));
                return returnType.IGNORE_FALSE(event.getMessageId());
            }
            wifeMapper.updateHusbandResponsive(event.getUserId(), -responsive);
            setCache("Kiss:" + event.getUserId(), targetQQ, 360);
            sendMsg(bot, event.getGroupId(), event.getUserId(), "你亲了亲" + targetQQ + "。❤\n你老公对你的好感度-" + responsive);
            return returnType.IGNORE_FALSE(event.getMessageId());
        }

        if (husbandQQ != null) {
            if (cachedQQ != null) {
                wifeMapper.updateWifeResponsive(event.getUserId(), -(responsive * 2));
                sendMsg(bot, event.getGroupId(), event.getUserId(), "你还想亲别人!\n你老婆对你的好感度-" + (responsive * 2));
                return returnType.IGNORE_FALSE(event.getMessageId());
            }
            wifeMapper.updateWifeResponsive(event.getUserId(), -responsive);
            setCache("Kiss:" + event.getUserId(), targetQQ, 360);
            sendMsg(bot, event.getGroupId(), event.getUserId(), "你亲了亲" + targetQQ + "。❤\n你老婆对你的好感度-" + responsive);
            return returnType.IGNORE_FALSE(event.getMessageId());
        }

        // 其他情况，什么都没发生
        sendMsg(bot, event.getGroupId(), event.getUserId(), "你亲了亲" + targetQQ + "。❤\n什么都没有发生。");
        return returnType.IGNORE_FALSE(event.getMessageId());
    }

    private int handleGivePocketMoney(Bot bot, GroupMessageEvent event, Long wifeQQ, Long husbandQQ) {
        if (moneyMapper.selectMoney(event.getUserId()) < 200) {
            sendMsg(bot, event.getGroupId(), event.getUserId(), "你的积分不多了,先照顾好自己吧。");
            return returnType.IGNORE_FALSE(event.getMessageId());
        }
        if (Boolean.TRUE.equals(redisTemplate.hasKey("giveMoney:" + event.getUserId()))) {
            sendMsg(bot, event.getGroupId(), event.getUserId(), "你今天已经给过对方零花钱了，明天再来吧。");
            return returnType.IGNORE_FALSE(event.getMessageId());
        }
        int i = random.nextInt(20) + 1;
        Wife wife = wifeMapper.selectInfo(event.getUserId());
        if (wife == null) {
            sendMsg(bot, event.getGroupId(), event.getUserId(), "你还没有结婚，无法给零花钱哦。");
            return returnType.IGNORE_FALSE(event.getMessageId());
        }

        if (Objects.equals(wife.getWife(), event.getUserId())) {
            moneyMapper.deductionMoney(event.getUserId(), -i * 10);
            moneyMapper.addMoney(wife.getHusband(), i * 10);
            wifeMapper.updateHusbandResponsive(event.getUserId(), i);
            setCache("giveMoney:" + event.getUserId(), 1, 24 * 60); // 24小时缓存
            sendMsg(bot, event.getGroupId(), event.getUserId(),
                    "你给了你的老公" + wife.getHusband() + i * 10 + "零花钱。\n你老公对你的好感度+" + i);
        } else if (Objects.equals(wife.getHusband(), event.getUserId())) {
            moneyMapper.deductionMoney(event.getUserId(), -i * 10);
            moneyMapper.addMoney(wife.getWife(), i * 10);
            wifeMapper.updateWifeResponsive(event.getUserId(), i);
            setCache("giveMoney:" + event.getUserId(), 1, 24 * 60);
            sendMsg(bot, event.getGroupId(), event.getUserId(),
                    "你给了你的老婆" + wife.getWife() + i * 10 + "零花钱。\n你老婆对你的好感度+" + i);
        } else {
            sendMsg(bot, event.getGroupId(), event.getUserId(), "你还没有结婚，无法给零花钱哦。");
        }
        return returnType.IGNORE_FALSE(event.getMessageId());
    }

    private Long getCachedQQ(String key) {
        Object val = redisTemplate.opsForValue().get(key);
        if (val instanceof Number) {
            return ((Number) val).longValue();
        }
        if (val instanceof String) {
            try {
                return Long.parseLong((String) val);
            } catch (NumberFormatException ignored) {
            }
        }
        return null;
    }

    private void setCache(String key, Object value, long minutes) {
        redisTemplate.opsForValue().set(key, value, minutes, TimeUnit.MINUTES);
    }

    private void sendMsg(Bot bot, long groupId, long userId, String text) {
        String msg = MsgUtils.builder().at(userId).text(text).build();
        bot.sendGroupMsg(groupId, msg, false);
    }

}
