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
        if (event.getGroupId() == 176282339L || limitUtil.isLimit(event.getUserId())) {
            return returnType.IGNORE_TRUE(event.getMessageId());
        }

        event.setMessage(AtUtil.parseCQCode(event.getMessage()));

        if (handleAction(bot, event, "抱抱", "Embrace:", 20, 120, TimeUnit.MINUTES)) {
            return returnType.IGNORE_FALSE(event.getMessageId());
        }

        if (handleKiss(bot, event)) {
            return returnType.IGNORE_FALSE(event.getMessageId());
        }

        if (event.getMessage().equals("给零花钱")) {
            return handlePocketMoney(bot, event);
        }

        return MESSAGE_IGNORE;
    }

    private boolean handleAction(Bot bot, GroupMessageEvent event, String action, String prefix, int bound, long duration, TimeUnit unit) {
        Pattern pattern = Pattern.compile(action + "\\[CQ:at,qq=(\\d+)]|\\[CQ:at,qq=(\\d+)]\\s*" + action);
        Matcher matcher = pattern.matcher(event.getMessage());
        if (!matcher.matches()) return false;

        int responsive = random.nextInt(bound) + 1;
        long userId = event.getUserId();
        long qq = Long.parseLong(matcher.group(1) != null ? matcher.group(1) : matcher.group(2));

        Long loverId = wifeMapper.isWife(userId);
        boolean isWife = Objects.equals(loverId, qq);
        loverId = loverId == null ? wifeMapper.isHusband(userId) : loverId;
        boolean isHusband = Objects.equals(loverId, qq);

        if (hasCooldown(prefix, userId)) {
            Long last = getCooldownTarget(prefix, userId);
            if (last.equals(qq)) {
                send(bot, event, "你已经" + action + "过你的" + (isWife ? "老公" : "老婆") + "了。\n过一会再来吧。", null);
            } else {
                updateResponsive(userId, -responsive, isWife);
                send(bot, event, "你已经" + action + "过别人了!\n你的" + (isWife ? "老公" : "老婆") + "给了你一拳!" + "你" + (isWife ? "老公" : "老婆") + "对你的好感度-" + responsive, null);
            }
            return true;
        }

        setCooldown(prefix, userId, qq, duration, unit);
        if (isWife || isHusband) {
            updateResponsive(userId, responsive, isWife);
            send(bot, event, "你" + action + "了" + (isWife ? "你的老公。" : "你的老婆。") + "\n你" + (isWife ? "老公" : "老婆") + "对你的好感度+" + responsive, null);
        } else if (loverId != null) {
            updateResponsive(userId, -responsive * 2, wifeMapper.isWife(userId) != null);
            send(bot, event, "你还想" + action + "别人!你" + (wifeMapper.isWife(userId) != null ? "老公" : "老婆") + "对你的好感度-" + (responsive * 2), null);
        } else {
            updateResponsive(userId, 0, true);
            send(bot, event, "你" + action + "了" + "", qq, "。\n什么都没有发生。");
        }
        return true;
    }

    private boolean handleKiss(Bot bot, GroupMessageEvent event) {
        Pattern pattern = Pattern.compile("亲亲\\[CQ:at,qq=(\\d+)]|\\[CQ:at,qq=(\\d+)]\\s*亲亲");
        Matcher matcher = pattern.matcher(event.getMessage());
        if (!matcher.matches()) return false;

        int responsive = random.nextInt(100) + 1;
        long userId = event.getUserId();
        long qq = Long.parseLong(matcher.group(1) != null ? matcher.group(1) : matcher.group(2));

        Wife wife = wifeMapper.selectInfo(userId);
        boolean isWife = Objects.equals(wife.getWife(), qq);
        boolean isHusband = Objects.equals(wife.getHusband(), qq);

        if ((isWife && wife.getHusbandFavorAbility() <= 1000) || (isHusband && wife.getWifeFavorAbility() <= 1000)) {
            send(bot, event, "你的" + (isWife ? "老公" : "老婆") + "拒绝了你的亲亲。\n他对你的好感度到1000之后再来吧。", null);
            return true;
        }

        if (hasCooldown("Kiss:", userId)) {
            Long last = getCooldownTarget("Kiss:", userId);
            if (last.equals(qq)) {
                send(bot, event, "你已经亲过你的" + (isWife ? "老公" : "老婆") + "了。\n过一会再来吧。", null);
            } else {
                updateResponsive(userId, -responsive, isWife);
                send(bot, event, "你已经亲过别人了!\n你的" + (isWife ? "老公" : "老婆") + "给了你一拳!你" + (isWife ? "老公" : "老婆") + "对你的好感度-" + responsive, null);
            }
            return true;
        }

        setCooldown("Kiss:", userId, qq, 6, TimeUnit.HOURS);
        if (isWife || isHusband) {
            updateResponsive(userId, responsive, isWife);
            send(bot, event, "你亲了亲你的" + (isWife ? "老公。" : "老婆。") + "❤\n你" + (isWife ? "老公" : "老婆") + "对你的好感度+" + responsive, null);
        } else if (wife.getWife() != null || wife.getHusband() != null) {
            updateResponsive(userId, -responsive * 2, wife.getWife() != null);
            send(bot, event, "你还想亲别人!你" + (wife.getWife() != null ? "老公" : "老婆") + "对你的好感度-" + (responsive * 2), null);
        } else {
            send(bot, event, "你亲了亲", qq, "。❤\n什么都没有发生。");
        }
        return true;
    }

    private int handlePocketMoney(Bot bot, GroupMessageEvent event) {
        long userId = event.getUserId();
        if (moneyMapper.selectMoney(userId) < 200) {
            send(bot, event, "你的积分不多了,先照顾好自己吧。", null);
            return returnType.IGNORE_FALSE(event.getMessageId());
        }

        if (hasCooldown("giveMoney:", userId)) {
            send(bot, event, "你今天已经给过对方零花钱了，明天再来吧。", null);
            return returnType.IGNORE_FALSE(event.getMessageId());
        }

        int i = random.nextInt(20) + 1;
        Wife wife = wifeMapper.selectInfo(userId);
        if (wife == null) {
            send(bot, event, "你还没有伴侣哦。", null);
        }
        if (Objects.equals(wife.getWife(), userId)) {
            transferMoney(bot, event, userId, wife.getHusband(), i, true);
        } else if (Objects.equals(wife.getHusband(), userId)) {
            transferMoney(bot, event, userId, wife.getWife(), i, false);
        }
        return returnType.IGNORE_FALSE(event.getMessageId());
    }

    private void transferMoney(Bot bot, GroupMessageEvent event, long from, long to, int i, boolean toHusband) {
        moneyMapper.deductionMoney(from, i * 10);
        moneyMapper.addMoney(to, i * 10);
        setCooldown("giveMoney:", from, 1L, 1, TimeUnit.DAYS);
        if (toHusband) {
            wifeMapper.updateHusbandResponsive(from, i);
        } else {
            wifeMapper.updateWifeResponsive(from, i);
        }
        send(bot, event, "你给了你的" + (toHusband ? "老公" : "老婆"), to, i * 10 + "零花钱。\n你" + (toHusband ? "老公" : "老婆") + "对你的好感度+" + i);
    }

    private boolean hasCooldown(String prefix, Long userId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(prefix + userId));
    }

    private void setCooldown(String prefix, Long userId, Long targetId, long duration, TimeUnit unit) {
        redisTemplate.opsForValue().set(prefix + userId, targetId, duration, unit);
    }

    private Long getCooldownTarget(String prefix, Long userId) {
        return (Long) redisTemplate.opsForValue().get(prefix + userId);
    }

    private void updateResponsive(Long userId, int responsive, boolean isWife) {
        if (responsive != 0) {
            if (isWife) {
                wifeMapper.updateHusbandResponsive(userId, responsive);
            } else {
                wifeMapper.updateWifeResponsive(userId, responsive);
            }
        }
    }

    private void send(Bot bot, GroupMessageEvent event, String text, Long atQq) {
        MsgUtils builder = MsgUtils.builder().at(event.getUserId()).text(text);
        if (atQq != null) {
            builder.at(atQq);
        }
        bot.sendGroupMsg(event.getGroupId(), builder.build(), false);
    }

    private void send(Bot bot, GroupMessageEvent event, String text, Long atQq, String suffix) {
        MsgUtils builder = MsgUtils.builder().at(event.getUserId()).text(text);
        if (atQq != null) {
            builder.at(atQq);
        }
        if (suffix != null) {
            builder.text(suffix);
        }
        bot.sendGroupMsg(event.getGroupId(), builder.build(), false);
    }
}
