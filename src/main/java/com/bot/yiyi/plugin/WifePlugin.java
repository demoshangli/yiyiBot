package com.bot.yiyi.plugin;

import com.bot.yiyi.Pojo.User;
import com.bot.yiyi.Pojo.Wife;
import com.bot.yiyi.mapper.MoneyMapper;
import com.bot.yiyi.mapper.WifeMapper;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.common.utils.OneBotMedia;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.action.response.GroupMemberInfoResp;
import com.mikuac.shiro.dto.action.response.StrangerInfoResp;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.bot.yiyi.plugin.RegisterPlugin.atBot;

@Component
public class WifePlugin extends BotPlugin {

    @Autowired
    private WifeMapper wifeMapper;
    @Autowired
    private MoneyMapper moneyMapper;
    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    public int onGroupMessage(Bot bot, GroupMessageEvent event) {

        Set<String> marrySet = new HashSet<>(Arrays.asList("娶群友", "娶老婆", atBot + "娶群友", atBot + "娶老婆"));
        if (marrySet.contains(event.getMessage())) {
            if (isMarry(bot, event, true)) {
                return MESSAGE_IGNORE;
            }
            List<GroupMemberInfoResp> memberList = bot.getGroupMemberList(event.getGroupId()).getData();
            if (memberList.size() <= 1) {
                bot.sendGroupMsg(event.getGroupId(),"群内成员太少，无法匹配。", false);
                return MESSAGE_IGNORE;
            }
            Random random = new Random();
            GroupMemberInfoResp wifeInfo = memberList.get(random.nextInt(memberList.size()));
            if (Objects.equals(wifeInfo.getUserId(), event.getUserId()))
                wifeInfo = memberList.get(random.nextInt(memberList.size()));
            wifeMapper.marry(event.getUserId(), wifeInfo.getUserId());
            String msg = MsgUtils.builder().at(event.getUserId()).text("恭喜你，在茫茫人海中成功娶到了").at(wifeInfo.getUserId())
                    .img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + wifeInfo.getUserId() + "&s=640"))
                    .text("记得好好珍惜她哦。").build();
            bot.sendGroupMsg(event.getGroupId(), msg, false);
            return MESSAGE_IGNORE;
        }
        marrySet = new HashSet<>(Arrays.asList("嫁群友", "嫁老公", atBot + "嫁群友", atBot + "嫁老公"));
        if (marrySet.contains(event.getMessage())) {
            if (isMarry(bot, event, true)) {
                return MESSAGE_IGNORE;
            }
            List<GroupMemberInfoResp> memberList = bot.getGroupMemberList(event.getGroupId()).getData();
            Random random = new Random();
            GroupMemberInfoResp wifeInfo = memberList.get(random.nextInt(memberList.size()));
            if (Objects.equals(wifeInfo.getUserId(), event.getUserId()))
                wifeInfo = memberList.get(random.nextInt(memberList.size()));
            wifeMapper.marry(wifeInfo.getUserId(), event.getUserId());
            String msg = MsgUtils.builder().at(event.getUserId()).text("恭喜你，在茫茫人海中成功嫁给了").at(wifeInfo.getUserId())
                    .img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + wifeInfo.getUserId() + "&s=640"))
                    .text("记得好好珍惜他哦。").build();
            bot.sendGroupMsg(event.getGroupId(), msg, false);
            return MESSAGE_IGNORE;
        }
        Pattern pattern = Pattern.compile("娶\\[CQ:at,qq=(\\d+)]|\\[CQ:at,qq=(\\d+)]娶|\\[CQ:at,qq=(\\d+)] 娶");
        Matcher matcher = pattern.matcher(event.getMessage());
        if (matcher.matches()) {
            if (isMarry(bot, event, true)) {
                return MESSAGE_IGNORE;
            }
            String qq = matcher.group(1);
            if (qq == null) qq = matcher.group(2);
            if (qq == null) qq = matcher.group(3);
            if (isMarry(bot, GroupMessageEvent.builder().userId(Long.parseLong(qq)).groupId(event.getGroupId()).build(), false)) {
                String msg = MsgUtils.builder().at(event.getUserId()).text("人家已经有伴侣了！还是换一个人吧！").build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
                return MESSAGE_IGNORE;
            }
            if (redisTemplate.hasKey(event.getUserId() + "+" + qq)) {
                String msg = MsgUtils.builder().at(event.getUserId()).text("你已经和她求婚了,快@她来处理吧。").build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
                return MESSAGE_IGNORE;
            }
            StrangerInfoResp wifeInfo = bot.getStrangerInfo(Long.parseLong(qq), true).getData();
            Random random = new Random();
            String confessionText = wifeMapper.SelectConfession(random.nextInt(56) + 1);
            redisTemplate.opsForValue().set(event.getUserId() + "+" + qq, 1, 10, TimeUnit.MINUTES);
            String msg = MsgUtils.builder().img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + event.getUserId() + "&s=640"))
                    .text(confessionText + "\n").img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + wifeInfo.getUserId() + "&s=640"))
                    .at(wifeInfo.getUserId()).text("你愿意嫁给").at(event.getUserId()).text("吗?\n")
                    .at(wifeInfo.getUserId()).text("请@对方并回复我愿意/我拒绝\n").
                    text("求婚请求只会存在10分钟哦~").build();
            bot.sendGroupMsg(event.getGroupId(), msg, false);
        }
        pattern = Pattern.compile("嫁\\[CQ:at,qq=(\\d+)]|\\[CQ:at,qq=(\\d+)]嫁|\\[CQ:at,qq=(\\d+)] 嫁");
        matcher = pattern.matcher(event.getMessage());
        if (matcher.matches()) {
            if (isMarry(bot, event, true)) {
                return MESSAGE_IGNORE;
            }
            String qq = matcher.group(1);
            if (qq == null) qq = matcher.group(2);
            if (qq == null) qq = matcher.group(3);
            if (isMarry(bot, GroupMessageEvent.builder().userId(Long.parseLong(qq)).groupId(event.getGroupId()).build(), false)) {
                String msg = MsgUtils.builder().at(event.getUserId()).text("人家已经有伴侣了！还是换一个人吧！").build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
                return MESSAGE_IGNORE;
            }
            if (redisTemplate.hasKey(event.getUserId() + "+" + qq)) {
                String msg = MsgUtils.builder().at(event.getUserId()).text("你已经和他求婚了,快@他来处理吧。").build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
                return MESSAGE_IGNORE;
            }
            StrangerInfoResp wifeInfo = bot.getStrangerInfo(Long.parseLong(qq), true).getData();
            Random random = new Random();
            String confessionText = wifeMapper.SelectConfession(random.nextInt(56) + 1);
            redisTemplate.opsForValue().set(event.getUserId() + "+" + qq, 0, 10, TimeUnit.MINUTES);
            String msg = MsgUtils.builder().img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + event.getUserId() + "&s=640"))
                    .text(confessionText + "\n").img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + wifeInfo.getUserId() + "&s=640"))
                    .at(wifeInfo.getUserId()).text("你愿意娶").at(event.getUserId()).text("吗?\n")
                    .at(wifeInfo.getUserId()).text("请@对方并回复我愿意/我拒绝\n").
                    text("求婚请求只会存在10分钟哦~").build();
            bot.sendGroupMsg(event.getGroupId(), msg, false);
        }
        pattern = Pattern.compile("强娶\\[CQ:at,qq=(\\d+)]|\\[CQ:at,qq=(\\d+)]强娶|\\[CQ:at,qq=(\\d+)] 强娶");
        matcher = pattern.matcher(event.getMessage());
        if (matcher.matches()) {
            if (isMarry(bot, event, true)) {
                return MESSAGE_IGNORE;
            }
            if (redisTemplate.hasKey("join"+ event.getUserId())) {
                String msg = MsgUtils.builder().at(event.getUserId()).text("你还没出狱呢！" + redisTemplate.getExpire("join"+ event.getUserId(), TimeUnit.SECONDS) + "秒后再来吧。").build();
            }
            String qq = matcher.group(1);
            if (qq == null) qq = matcher.group(2);
            if (qq == null) qq = matcher.group(3);
            if (isMarry(bot, GroupMessageEvent.builder().userId(Long.parseLong(qq)).groupId(event.getGroupId()).build(), false)) {
                String msg = MsgUtils.builder().at(event.getUserId()).text("人家已经有伴侣了！还是换一个人吧！").build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
                return MESSAGE_IGNORE;
            }
            Random random = new Random();
            int num = random.nextInt(1000) + 1;
            if (num <= 300) {
                wifeMapper.marry(event.getUserId(), Long.parseLong(qq));
                String msg = MsgUtils.builder().at(event.getUserId()).text("强娶成功了!")
                        .at(Long.parseLong(qq)).text("已经成为你的老婆了!")
                        .img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + qq + "&s=640")).build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
                return MESSAGE_IGNORE;
            } else {
                int time = random.nextInt(300) + 300;
                redisTemplate.opsForValue().set("join"+ event.getUserId(), 1, time, TimeUnit.SECONDS);
                String msg = MsgUtils.builder().at(event.getUserId()).text("很遗憾,你没能娶到她。\n她报警，你被关进大牢" + time + "秒。").build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
                return MESSAGE_IGNORE;
            }
        }
        pattern = Pattern.compile("硬嫁\\[CQ:at,qq=(\\d+)]|\\[CQ:at,qq=(\\d+)]硬嫁|\\[CQ:at,qq=(\\d+)] 硬嫁");
        matcher = pattern.matcher(event.getMessage());
        if (matcher.matches()) {
            if (isMarry(bot, event, true)) {
                return MESSAGE_IGNORE;
            }
            if (redisTemplate.hasKey("join"+ event.getUserId())) {
                String msg = MsgUtils.builder().at(event.getUserId()).text("你还没出狱呢！" + redisTemplate.getExpire("join"+ event.getUserId(), TimeUnit.SECONDS) + "秒后再来吧。").build();
            }
            String qq = matcher.group(1);
            if (qq == null) qq = matcher.group(2);
            if (qq == null) qq = matcher.group(3);
            if (isMarry(bot, GroupMessageEvent.builder().userId(Long.parseLong(qq)).groupId(event.getGroupId()).build(), false)) {
                String msg = MsgUtils.builder().at(event.getUserId()).text("人家已经有伴侣了！还是换一个人吧！").build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
                return MESSAGE_IGNORE;
            }
            Random random = new Random();
            int num = random.nextInt(1000) + 1;
            if (num <= 300) {
                wifeMapper.marry(event.getUserId(), Long.parseLong(qq));
                String msg = MsgUtils.builder().at(event.getUserId()).text("硬嫁成功了!")
                        .at(Long.parseLong(qq)).text("已经成为你的老公了!")
                        .img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + qq + "&s=640")).build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
                return MESSAGE_IGNORE;
            } else {
                int time = random.nextInt(300) + 300;
                redisTemplate.opsForValue().set("join"+ event.getUserId(), 1, time, TimeUnit.SECONDS);
                String msg = MsgUtils.builder().at(event.getUserId()).text("很遗憾,你没能嫁给他。\n他报警，你被关进大牢" + time + "秒。").build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
                return MESSAGE_IGNORE;
            }
        }
        pattern = Pattern.compile("\\[CQ:at,qq=(\\d+)] 我愿意|\\[CQ:at,qq=(\\d+)]我愿意");
        matcher = pattern.matcher(event.getMessage());
        if (matcher.matches()) {
            String qq = matcher.group(1);
            if (qq == null) qq = matcher.group(2);
            if (!redisTemplate.hasKey(qq + "+" + event.getUserId())) {
                String msg = MsgUtils.builder().at(event.getUserId()).text("对方还没有和你求婚呢。").build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
                return MESSAGE_IGNORE;
            }
            if ((int) redisTemplate.opsForValue().get(qq + "+" + event.getUserId()) == 0) {
                wifeMapper.marry(event.getUserId(), Long.parseLong(qq));
            } else  {
                wifeMapper.marry(Long.parseLong(qq), event.getUserId());
            }
                String msg = MsgUtils.builder().at(Long.parseLong(qq)).img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + qq + "&s=640"))
                        .at(event.getUserId()).img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + event.getUserId() + "&s=640"))
                        .text("恭喜你们成功结为夫妻,祝你们百年好合。").build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
                redisTemplate.delete(qq + "+" + event.getUserId());
        }
        pattern = Pattern.compile("\\[CQ:at,qq=(\\d+)] 我拒绝|\\[CQ:at,qq=(\\d+)]我拒绝");
        matcher = pattern.matcher(event.getMessage());
        if (matcher.matches()) {
            String qq = matcher.group(1);
            if (qq == null) qq = matcher.group(2);
            if (!redisTemplate.hasKey(qq + "+" + event.getUserId())) {
                String msg = MsgUtils.builder().at(event.getUserId()).text("对方还没有和你求婚呢。").build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
                return MESSAGE_IGNORE;
            }
            String msg = MsgUtils.builder().at(Long.parseLong(qq)).text("对方拒绝了你的求婚，天涯何处无芳草，何必单恋一支花。").build();
            bot.sendGroupMsg(event.getGroupId(), msg, false);
            redisTemplate.delete(qq + "+" + event.getUserId());
        }
        marrySet = new HashSet<>(Arrays.asList("我的老婆", atBot + "我的老婆"));
        if (marrySet.contains(event.getMessage())) {
            Wife wife = wifeMapper.selectWife(event.getUserId());
            if (wife != null) {
                double husbandProbability = 50 - (double) wife.getHusbandFavorAbility() / 100;
                if (husbandProbability < 0) husbandProbability = 0.0;
                double wifeProbability = 50 - (double) wife.getWifeFavorAbility() / 100;
                if (wifeProbability < 0) wifeProbability = 0.0;
                StrangerInfoResp wifeInfo = bot.getStrangerInfo(Long.parseLong(wife.getWife()), true).getData();
                String msg = MsgUtils.builder().at(event.getUserId()).text("你的老婆是").at(wifeInfo.getUserId())
                        .img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + wifeInfo.getUserId() + "&s=640"))
                        .text("她对你的好感度是" + wife.getWifeFavorAbility() + "。\n")
                        .text("你对她好感度是" + wife.getHusbandFavorAbility() + "。\n")
                        .text("你们的结婚时间是" + wife.getMarryTime() + "。\n")
                        .text("她被抢走的概率为" + wifeProbability + "%。\n")
                        .text("你被抢走的概率为" + husbandProbability + "%。").build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
                return MESSAGE_IGNORE;
            } else {
                wife = wifeMapper.selectHusband(event.getUserId());
                if (wife != null) {
                    double husbandProbability = 50 - (double) wife.getHusbandFavorAbility() / 100;
                    if (husbandProbability < 0) husbandProbability = 0.0;
                    double wifeProbability = 50 - (double) wife.getWifeFavorAbility() / 100;
                    if (wifeProbability < 0) wifeProbability = 0.0;
                    StrangerInfoResp husbandInfo = bot.getStrangerInfo(Long.parseLong(wife.getHusband()), true).getData();
                    String msg = MsgUtils.builder().at(event.getUserId()).text("你没有老婆,但是你有老公。你的老公是").at(husbandInfo.getUserId())
                            .img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + husbandInfo.getUserId() + "&s=640"))
                            .text("他对你的好感度是" + wife.getHusbandFavorAbility() + "。\n")
                            .text("你对他好感度是" + wife.getWifeFavorAbility() + "。\n")
                            .text("你们的结婚时间是" + wife.getMarryTime() + "。\n")
                            .text("他被抢走的概率为" + husbandProbability + "%。\n")
                            .text("你被抢走的概率为" + wifeProbability + "%。").build();
                    bot.sendGroupMsg(event.getGroupId(), msg, false);
                } else {
                    String msg = MsgUtils.builder().at(event.getUserId()).text("醒醒!你既没有老公也没有老婆!").build();
                    bot.sendGroupMsg(event.getGroupId(), msg, false);
                }
                return MESSAGE_IGNORE;
            }
        }
        marrySet = new HashSet<>(Arrays.asList("我的老公", atBot + "我的老公"));
        if (marrySet.contains(event.getMessage())) {
            Wife wife = wifeMapper.selectHusband(event.getUserId());
            if (wife != null) {
                double husbandProbability = 50 - (double) wife.getHusbandFavorAbility() / 100;
                if (husbandProbability < 0) husbandProbability = 0.0;
                double wifeProbability = 50 - (double) wife.getWifeFavorAbility() / 100;
                if (wifeProbability < 0) wifeProbability = 0.0;
                StrangerInfoResp husbandInfo = bot.getStrangerInfo(Long.parseLong(wife.getHusband()), true).getData();
                String msg = MsgUtils.builder().at(event.getUserId()).text("你的老公是").at(husbandInfo.getUserId())
                        .img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + husbandInfo.getUserId() + "&s=640"))
                        .text("他对你的好感度是" + wife.getHusbandFavorAbility() + "。\n")
                        .text("你对他好感度是" + wife.getWifeFavorAbility() + "。\n")
                        .text("你们的结婚时间是" + wife.getMarryTime() + "。\n")
                        .text("他被抢走的概率为" + husbandProbability + "%。\n")
                        .text("你被抢走的概率为" + wifeProbability + "%。").build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
                return MESSAGE_IGNORE;
            } else {
                wife = wifeMapper.selectWife(event.getUserId());
                if (wife != null) {
                    double husbandProbability = 50 - (double) wife.getHusbandFavorAbility() / 100;
                    if (husbandProbability < 0) husbandProbability = 0.0;
                    double wifeProbability = 50 - (double) wife.getWifeFavorAbility() / 100;
                    if (wifeProbability < 0) wifeProbability = 0.0;
                    StrangerInfoResp wifeInfo = bot.getStrangerInfo(Long.parseLong(wife.getWife()), true).getData();
                    String msg = MsgUtils.builder().at(event.getUserId()).text("你没有老公,但是你有老婆。你的老婆是").at(wifeInfo.getUserId())
                            .img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + wifeInfo.getUserId() + "&s=640"))
                            .text("她对你的好感度是" + wife.getWifeFavorAbility() + "。\n")
                            .text("你对她好感度是" + wife.getHusbandFavorAbility() + "。\n")
                            .text("你们的结婚时间是" + wife.getMarryTime() + "。\n")
                            .text("她被抢走的概率为" + wifeProbability + "%。\n")
                            .text("你被抢走的概率为" + husbandProbability + "%。").build();
                    bot.sendGroupMsg(event.getGroupId(), msg, false);
                } else {
                    String msg = MsgUtils.builder().at(event.getUserId()).text("醒醒!你既没有老公也没有老婆!").build();
                    bot.sendGroupMsg(event.getGroupId(), msg, false);
                }
                return MESSAGE_IGNORE;
            }
        }
        if (event.getMessage().contains("抢群友")) {
            String msg = MsgUtils.builder().at(event.getUserId()).text("你想做男还是做女。用抢老婆或者抢老公吧。").build();
            bot.sendGroupMsg(event.getGroupId(), msg, false);
            return MESSAGE_IGNORE;
        }
        pattern = Pattern.compile("\\[CQ:at,qq=(\\d+)] 抢老婆|\\[CQ:at,qq=(\\d+)]抢老婆|抢老婆\\[CQ:at,qq=(\\d+)]");
        matcher = pattern.matcher(event.getMessage());
        if (matcher.matches()) {
            if (!isMarry(bot, event, true)) {
                return MESSAGE_IGNORE;
            }
            User user = moneyMapper.selectUser(event.getUserId());
            if (user.getMoney() < 100) {
                String msg = MsgUtils.builder().at(event.getUserId()).text("你积分不够了!你只有" + user.getMoney() + "积分!攒够100积分再来吧!").build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
            }
            String qq = matcher.group(1);
            if (qq == null) qq = matcher.group(2);
            if (qq == null) qq = matcher.group(3);
            Wife wife = wifeMapper.selectHusband(Long.valueOf(qq));
            Random random = new Random();
            double randomProbability = random.nextDouble() * 100;
            int money = random.nextInt(100);
            if (wife != null) {
                double wifeProbability = 50 - (double) wife.getWifeFavorAbility() / 100;
                if (randomProbability <= wifeProbability) {
                    wifeMapper.deleteWife(Long.parseLong(qq));
                    wifeMapper.marry(event.getUserId(), Long.valueOf(qq));
                    moneyMapper.deductionMoney(event.getUserId(), money);
                    String msg = MsgUtils.builder().at(event.getUserId()).text("恭喜你花费" + money + "积分抢到了")
                            .at(Long.parseLong(wife.getHusband())).text("的老婆!\n")
                            .text("她现在是你的老婆了!").build();
                    bot.sendGroupMsg(event.getGroupId(), msg, false);
                    msg = MsgUtils.builder().at(Long.parseLong(wife.getHusband())).text("你的老婆不要你了!和别人跑掉了!").build();
                    bot.sendGroupMsg(Long.parseLong(wife.getHusband()), msg, false);
                } else {
                    moneyMapper.deductionMoney(event.getUserId(), money);
                    String msg = MsgUtils.builder().at(event.getUserId()).text("抢老婆失败了,对方报警," + money + "积分打水漂了。").build();
                    bot.sendGroupMsg(event.getGroupId(), msg, false);
                }
            } else if((wife = wifeMapper.selectWife(Long.valueOf(qq))) != null) {
                double wifeProbability = 50 - (double) wife.getHusbandFavorAbility() / 100;
                if (randomProbability <= wifeProbability) {
                    wifeMapper.deleteWife(Long.parseLong(qq));
                    wifeMapper.marry(event.getUserId(), Long.valueOf(qq));
                    moneyMapper.deductionMoney(event.getUserId(), money);
                    String msg = MsgUtils.builder().at(event.getUserId()).text("恭喜你花费" + money + "积分抢到了")
                            .at(Long.parseLong(wife.getWife())).text("的老公!\n")
                            .text("她现在是你的老婆了!").build();
                    bot.sendGroupMsg(event.getGroupId(), msg, false);
                    msg = MsgUtils.builder().at(Long.parseLong(wife.getWife())).text("你的老公不要你了!和别人跑掉了!").build();
                    bot.sendGroupMsg(Long.parseLong(wife.getHusband()), msg, false);
                } else {
                    moneyMapper.deductionMoney(event.getUserId(), money);
                    String msg = MsgUtils.builder().at(event.getUserId()).text("抢老婆失败了,对方报警," + money + "积分打水漂了。").build();
                    bot.sendGroupMsg(event.getGroupId(), msg, false);
                }
            } else {
                String msg = MsgUtils.builder().at(event.getUserId()).text("对方还没有伴侣。\n发送娶或嫁向对方求婚吧").build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
            }
        }
        pattern = Pattern.compile("\\[CQ:at,qq=(\\d+)] 抢老公|\\[CQ:at,qq=(\\d+)]抢老公|抢老公\\[CQ:at,qq=(\\d+)]");
        matcher = pattern.matcher(event.getMessage());
        if (matcher.matches()) {
            if (!isMarry(bot, event, true)) {
                return MESSAGE_IGNORE;
            }
            User user = moneyMapper.selectUser(event.getUserId());
            if (user.getMoney() < 100) {
                String msg = MsgUtils.builder().at(event.getUserId()).text("你积分不够了!你只有" + user.getMoney() + "积分!攒够100积分再来吧!").build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
            }
            String qq = matcher.group(1);
            if (qq == null) qq = matcher.group(2);
            if (qq == null) qq = matcher.group(3);
            Wife wife = wifeMapper.selectHusband(Long.valueOf(qq));
            Random random = new Random();
            double randomProbability = random.nextDouble() * 100;
            int money = random.nextInt(100);
            if (wife != null) {
                double wifeProbability = 50 - (double) wife.getWifeFavorAbility() / 100;
                if (randomProbability <= wifeProbability) {
                    wifeMapper.deleteWife(Long.parseLong(qq));
                    wifeMapper.marry(Long.valueOf(qq), event.getUserId());
                    moneyMapper.deductionMoney(event.getUserId(), money);
                    String msg = MsgUtils.builder().at(event.getUserId()).text("恭喜你花费" + money + "积分抢到了")
                            .at(Long.parseLong(wife.getHusband())).text("的老婆!\n")
                            .text("他现在是你的老公了!").build();
                    bot.sendGroupMsg(event.getGroupId(), msg, false);
                    msg = MsgUtils.builder().at(Long.parseLong(wife.getHusband())).text("你的老婆不要你了!和别人跑掉了!").build();
                    bot.sendGroupMsg(Long.parseLong(wife.getHusband()), msg, false);
                } else {
                    moneyMapper.deductionMoney(event.getUserId(), money);
                    String msg = MsgUtils.builder().at(event.getUserId()).text("抢老公失败了,对方报警," + money + "积分打水漂了。").build();
                    bot.sendGroupMsg(event.getGroupId(), msg, false);
                }
            } else if((wife = wifeMapper.selectWife(Long.valueOf(qq))) != null) {
                double wifeProbability = 50 - (double) wife.getHusbandFavorAbility() / 100;
                if (randomProbability <= wifeProbability) {
                    wifeMapper.deleteWife(Long.parseLong(qq));
                    wifeMapper.marry(Long.valueOf(qq), event.getUserId());
                    moneyMapper.deductionMoney(event.getUserId(), money);
                    String msg = MsgUtils.builder().at(event.getUserId()).text("恭喜你花费" + money + "积分抢到了")
                            .at(Long.parseLong(wife.getWife())).text("的老公!\n")
                            .text("他现在是你的老公了!").build();
                    bot.sendGroupMsg(event.getGroupId(), msg, false);
                    msg = MsgUtils.builder().at(Long.parseLong(wife.getWife())).text("你的老公不要你了!和别人跑掉了!").build();
                    bot.sendGroupMsg(Long.parseLong(wife.getHusband()), msg, false);
                } else {
                    moneyMapper.deductionMoney(event.getUserId(), money);
                    String msg = MsgUtils.builder().at(event.getUserId()).text("抢老公失败了,对方报警," + money + "积分打水漂了。").build();
                    bot.sendGroupMsg(event.getGroupId(), msg, false);
                }
            } else {
                String msg = MsgUtils.builder().at(event.getUserId()).text("对方还没有伴侣。\n发送娶或嫁向对方求婚吧").build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
            }
        }
        marrySet = new HashSet<>(Arrays.asList("闹离婚", "闹分手"));
        if (marrySet.contains(event.getMessage())) {
            if (!isMarry(bot, event, false)) {
                String msg = MsgUtils.builder().at(event.getUserId()).text("你还没有伴侣,你离个锤子!").build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
            }
            String wife = wifeMapper.isHusband(event.getUserId());
            if (wife == null) wife = wifeMapper.isWife(event.getUserId());
            wifeMapper.deleteWife(event.getUserId());
            String msg = MsgUtils.builder().at(event.getUserId()).text("你与").at(Long.parseLong(wife)).text("的婚约已解除!\n")
                    .text("没想到你们会走到这一步，祝你们各自安好。").build();
            bot.sendGroupMsg(event.getGroupId(), msg, false);
        }
        return MESSAGE_IGNORE;
    }

    private boolean isMarry(Bot bot, GroupMessageEvent event, boolean isMsg) {
        String companion = wifeMapper.isWife(event.getUserId());
        if (companion != null) {
            if (isMsg) {
                StrangerInfoResp wifeInfo = bot.getStrangerInfo(Long.parseLong(companion), true).getData();
                String msg = MsgUtils.builder().at(event.getUserId()).text("你已经有老公了!你的老公是").at(wifeInfo.getUserId())
                        .img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + wifeInfo.getUserId() + "&s=640"))
                        .text("不要朝三暮四哦。").build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
            }
            return true;
        }
        companion = wifeMapper.isHusband(event.getUserId());
        if (companion != null) {
            if (isMsg) {
                StrangerInfoResp husbandInfo = bot.getStrangerInfo(Long.parseLong(companion), true).getData();
                String msg = MsgUtils.builder().at(event.getUserId()).text("你已经有老婆了!你的老婆是").at(husbandInfo.getUserId())
                        .img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + husbandInfo.getUserId() + "&s=640"))
                        .text("不要朝三暮四哦。").build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
            }
            return true;
        }
        return false;
    }
}
