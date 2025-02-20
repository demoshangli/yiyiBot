package com.bot.yiyi.plugin;

import com.bot.yiyi.Pojo.ReturnType;
import com.bot.yiyi.Pojo.Wife;
import com.bot.yiyi.mapper.MoneyMapper;
import com.bot.yiyi.mapper.WifeMapper;
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

    @Override
    public int onGroupMessage(Bot bot, GroupMessageEvent event) {
        if (event.getGroupId() == 176282339L) {
            return ReturnType.IGNORE_TRUE();
        }
        Random random = new Random();
        Pattern pattern = Pattern.compile("抱抱\\[CQ:at,qq=(\\d+)]|\\[CQ:at,qq=(\\d+)]\\s*抱抱");
        Matcher matcher = pattern.matcher(event.getMessage());
        if (matcher.matches()) {
            int responsive = random.nextInt(20) + 1;
            long qq = Long.parseLong(matcher.group(1) != null ? matcher.group(1) : matcher.group(2));
            if (Objects.equals(wifeMapper.isWife(event.getUserId()), qq)) {
                if (Boolean.TRUE.equals(redisTemplate.hasKey("Embrace" + event.getUserId()))) {
                    Long type = (Long) redisTemplate.opsForValue().get("Embrace" + event.getUserId());
                    if (type == qq) {
                        String msg = MsgUtils.builder().at(event.getUserId()).text("你已经抱过你的老公了。")
                                .text("\n过一会再来吧。").build();
                        bot.sendGroupMsg(event.getGroupId(), msg, false);
                        return ReturnType.IGNORE_FALSE();
                    } else {
                        wifeMapper.updateHusbandResponsive(event.getUserId(), -responsive);
                        String msg = MsgUtils.builder().at(event.getUserId()).text("你已经抱过别人了!")
                                .text("\n你的老公给了你一拳!")
                                .text("你老公对你的好感度-" + responsive).build();
                        bot.sendGroupMsg(event.getGroupId(), msg, false);
                        return ReturnType.IGNORE_FALSE();
                    }
                }
                wifeMapper.updateHusbandResponsive(event.getUserId(), responsive);
                redisTemplate.opsForValue().set("Embrace"+ event.getUserId(), qq, 120, TimeUnit.MINUTES);
                String msg = MsgUtils.builder().at(event.getUserId()).text("你抱了抱你的老公。").text("\n")
                        .text("你老公对你的好感度+" + responsive).build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
                return ReturnType.IGNORE_FALSE();
            } else if (Objects.equals(wifeMapper.isHusband(event.getUserId()), qq)) {
                if (Boolean.TRUE.equals(redisTemplate.hasKey("Embrace" + event.getUserId()))) {
                    Long type = (Long) redisTemplate.opsForValue().get("Embrace" + event.getUserId());
                    if (type == qq) {
                        String msg = MsgUtils.builder().at(event.getUserId()).text("你已经抱过你的老婆了。")
                                .text("\n过一会再来吧。").build();
                        bot.sendGroupMsg(event.getGroupId(), msg, false);
                        return ReturnType.IGNORE_FALSE();
                    } else {
                        wifeMapper.updateWifeResponsive(event.getUserId(), -responsive);
                        String msg = MsgUtils.builder().at(event.getUserId()).text("你已经抱过别人了!")
                                .text("\n你的老婆给了你一拳!")
                                .text("你老婆对你的好感度-" + responsive).build();
                        bot.sendGroupMsg(event.getGroupId(), msg, false);
                        return ReturnType.IGNORE_FALSE();
                    }
                }
                wifeMapper.updateWifeResponsive(event.getUserId(), responsive);
                redisTemplate.opsForValue().set("Embrace"+ event.getUserId(), qq, 120, TimeUnit.MINUTES);
                String msg = MsgUtils.builder().at(event.getUserId()).text("你抱了抱你的老婆。").text("\n")
                        .text("你老婆对你的好感度+" + responsive).build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
                return ReturnType.IGNORE_FALSE();
            } else if (wifeMapper.isWife(event.getUserId()) != null){
                if (Boolean.TRUE.equals(redisTemplate.hasKey("Embrace" + event.getUserId()))) {
                        wifeMapper.updateHusbandResponsive(event.getUserId(), -(responsive * 2));
                        String msg = MsgUtils.builder().at(event.getUserId()).text("你还想抱别人!")
                                .text("你老公对你的好感度-" + (responsive * 2)).build();
                        bot.sendGroupMsg(event.getGroupId(), msg, false);
                    return ReturnType.IGNORE_FALSE();
                }
                wifeMapper.updateHusbandResponsive(event.getUserId(), -responsive);
                redisTemplate.opsForValue().set("Embrace"+ event.getUserId(), qq, 120, TimeUnit.MINUTES);
                String msg = MsgUtils.builder().at(event.getUserId()).text("你抱了抱").at(qq).text("。\n")
                        .text("你老公对你的好感度-" + responsive).build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
                return ReturnType.IGNORE_FALSE();
            } else if (wifeMapper.isHusband(event.getUserId()) != null){
                if (Boolean.TRUE.equals(redisTemplate.hasKey("Embrace" + event.getUserId()))) {
                    wifeMapper.updateWifeResponsive(event.getUserId(), -(responsive * 2));
                    String msg = MsgUtils.builder().at(event.getUserId()).text("你还想抱别人!")
                            .text("你老婆对你的好感度-" + (responsive * 2)).build();
                    bot.sendGroupMsg(event.getGroupId(), msg, false);
                    return ReturnType.IGNORE_FALSE();
                }
                wifeMapper.updateWifeResponsive(event.getUserId(), -responsive);
                redisTemplate.opsForValue().set("Embrace"+ event.getUserId(), qq, 120, TimeUnit.MINUTES);
                String msg = MsgUtils.builder().at(event.getUserId()).text("你抱了抱").at(qq).text("。\n")
                        .text("你老婆对你的好感度-" + responsive).build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
                return ReturnType.IGNORE_FALSE();
            } else {
                String msg = MsgUtils.builder().at(event.getUserId()).text("你抱了抱").at(qq).text("。\n")
                        .text("什么都没有发生。").build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
                return ReturnType.IGNORE_FALSE();
            }
        }
        pattern = Pattern.compile("亲亲\\[CQ:at,qq=(\\d+)]|\\[CQ:at,qq=(\\d+)]\\s*亲亲");
        matcher = pattern.matcher(event.getMessage());
        if (matcher.matches()) {
            int responsive = random.nextInt(100) + 1;
            long qq = Long.parseLong(matcher.group(1) != null ? matcher.group(1) : matcher.group(2));
            if (Objects.equals(wifeMapper.isWife(event.getUserId()), qq)) {
                Wife wife = wifeMapper.selectInfo(event.getUserId());
                if (wife.getHusbandFavorAbility() <= 1000) {
                    String msg = MsgUtils.builder().at(event.getUserId()).text("你的老公拒绝了你的亲亲。\n他对你的好感度到1000之后再来吧。").build();
                    bot.sendGroupMsg(event.getGroupId(), msg, false);
                    return ReturnType.IGNORE_FALSE();
                }
                if (Boolean.TRUE.equals(redisTemplate.hasKey("Kiss" + event.getUserId()))) {
                    Long type = (Long) redisTemplate.opsForValue().get("Kiss" + event.getUserId());
                    if (type == qq) {
                        String msg = MsgUtils.builder().at(event.getUserId()).text("你已经亲过你的老公了。")
                                .text("\n过一会再来吧。").build();
                        bot.sendGroupMsg(event.getGroupId(), msg, false);
                        return ReturnType.IGNORE_FALSE();
                    } else {
                        wifeMapper.updateHusbandResponsive(event.getUserId(), -responsive);
                        String msg = MsgUtils.builder().at(event.getUserId()).text("你已经亲过别人了!")
                                .text("\n你的老公给了你一拳!")
                                .text("你老公对你的好感度-" + responsive).build();
                        bot.sendGroupMsg(event.getGroupId(), msg, false);
                        return ReturnType.IGNORE_FALSE();
                    }
                }
                wifeMapper.updateHusbandResponsive(event.getUserId(), responsive);
                redisTemplate.opsForValue().set("Kiss"+ event.getUserId(), qq, 6, TimeUnit.HOURS);
                String msg = MsgUtils.builder().at(event.getUserId()).text("你亲了亲你的老公。").text("❤\n")
                        .text("你老公对你的好感度+" + responsive).build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
                return ReturnType.IGNORE_FALSE();
            } else if (Objects.equals(wifeMapper.isHusband(event.getUserId()), qq)) {
                Wife wife = wifeMapper.selectInfo(event.getUserId());
                if (wife.getWifeFavorAbility() <= 1000) {
                    String msg = MsgUtils.builder().at(event.getUserId()).text("你的老婆拒绝了你的亲亲。\n她对你的好感度到1000之后再来吧。").build();
                    bot.sendGroupMsg(event.getGroupId(), msg, false);
                    return ReturnType.IGNORE_FALSE();
                }
                if (Boolean.TRUE.equals(redisTemplate.hasKey("Kiss" + event.getUserId()))) {
                    Long type = (Long) redisTemplate.opsForValue().get("Kiss" + event.getUserId());
                    if (type == qq) {
                        String msg = MsgUtils.builder().at(event.getUserId()).text("你已经亲过你的老婆了。")
                                .text("\n过一会再来吧。").build();
                        bot.sendGroupMsg(event.getGroupId(), msg, false);
                        return ReturnType.IGNORE_FALSE();
                    } else {
                        wifeMapper.updateWifeResponsive(event.getUserId(), -responsive);
                        String msg = MsgUtils.builder().at(event.getUserId()).text("你已经亲过别人了!")
                                .text("\n你的老婆给了你一拳!")
                                .text("你老婆对你的好感度-" + responsive).build();
                        bot.sendGroupMsg(event.getGroupId(), msg, false);
                        return ReturnType.IGNORE_FALSE();
                    }
                }
                wifeMapper.updateWifeResponsive(event.getUserId(), responsive);
                redisTemplate.opsForValue().set("Kiss"+ event.getUserId(), qq, 6, TimeUnit.HOURS);
                String msg = MsgUtils.builder().at(event.getUserId()).text("你亲了亲你的老婆。").text("❤\n")
                        .text("你老婆对你的好感度+" + responsive).build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
                return ReturnType.IGNORE_FALSE();
            } else if (wifeMapper.isWife(event.getUserId()) != null){
                if (Boolean.TRUE.equals(redisTemplate.hasKey("Kiss" + event.getUserId()))) {
                    wifeMapper.updateHusbandResponsive(event.getUserId(), -(responsive * 2));
                    String msg = MsgUtils.builder().at(event.getUserId()).text("你还想亲别人!")
                            .text("你老公对你的好感度-" + (responsive * 2)).build();
                    bot.sendGroupMsg(event.getGroupId(), msg, false);
                    return ReturnType.IGNORE_FALSE();
                }
                wifeMapper.updateHusbandResponsive(event.getUserId(), -responsive);
                redisTemplate.opsForValue().set("Kiss"+ event.getUserId(), qq, 6, TimeUnit.HOURS);
                String msg = MsgUtils.builder().at(event.getUserId()).text("你亲了亲").at(qq).text("。❤\n")
                        .text("你老公对你的好感度-" + responsive).build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
                return ReturnType.IGNORE_FALSE();
            } else if (wifeMapper.isHusband(event.getUserId()) != null){
                if (Boolean.TRUE.equals(redisTemplate.hasKey("Kiss" + event.getUserId()))) {
                    wifeMapper.updateWifeResponsive(event.getUserId(), -(responsive * 2));
                    String msg = MsgUtils.builder().at(event.getUserId()).text("你还想亲别人!")
                            .text("你老婆对你的好感度-" + (responsive * 2)).build();
                    bot.sendGroupMsg(event.getGroupId(), msg, false);
                    return ReturnType.IGNORE_FALSE();
                }
                wifeMapper.updateWifeResponsive(event.getUserId(), -responsive);
                redisTemplate.opsForValue().set("Kiss"+ event.getUserId(), qq, 6, TimeUnit.HOURS);
                String msg = MsgUtils.builder().at(event.getUserId()).text("你亲了亲").at(qq).text("。❤\n")
                        .text("你老婆对你的好感度-" + responsive).build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
                return ReturnType.IGNORE_FALSE();
            } else {
                String msg = MsgUtils.builder().at(event.getUserId()).text("你亲了亲").at(qq).text("。❤\n")
                        .text("什么都没有发生。").build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
                return ReturnType.IGNORE_FALSE();
            }
        }
        if (event.getMessage().equals("给零花钱")) {
            if (moneyMapper.selectMoney(event.getUserId()) < 200) {
                String msg = MsgUtils.builder().at(event.getUserId()).text("你的积分不多了,先照顾好自己吧。").build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
                return ReturnType.IGNORE_FALSE();
            }
            if (Boolean.TRUE.equals(redisTemplate.hasKey("giveMoney" + event.getUserId()))) {
                String msg = MsgUtils.builder().at(event.getUserId()).text("你今天已经给过对方零花钱了，明天再来吧。").build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
                return ReturnType.IGNORE_FALSE();
            }
            int i = random.nextInt(20) + 1;
            Wife wife = wifeMapper.selectInfo(event.getUserId());
            if (Objects.equals(wife.getWife(), event.getUserId())) {
                moneyMapper.deductionMoney(event.getUserId(), -i * 10);
                moneyMapper.addMoney(wife.getHusband(), i * 10);
                wifeMapper.updateHusbandResponsive(event.getUserId(), i);
                redisTemplate.opsForValue().set("giveMoney" + event.getUserId(), 1);
                String msg = MsgUtils.builder().at(event.getUserId()).text("你给了你的老公").at(wife.getHusband()).text( i * 10 + "零花钱。")
                        .text("\n你老公对你的好感度+" + i).build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
            } else if (Objects.equals(wife.getHusband(), event.getUserId())) {
                moneyMapper.deductionMoney(event.getUserId(), -i * 10);
                moneyMapper.addMoney(wife.getWife(), i * 10);
                wifeMapper.updateWifeResponsive(event.getUserId(), i);
                redisTemplate.opsForValue().set("giveMoney" + event.getUserId(), 1);
                String msg = MsgUtils.builder().at(event.getUserId()).text("你给了你的老婆").at(wife.getWife()).text( i * 10 + "零花钱。")
                        .text("\n你老婆对你的好感度+" + i).build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
            }
            return ReturnType.IGNORE_FALSE();
        }
            return ReturnType.IGNORE_TRUE();
    }
}
