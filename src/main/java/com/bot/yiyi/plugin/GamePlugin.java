package com.bot.yiyi.plugin;

import com.bot.yiyi.Pojo.ReturnType;
import com.bot.yiyi.mapper.MoneyMapper;
import com.bot.yiyi.utils.LimitUtil;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.common.utils.OneBotMedia;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.response.GroupMemberInfoResp;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.bot.yiyi.Pojo.AtBot.AT_BOT;

@Component
public class GamePlugin extends BasePlugin {

    @Autowired
    private LimitUtil limitUtil;

    @Autowired
    private MoneyMapper moneyMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ReturnType returnType;

    // 每条群消息会触发此方法
    @Override
    public int onGroupMessage(Bot bot, GroupMessageEvent event) {
        long groupId = event.getGroupId();
        long userId = event.getUserId();
        int messageId = event.getMessageId();
        String message = event.getMessage();

        // 指定群不处理
        if (groupId == 176282339L) {
            return returnType.IGNORE_TRUE(messageId);
        }

        // 判断是否触发限流
        if (limitUtil.isLimit(userId)) {
            return returnType.IGNORE_TRUE(messageId);
        }

        // 游戏触发关键词
        Set<String> gameSet = new HashSet<>(Arrays.asList("看头像猜群友", AT_BOT + "看头像猜群友"));
        if (gameSet.contains(message)) {
            String redisKey = "groupFriend:" + userId;

            // 判断是否已有未完成游戏
            if (Boolean.TRUE.equals(redisTemplate.hasKey(redisKey))) {
                Long lastTargetId = (Long) redisTemplate.opsForValue().get(redisKey);
                String msg = MsgUtils.builder()
                        .at(userId).text("你的上一轮还没有完成哦~")
                        .img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + lastTargetId + "&s=640"))
                        .text("猜猜他是谁吧。").build();
                bot.sendGroupMsg(groupId, msg, false);
                return returnType.IGNORE_FALSE(messageId);
            }

            // 查询积分
            int money = moneyMapper.selectMoney(userId);
            if (money < 100) {
                String msg = MsgUtils.builder().at(userId).text("你的积分不够玩这个游戏哦~").build();
                bot.sendGroupMsg(groupId, msg, false);
                return returnType.IGNORE_FALSE(messageId);
            }

            // 群成员数量检查
            List<GroupMemberInfoResp> memberList = bot.getGroupMemberList(groupId).getData();
            if (memberList.size() < 100) {
                String msg = MsgUtils.builder().at(userId).text("群内人数太少，无法开始游戏。").build();
                bot.sendGroupMsg(groupId, msg, false);
                return returnType.IGNORE_FALSE(messageId);
            }

            // 扣除积分
            moneyMapper.deductionMoney(userId, 100);

            // 随机选择群友
            GroupMemberInfoResp target = memberList.get(new Random().nextInt(memberList.size()));
            redisTemplate.opsForValue().set(redisKey, target.getUserId());

            // 发起游戏提示
            String msg = MsgUtils.builder()
                    .at(userId)
                    .text("猜猜这个头像的群友是谁：")
                    .img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + target.getUserId() + "&s=640"))
                    .text("(注：回答请回复 猜群友@目标群友)").build();
            bot.sendGroupMsg(groupId, msg, false);
            return returnType.IGNORE_FALSE(messageId);
        }

        // 匹配用户的猜测消息
        Pattern pattern = Pattern.compile("猜群友\\[CQ:at,qq=(\\d+)]|\\[CQ:at,qq=(\\d+)]\\s*猜群友");
        Matcher matcher = pattern.matcher(message);
        if (matcher.matches()) {
            String redisKey = "groupFriend:" + userId;

            // 没有游戏记录
            if (Boolean.FALSE.equals(redisTemplate.hasKey(redisKey))) {
                String msg = MsgUtils.builder().at(userId).text("你还没有开始游戏哦。发送 看头像猜群友 开始吧~").build();
                bot.sendGroupMsg(groupId, msg, false);
                return returnType.IGNORE_FALSE(messageId);
            }

            // 获取玩家猜测目标
            Long guessId = Long.parseLong(matcher.group(1) != null ? matcher.group(1) : matcher.group(2));
            Long answerId = (Long) redisTemplate.opsForValue().get(redisKey);

            // 判断猜测是否正确
            redisTemplate.delete(redisKey); // 无论正确与否都清除记录
            if (guessId.equals(answerId)) {
                moneyMapper.addMoney(userId, 500);
                String msg = MsgUtils.builder().at(userId).text("恭喜你答对了！\n获得500积分奖励").build();
                bot.sendGroupMsg(groupId, msg, false);
            } else {
                String msg = MsgUtils.builder().at(userId).text("很遗憾你答错了。正确答案是：").at(answerId)
                        .text("\n下一轮再努力吧！").build();
                bot.sendGroupMsg(groupId, msg, false);
            }
            return returnType.IGNORE_FALSE(messageId);
        }

        return MESSAGE_IGNORE;
    }
}
