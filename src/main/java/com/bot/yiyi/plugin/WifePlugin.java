package com.bot.yiyi.plugin;

import com.bot.yiyi.Pojo.ReturnType;
import com.bot.yiyi.Pojo.User;
import com.bot.yiyi.Pojo.Wife;
import com.bot.yiyi.mapper.MoneyMapper;
import com.bot.yiyi.mapper.UserMapper;
import com.bot.yiyi.mapper.WifeMapper;
import com.bot.yiyi.utils.AtUtil;
import com.bot.yiyi.utils.LimitUtil;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.common.utils.OneBotMedia;
import com.mikuac.shiro.core.Bot;
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

import static com.bot.yiyi.Pojo.AtBot.AT_BOT;


@Component
public class  WifePlugin extends BasePlugin {

    private final String PLUGIN_NAME = "WifePlugin";

    @Autowired
    private WifeMapper wifeMapper;
    @Autowired
    private MoneyMapper moneyMapper;
    @Autowired
    private UserMapper usersMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private LimitUtil limitUtil;
    @Autowired
    private ReturnType returnType;


    @Override
    public int onGroupMessage(Bot bot, GroupMessageEvent event) {

        if (shouldIgnore(event, PLUGIN_NAME)) return MESSAGE_IGNORE;

        if (limitUtil.isLimit(event.getUserId()))
            return MESSAGE_IGNORE;
        event.setMessage(AtUtil.parseCQCode(event.getMessage()));
        Set<String> marrySet = new HashSet<>(Arrays.asList("娶群友", "娶老婆", AT_BOT + "娶群友", AT_BOT + "娶老婆"));
        if (marrySet.contains(event.getMessage())) {
            if (isMarry(bot, event)) {
                return returnType.IGNORE_FALSE(event.getMessageId());
            }
            List<GroupMemberInfoResp> memberList = bot.getGroupMemberList(event.getGroupId()).getData();
            if (memberList.size() <= 1) {
                bot.sendGroupMsg(event.getGroupId(), "群内成员太少，无法匹配。", false);
                return returnType.IGNORE_FALSE(event.getMessageId());
            }
            Random random = new Random();
            GroupMemberInfoResp wifeInfo = memberList.get(random.nextInt(memberList.size()));
            if (Objects.equals(wifeInfo.getUserId(), event.getUserId()))
                wifeInfo = memberList.get(random.nextInt(memberList.size()));
            if (isMarry(wifeInfo.getUserId()))
                wifeInfo = memberList.get(random.nextInt(memberList.size()));
            wifeMapper.marry(event.getUserId(), wifeInfo.getUserId());
            String msg = MsgUtils.builder().at(event.getUserId()).text("恭喜你，在茫茫人海中成功娶到了").at(wifeInfo.getUserId())
                    .img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + wifeInfo.getUserId() + "&s=640"))
                    .text("记得好好珍惜她哦。").build();
            bot.sendGroupMsg(event.getGroupId(), msg, false);
            return returnType.IGNORE_FALSE(event.getMessageId());
        }
        marrySet = new HashSet<>(Arrays.asList("嫁群友", "嫁老公", AT_BOT + "嫁群友", AT_BOT + "嫁老公"));
        if (marrySet.contains(event.getMessage())) {
            if (isMarry(bot, event)) {
                return returnType.IGNORE_FALSE(event.getMessageId());
            }
            List<GroupMemberInfoResp> memberList = bot.getGroupMemberList(event.getGroupId()).getData();
            Random random = new Random();
            GroupMemberInfoResp wifeInfo = memberList.get(random.nextInt(memberList.size()));
            if (Objects.equals(wifeInfo.getUserId(), event.getUserId()))
                wifeInfo = memberList.get(random.nextInt(memberList.size()));
            if (isMarry(wifeInfo.getUserId()))
                wifeInfo = memberList.get(random.nextInt(memberList.size()));
            wifeMapper.marry(wifeInfo.getUserId(), event.getUserId());
            String msg = MsgUtils.builder().at(event.getUserId()).text("恭喜你，在茫茫人海中成功嫁给了").at(wifeInfo.getUserId())
                    .img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + wifeInfo.getUserId() + "&s=640"))
                    .text("记得好好珍惜他哦。").build();
            bot.sendGroupMsg(event.getGroupId(), msg, false);
            return returnType.IGNORE_FALSE(event.getMessageId());
        }
        Pattern pattern = Pattern.compile("娶\\[CQ:at,qq=(\\d+)]|\\[CQ:at,qq=(\\d+)]\\s*娶");
        Matcher matcher = pattern.matcher(event.getMessage());
        if (matcher.matches()) {
            if (isMarry(bot, event)) {
                return returnType.IGNORE_FALSE(event.getMessageId());
            }
            Long qq = Long.valueOf(matcher.group(1) != null ? matcher.group(1) : matcher.group(2));
            if (qq.equals(event.getUserId())) {
                String msg = MsgUtils.builder().at(event.getUserId()).text("不能娶自己！").build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
                return returnType.IGNORE_FALSE(event.getMessageId());
            }
            if (isMarry(qq)) {
                String msg = MsgUtils.builder().at(event.getUserId()).text("人家已经有伴侣了！还是换一个人吧！").build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
                return returnType.IGNORE_FALSE(event.getMessageId());
            }
            if (Boolean.TRUE.equals(redisTemplate.hasKey(event.getUserId() + "+" + qq))) {
                String msg = MsgUtils.builder().at(event.getUserId()).text("你已经和她求婚了,快@她来处理吧。").build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
                return returnType.IGNORE_FALSE(event.getMessageId());
            }
            StrangerInfoResp wifeInfo = bot.getStrangerInfo(qq, true).getData();
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
        pattern = Pattern.compile("嫁\\[CQ:at,qq=(\\d+)]|\\[CQ:at,qq=(\\d+)]\\s*嫁");
        matcher = pattern.matcher(event.getMessage());
        if (matcher.matches()) {
            if (isMarry(bot, event)) {
                return returnType.IGNORE_FALSE(event.getMessageId());
            }
            Long qq = Long.valueOf(matcher.group(1) != null ? matcher.group(1) : matcher.group(2));
            if (qq.equals(event.getUserId())) {
                String msg = MsgUtils.builder().at(event.getUserId()).text("不能嫁自己！").build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
                return returnType.IGNORE_FALSE(event.getMessageId());
            }
            if (isMarry(qq)) {
                String msg = MsgUtils.builder().at(event.getUserId()).text("人家已经有伴侣了！还是换一个人吧！").build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
                return returnType.IGNORE_FALSE(event.getMessageId());
            }
            if (Boolean.TRUE.equals(redisTemplate.hasKey(event.getUserId() + "+" + qq))) {
                String msg = MsgUtils.builder().at(event.getUserId()).text("你已经和他求婚了,快@他来处理吧。").build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
                return returnType.IGNORE_FALSE(event.getMessageId());
            }
            StrangerInfoResp wifeInfo = bot.getStrangerInfo(qq, true).getData();
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
        pattern = Pattern.compile("强娶\\[CQ:at,qq=(\\d+)]|\\[CQ:at,qq=(\\d+)]\\s*强娶");
        matcher = pattern.matcher(event.getMessage());
        if (matcher.matches()) {
            if (isMarry(bot, event)) {
                return returnType.IGNORE_FALSE(event.getMessageId());
            }
            Long qq = Long.valueOf(matcher.group(1) != null ? matcher.group(1) : matcher.group(2));
            if (qq.equals(event.getUserId())) {
                String msg = MsgUtils.builder().at(event.getUserId()).text("不能强娶自己！").build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
                return returnType.IGNORE_FALSE(event.getMessageId());
            }
            if (isMarry(qq)) {
                String msg = MsgUtils.builder().at(event.getUserId()).text("人家已经有伴侣了！还是换一个人吧！").build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
                return returnType.IGNORE_FALSE(event.getMessageId());
            }
            Random random = new Random();
            int num = random.nextInt(1000) + 1;
            if (num <= 300) {
                wifeMapper.marry(event.getUserId(), qq);
                String msg = MsgUtils.builder().at(event.getUserId()).text("强娶成功了!")
                        .at(qq).text("已经成为你的老婆了!")
                        .img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + qq + "&s=640")).build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
                return returnType.IGNORE_FALSE(event.getMessageId());
            } else {
                int time = random.nextInt(300) + 300;
                redisTemplate.opsForValue().set("join:" + event.getUserId(), 1, time, TimeUnit.SECONDS);
                String msg = MsgUtils.builder().at(event.getUserId()).text("很遗憾,你没能娶到她。\n她报警，你被关进大牢" + time + "秒。").build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
                return returnType.IGNORE_FALSE(event.getMessageId());
            }
        }
        pattern = Pattern.compile("硬嫁\\[CQ:at,qq=(\\d+)]|\\[CQ:at,qq=(\\d+)]\\s*硬嫁");
        matcher = pattern.matcher(event.getMessage());
        if (matcher.matches()) {
            if (isMarry(bot, event)) {
                return returnType.IGNORE_FALSE(event.getMessageId());
            }
            Long qq = Long.valueOf(matcher.group(1) != null ? matcher.group(1) : matcher.group(2));
            if (qq.equals(event.getUserId())) {
                String msg = MsgUtils.builder().at(event.getUserId()).text("不能硬嫁自己！").build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
                return returnType.IGNORE_FALSE(event.getMessageId());
            }
            if (isMarry(qq)) {
                String msg = MsgUtils.builder().at(event.getUserId()).text("人家已经有伴侣了！还是换一个人吧！").build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
                return returnType.IGNORE_FALSE(event.getMessageId());
            }
            Random random = new Random();
            int num = random.nextInt(1000) + 1;
            if (num <= 300) {
                wifeMapper.marry(event.getUserId(), qq);
                String msg = MsgUtils.builder().at(event.getUserId()).text("硬嫁成功了!")
                        .at(qq).text("已经成为你的老公了!")
                        .img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + qq + "&s=640")).build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
                return returnType.IGNORE_FALSE(event.getMessageId());
            } else {
                int time = random.nextInt(300) + 300;
                redisTemplate.opsForValue().set("join:" + event.getUserId(), 1, time, TimeUnit.SECONDS);
                String msg = MsgUtils.builder().at(event.getUserId()).text("很遗憾,你没能嫁给他。\n他报警，你被关进大牢" + time + "秒。").build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
                return returnType.IGNORE_FALSE(event.getMessageId());
            }
        }
        pattern = Pattern.compile("\\[CQ:at,qq=(\\d+)]\\s*我愿意|我愿意\\[CQ:at,qq=(\\d+)]");
        matcher = pattern.matcher(event.getMessage());
        if (matcher.matches()) {
            Long qq = matcher.group(1) != null ? Long.valueOf(matcher.group(1)) : Long.valueOf(matcher.group(2));
            if (!redisTemplate.hasKey(qq + "+" + event.getUserId())) {
                String msg = MsgUtils.builder().at(event.getUserId()).text("对方还没有和你求婚呢。").build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
                return returnType.IGNORE_FALSE(event.getMessageId());
            }
            if ((int) redisTemplate.opsForValue().get(qq + "+" + event.getUserId()) == 0) {
                wifeMapper.marry(event.getUserId(), qq);
            } else {
                wifeMapper.marry(qq, event.getUserId());
            }
            String msg = MsgUtils.builder().at(qq).img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + qq + "&s=640"))
                    .at(event.getUserId()).img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + event.getUserId() + "&s=640"))
                    .text("恭喜你们成功结为夫妻,祝你们百年好合。").build();
            bot.sendGroupMsg(event.getGroupId(), msg, false);
            redisTemplate.delete(qq + "+" + event.getUserId());
        }
        pattern = Pattern.compile("\\[CQ:at,qq=(\\d+)]\\s*我拒绝|我拒绝\\[CQ:at,qq=(\\d+)]");
        matcher = pattern.matcher(event.getMessage());
        if (matcher.matches()) {
            Long qq = Long.valueOf(matcher.group(1));
            if (!redisTemplate.hasKey(qq + "+" + event.getUserId())) {
                String msg = MsgUtils.builder().at(event.getUserId()).text("对方还没有和你求婚呢。").build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
                return returnType.IGNORE_FALSE(event.getMessageId());
            }
            String msg = MsgUtils.builder().at(qq).text("对方拒绝了你的求婚，天涯何处无芳草，何必单恋一支花。").build();
            bot.sendGroupMsg(event.getGroupId(), msg, false);
            redisTemplate.delete(qq + "+" + event.getUserId());
        }
        marrySet = new HashSet<>(Arrays.asList("我的老婆", AT_BOT + "我的老婆"));
        if (marrySet.contains(event.getMessage())) {
            Wife wife = wifeMapper.selectWife(event.getUserId());
            if (wife != null) {
                double husbandProbability = 50 - (double) wife.getHusbandFavorAbility() / 100;
                if (husbandProbability < 0) husbandProbability = 0.0;
                double wifeProbability = 50 - (double) wife.getWifeFavorAbility() / 100;
                if (wifeProbability < 0) wifeProbability = 0.0;
                StrangerInfoResp wifeInfo = bot.getStrangerInfo(wife.getWife(), true).getData();
                String msg = MsgUtils.builder().at(event.getUserId()).text("你的老婆是").at(wifeInfo.getUserId())
                        .img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + wifeInfo.getUserId() + "&s=640"))
                        .text("她对你的好感度是" + wife.getWifeFavorAbility() + "。\n")
                        .text("你对她好感度是" + wife.getHusbandFavorAbility() + "。\n")
                        .text("你们的结婚时间是" + wife.getMarryTime() + "。\n")
                        .text("她被抢走的概率为" + wifeProbability + "%。\n")
                        .text("你被抢走的概率为" + husbandProbability + "%。").build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
                return returnType.IGNORE_FALSE(event.getMessageId());
            } else {
                wife = wifeMapper.selectHusband(event.getUserId());
                if (wife != null) {
                    double husbandProbability = 50 - (double) wife.getHusbandFavorAbility() / 100;
                    if (husbandProbability < 0) husbandProbability = 0.0;
                    double wifeProbability = 50 - (double) wife.getWifeFavorAbility() / 100;
                    if (wifeProbability < 0) wifeProbability = 0.0;
                    StrangerInfoResp husbandInfo = bot.getStrangerInfo(wife.getHusband(), true).getData();
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
                return returnType.IGNORE_FALSE(event.getMessageId());
            }
        }
        marrySet = new HashSet<>(Arrays.asList("我的老公", AT_BOT + "我的老公"));
        if (marrySet.contains(event.getMessage())) {
            Wife wife = wifeMapper.selectHusband(event.getUserId());
            if (wife != null) {
                double husbandProbability = 50 - (double) wife.getHusbandFavorAbility() / 100;
                if (husbandProbability < 0) husbandProbability = 0.0;
                double wifeProbability = 50 - (double) wife.getWifeFavorAbility() / 100;
                if (wifeProbability < 0) wifeProbability = 0.0;
                StrangerInfoResp husbandInfo = bot.getStrangerInfo(wife.getHusband(), true).getData();
                String msg = MsgUtils.builder().at(event.getUserId()).text("你的老公是").at(husbandInfo.getUserId())
                        .img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + husbandInfo.getUserId() + "&s=640"))
                        .text("他对你的好感度是" + wife.getHusbandFavorAbility() + "。\n")
                        .text("你对他好感度是" + wife.getWifeFavorAbility() + "。\n")
                        .text("你们的结婚时间是" + wife.getMarryTime() + "。\n")
                        .text("他被抢走的概率为" + husbandProbability + "%。\n")
                        .text("你被抢走的概率为" + wifeProbability + "%。").build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
                return returnType.IGNORE_FALSE(event.getMessageId());
            } else {
                wife = wifeMapper.selectWife(event.getUserId());
                if (wife != null) {
                    double husbandProbability = 50 - (double) wife.getHusbandFavorAbility() / 100;
                    if (husbandProbability < 0) husbandProbability = 0.0;
                    double wifeProbability = 50 - (double) wife.getWifeFavorAbility() / 100;
                    if (wifeProbability < 0) wifeProbability = 0.0;
                    StrangerInfoResp wifeInfo = bot.getStrangerInfo(wife.getWife(), true).getData();
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
                return returnType.IGNORE_FALSE(event.getMessageId());
            }
        }
        if (event.getMessage().contains("抢群友")) {
            String msg = MsgUtils.builder().at(event.getUserId()).text("你想做男还是做女。用抢老婆或者抢老公吧。").build();
            bot.sendGroupMsg(event.getGroupId(), msg, false);
            return returnType.IGNORE_FALSE(event.getMessageId());
        }
        pattern = Pattern.compile("\\[CQ:at,qq=(\\d+)]\\s*抢老婆|抢老婆\\[CQ:at,qq=(\\d+)]");
        matcher = pattern.matcher(event.getMessage());
        if (matcher.matches()) {
            if (isMarry(bot, event)) {
                return returnType.IGNORE_FALSE(event.getMessageId());
            }
            User user = usersMapper.selectUser(event.getUserId());
            if (user.getMoney() < 100) {
                String msg = MsgUtils.builder().at(event.getUserId()).text("你积分不够了!你只有" + user.getMoney() + "积分!攒够100积分再来吧!").build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
                return returnType.IGNORE_FALSE(event.getMessageId());
            }
            Long qq = Long.parseLong(matcher.group(1) != null ? matcher.group(1) : matcher.group(2));
            Wife wife = wifeMapper.selectHusband(qq);
            Random random = new Random();
            double randomProbability = random.nextDouble() * 100;
            int money = random.nextInt(50) + 50;
            if (wife != null) {
                double wifeProbability = 50 - (double) wife.getWifeFavorAbility() / 100;
                if (randomProbability <= wifeProbability) {
                    wifeMapper.deleteWife(qq);
                    wifeMapper.marry(event.getUserId(), qq);
                    moneyMapper.deductionMoney(event.getUserId(), money);
                    String msg = MsgUtils.builder().at(event.getUserId()).text("恭喜你花费" + money + "积分抢到了")
                            .at(wife.getHusband()).text("的老婆!\n")
                            .img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + qq + "&s=640"))
                            .text("她现在是你的老婆了!").build();
                    bot.sendGroupMsg(event.getGroupId(), msg, false);
                    msg = MsgUtils.builder().at(wife.getHusband()).text("你的老婆不要你了!和别人跑掉了!").build();
                    bot.sendGroupMsg(wife.getHusband(), msg, false);
                } else {
                    moneyMapper.deductionMoney(event.getUserId(), money);
                    String msg = MsgUtils.builder().at(event.getUserId()).text("抢老婆失败了,对方报警," + money + "积分打水漂了。").build();
                    bot.sendGroupMsg(event.getGroupId(), msg, false);
                }
            } else if ((wife = wifeMapper.selectWife(qq)) != null) {
                double wifeProbability = 50 - (double) wife.getHusbandFavorAbility() / 100;
                if (randomProbability <= wifeProbability) {
                    wifeMapper.deleteWife(qq);
                    wifeMapper.marry(event.getUserId(), Long.valueOf(qq));
                    moneyMapper.deductionMoney(event.getUserId(), money);
                    String msg = MsgUtils.builder().at(event.getUserId()).text("恭喜你花费" + money + "积分抢到了")
                            .at(wife.getWife()).text("的老公!\n")
                            .img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + qq + "&s=640"))
                            .text("她现在是你的老婆了!").build();
                    bot.sendGroupMsg(event.getGroupId(), msg, false);
                    msg = MsgUtils.builder().at(wife.getWife()).text("你的老公不要你了!和别人跑掉了!").build();
                    bot.sendGroupMsg(wife.getHusband(), msg, false);
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
        pattern = Pattern.compile("\\[CQ:at,qq=(\\d+)]\\s*抢老公|抢老公\\[CQ:at,qq=(\\d+)]");
        matcher = pattern.matcher(event.getMessage());
        if (matcher.matches()) {
            if (isMarry(bot, event)) {
                return returnType.IGNORE_FALSE(event.getMessageId());
            }
            User user = usersMapper.selectUser(event.getUserId());
            if (user.getMoney() < 100) {
                String msg = MsgUtils.builder().at(event.getUserId()).text("你积分不够了!你只有" + user.getMoney() + "积分!攒够100积分再来吧!").build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
                return returnType.IGNORE_FALSE(event.getMessageId());
            }
            Long qq = Long.parseLong(matcher.group(1) != null ? matcher.group(1) : matcher.group(2));
            Wife wife = wifeMapper.selectHusband(qq);
            Random random = new Random();
            double randomProbability = random.nextDouble() * 100;
            int money = random.nextInt(50) + 50;
            if (wife != null) {
                double wifeProbability = 50 - (double) wife.getWifeFavorAbility() / 100;
                if (randomProbability <= wifeProbability) {
                    wifeMapper.deleteWife(qq);
                    wifeMapper.marry(qq, event.getUserId());
                    moneyMapper.deductionMoney(event.getUserId(), money);
                    String msg = MsgUtils.builder().at(event.getUserId()).text("恭喜你花费" + money + "积分抢到了")
                            .at(wife.getHusband()).text("的老婆!\n")
                            .img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + qq + "&s=640"))
                            .text("他现在是你的老公了!").build();
                    bot.sendGroupMsg(event.getGroupId(), msg, false);
                    msg = MsgUtils.builder().at(wife.getHusband()).text("你的老婆不要你了!和别人跑掉了!").build();
                    bot.sendGroupMsg(wife.getHusband(), msg, false);
                } else {
                    moneyMapper.deductionMoney(event.getUserId(), money);
                    String msg = MsgUtils.builder().at(event.getUserId()).text("抢老公失败了,对方报警," + money + "积分打水漂了。").build();
                    bot.sendGroupMsg(event.getGroupId(), msg, false);
                }
            } else if ((wife = wifeMapper.selectWife(Long.valueOf(qq))) != null) {
                double wifeProbability = 50 - (double) wife.getHusbandFavorAbility() / 100;
                if (randomProbability <= wifeProbability) {
                    wifeMapper.deleteWife(qq);
                    wifeMapper.marry(Long.valueOf(qq), event.getUserId());
                    moneyMapper.deductionMoney(event.getUserId(), money);
                    String msg = MsgUtils.builder().at(event.getUserId()).text("恭喜你花费" + money + "积分抢到了")
                            .at(wife.getWife()).text("的老公!\n")
                            .img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + qq + "&s=640"))
                            .text("他现在是你的老公了!").build();
                    bot.sendGroupMsg(event.getGroupId(), msg, false);
                    msg = MsgUtils.builder().at(wife.getWife()).text("你的老公不要你了!和别人跑掉了!").build();
                    bot.sendGroupMsg(wife.getHusband(), msg, false);
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
            if (!isMarry(event.getUserId())) {
                String msg = MsgUtils.builder().at(event.getUserId()).text("你还没有伴侣,你离个锤子!").build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
            }
            Long wife = wifeMapper.isHusband(event.getUserId());
            if (wife == null) wife = wifeMapper.isWife(event.getUserId());
            wifeMapper.deleteWife(event.getUserId());
            String msg = MsgUtils.builder().at(event.getUserId()).text("你与").at(wife).text("的婚约已解除!\n")
                    .text("没想到你们会走到这一步，祝你们各自安好。").build();
            bot.sendGroupMsg(event.getGroupId(), msg, false);
        }
        pattern = Pattern.compile("\\[CQ:at,qq=(\\d+)]\\s*强暴|强暴\\[CQ:at,qq=(\\d+)]");
        matcher = pattern.matcher(event.getMessage());
        if (matcher.matches()) {
            int time;
            if (!redisTemplate.hasKey("rape:" + event.getUserId())){
                time = 0;
            } else {
                time = (int) redisTemplate.opsForValue().get("rape:" + event.getUserId());
            }
            if (time == 5) {
                String msg = MsgUtils.builder().at(event.getUserId()).text("停下吧!再强暴别人你会死掉的!").build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
                return returnType.IGNORE_FALSE(event.getMessageId());
            }
            redisTemplate.opsForValue().set("rape:" + event.getUserId(), time + 1, 12, TimeUnit.HOURS);
            Random random = new Random();
            Long qq = Long.valueOf(matcher.group(1) != null ? matcher.group(1) : matcher.group(2));
            String msg = "";
            if (qq.equals(wifeMapper.isWife(event.getUserId())) || qq.equals(wifeMapper.isHusband(event.getUserId()))) {
                int i = random.nextInt(50) + 100;
                if (wifeMapper.isWife(event.getUserId()) != null) {
                    switch (random.nextInt(5)) {
                        case 0:
                        case 1:
                            wifeMapper.updateHusbandResponsive(qq, i);
                            msg = MsgUtils.builder().at(event.getUserId()).text("你成功强暴了你的老公").at(qq).text("!")
                                    .img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + qq + "&s=640"))
                                    .at(event.getUserId()).text("你的老公很幸福，他对你的好感度+" + i).build();
                            break;
                        case 2:
                        case 3:
                            wifeMapper.updateHusbandResponsive(qq, -i);
                            msg = MsgUtils.builder().at(event.getUserId()).text("你成功强暴了你的老公").at(qq).text("!")
                                    .img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + qq + "&s=640"))
                                    .at(event.getUserId()).text("你的老公觉得你很粗鲁，他对你的好感度-" + i).build();
                            break;
                        case 4:
                            wifeMapper.deleteWife(qq);
                            msg = MsgUtils.builder().at(event.getUserId()).text("你成功强暴了你的老公").at(qq).text("!")
                                    .img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + qq + "&s=640"))
                                    .at(event.getUserId()).text("你的老公非常伤心,和你离婚了!").build();
                            break;
                    }
                    bot.sendGroupMsg(event.getGroupId(), msg, false);
                    return returnType.IGNORE_FALSE(event.getMessageId());
                } else {
                    switch (random.nextInt(5)) {
                        case 0:
                        case 1:
                            wifeMapper.updateWifeResponsive(qq, i);
                            msg = MsgUtils.builder().at(event.getUserId()).text("你成功强暴了你的老婆").at(qq).text("!")
                                    .img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + qq + "&s=640"))
                                    .at(event.getUserId()).text("你的老婆很幸福，她对你的好感度+" + i).build();
                            break;
                        case 2:
                        case 3:
                            wifeMapper.updateWifeResponsive(qq, -i);
                            msg = MsgUtils.builder().at(event.getUserId()).text("你成功强暴了你的老婆").at(qq).text("!")
                                    .img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + qq + "&s=640"))
                                    .at(event.getUserId()).text("你的老婆觉得你很粗鲁，她对你的好感度-" + i).build();
                            break;
                        case 4:
                            wifeMapper.deleteWife(qq);
                            msg = MsgUtils.builder().at(event.getUserId()).text("你成功强暴了你的老婆").at(qq).text("!")
                                    .img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + qq + "&s=640"))
                                    .at(event.getUserId()).text("你的老婆非常伤心,和你离婚了!").build();
                            break;
                    }
                    bot.sendGroupMsg(event.getGroupId(), msg, false);
                    return returnType.IGNORE_FALSE(event.getMessageId());
                }
            }
            if (random.nextInt(5) == 0) {
                if (isMarry(event.getUserId())) {
                    if (isMarry( qq)) {
                        switch (random.nextInt(4)) {
                            case 0:
                                wifeMapper.deleteWife(qq);
                                if (wifeMapper.isWife(qq) != null) {
                                    msg = MsgUtils.builder().at(event.getUserId()).text("你成功强暴了").at(qq).text("!")
                                            .img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + qq + "&s=640"))
                                            .at(qq).text("感觉对不起她的老公").at(wifeMapper.isWife(qq)).text(",和她的老公离婚了。").build();
                                } else {
                                    msg = MsgUtils.builder().at(event.getUserId()).text("你成功强暴了").at(qq).text("!")
                                            .img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + qq + "&s=640"))
                                            .at(qq).text("感觉对不起他的老婆").at(wifeMapper.isHusband(qq)).text(",和他的老婆离婚了。").build();
                                }
                                break;
                            case 1:
                                wifeMapper.deleteWife(event.getUserId());
                                if (wifeMapper.isWife(event.getUserId()) != null) {
                                    msg = MsgUtils.builder().at(event.getUserId()).text("你成功强暴了").at(qq).text("!")
                                            .img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + qq + "&s=640"))
                                            .at(qq).text("你被你的老公").at(wifeMapper.isWife(event.getUserId())).text("发现了,你的老公和你离婚了。").build();
                                } else {
                                    msg = MsgUtils.builder().at(event.getUserId()).text("你成功强暴了").at(qq).text("!")
                                            .img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + qq + "&s=640"))
                                            .at(qq).text("你被你的老婆").at(wifeMapper.isHusband(event.getUserId())).text("发现了,你的老婆和你离婚了。").build();
                                }
                                break;
                            case 3:
                                int i = random.nextInt(100);
                                if (random.nextInt(2) == 0) {
                                    if (wifeMapper.isWife(qq) != null) {
                                        wifeMapper.updateWifeResponsive(wifeMapper.isWife(qq), -i);
                                        msg = MsgUtils.builder().at(event.getUserId()).text("你成功强暴了").at(qq).text("!")
                                                .img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + qq + "&s=640"))
                                                .at(qq).text("感觉你比她的老公厉害多了，她对她老公的好感度-" + i).build();
                                    } else {
                                        wifeMapper.updateHusbandResponsive(wifeMapper.isHusband(qq), -i);
                                        msg = MsgUtils.builder().at(event.getUserId()).text("你成功强暴了").at(qq).text("!")
                                                .img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + qq + "&s=640"))
                                                .at(qq).text("感觉你比他老婆厉害多了，他对他老婆的好感度-" + i).build();
                                    }
                                } else {
                                    if (wifeMapper.isWife(event.getUserId()) != null) {
                                        wifeMapper.updateWifeResponsive(wifeMapper.isWife(qq), i);
                                        msg = MsgUtils.builder().at(event.getUserId()).text("你成功强暴了").at(qq).text("!")
                                                .img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + qq + "&s=640"))
                                                .at(qq).text("感觉你没她的老公厉害，她对她老公的好感度+" + i).build();
                                    } else {
                                        wifeMapper.updateHusbandResponsive(wifeMapper.isHusband(qq), i);
                                        msg = MsgUtils.builder().at(event.getUserId()).text("你成功强暴了").at(qq).text("!")
                                                .img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + qq + "&s=640"))
                                                .at(qq).text("感觉你没他老婆厉害，他对他老婆的好感度+" + i).build();
                                    }
                                }
                                break;
                            case 2:
                                wifeMapper.deleteWife(event.getUserId());
                                wifeMapper.deleteWife(qq);
                                if (random.nextInt(2) == 0) {
                                    wifeMapper.marry(event.getUserId(), qq);
                                } else {
                                    wifeMapper.marry(qq, event.getUserId());
                                }
                                msg = MsgUtils.builder().at(event.getUserId()).text("你成功强暴了").at(qq).text("!")
                                        .img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + qq + "&s=640"))
                                        .at(qq).text("选择和你私奔。恭喜你!").build();
                                break;
                        }
                        bot.sendGroupMsg(event.getGroupId(), msg, false);
                        return returnType.IGNORE_FALSE(event.getMessageId());
                    } else {
                        switch (random.nextInt(2)) {
                            case 0:
                                wifeMapper.deleteWife(event.getUserId());
                                if (random.nextInt(2) == 0) {
                                    wifeMapper.marry(event.getUserId(), qq);
                                } else {
                                    wifeMapper.marry(qq, event.getUserId());
                                }
                                msg = MsgUtils.builder().at(event.getUserId()).text("你成功强暴了").at(qq).text("!")
                                        .img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + qq + "&s=640"))
                                        .at(qq).text("选择和你私奔。恭喜你!").build();
                                break;
                            case 1:
                                wifeMapper.deleteWife(event.getUserId());
                                if (wifeMapper.isWife(event.getUserId()) != null) {
                                    msg = MsgUtils.builder().at(event.getUserId()).text("你成功强暴了").at(qq).text("!")
                                            .img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + qq + "&s=640"))
                                            .at(qq).text("你被你的老公").at(wifeMapper.isWife(event.getUserId())).text("发现了,你的老公和你离婚了。").build();
                                } else {
                                    msg = MsgUtils.builder().at(event.getUserId()).text("你成功强暴了").at(qq).text("!")
                                            .img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + qq + "&s=640"))
                                            .at(qq).text("你被你的老婆").at(wifeMapper.isHusband(event.getUserId())).text("发现了,你的老婆和你离婚了。").build();
                                }
                                break;
                        }
                        bot.sendGroupMsg(event.getGroupId(), msg, false);
                        return returnType.IGNORE_FALSE(event.getMessageId());
                    }
                } else {
                    if (isMarry(qq)) {
                        switch (random.nextInt(3)) {
                            case 0:
                                wifeMapper.deleteWife(qq);
                                if (wifeMapper.isWife(qq) != null) {
                                    msg = MsgUtils.builder().at(event.getUserId()).text("你成功强暴了").at(qq).text("!")
                                            .img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + qq + "&s=640"))
                                            .at(qq).text("感觉对不起她的老公").at(wifeMapper.isWife(qq)).text(",和她的老公离婚了。").build();
                                } else {
                                    msg = MsgUtils.builder().at(event.getUserId()).text("你成功强暴了").at(qq).text("!")
                                            .img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + qq + "&s=640"))
                                            .at(qq).text("感觉对不起他的老婆").at(wifeMapper.isHusband(qq)).text(",和他的老婆离婚了。").build();
                                }
                                break;
                            case 1:
                                int i = random.nextInt(100);
                                if (random.nextInt(2) == 0) {
                                    if (wifeMapper.isWife(qq) != null) {
                                        wifeMapper.updateWifeResponsive(wifeMapper.isWife(qq), -i);
                                        msg = MsgUtils.builder().at(event.getUserId()).text("你成功强暴了").at(qq).text("!")
                                                .img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + qq + "&s=640"))
                                                .at(qq).text("感觉你比她的老公厉害多了，她对她老公的好感度-" + i).build();
                                    } else {
                                        wifeMapper.updateHusbandResponsive(wifeMapper.isHusband(qq), -i);
                                        msg = MsgUtils.builder().at(event.getUserId()).text("你成功强暴了").at(qq).text("!")
                                                .img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + qq + "&s=640"))
                                                .at(qq).text("感觉你比他老婆厉害多了，他对他老婆的好感度-" + i).build();
                                    }
                                } else {
                                    if (wifeMapper.isWife(event.getUserId()) != null) {
                                        wifeMapper.updateWifeResponsive(wifeMapper.isWife(qq), i);
                                        msg = MsgUtils.builder().at(event.getUserId()).text("你成功强暴了").at(qq).text("!")
                                                .img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + qq + "&s=640"))
                                                .at(qq).text("感觉你没她的老公厉害，她对她老公的好感度+" + i).build();
                                    } else {
                                        wifeMapper.updateHusbandResponsive(wifeMapper.isHusband(qq), i);
                                        msg = MsgUtils.builder().at(event.getUserId()).text("你成功强暴了").at(qq).text("!")
                                                .img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + qq + "&s=640"))
                                                .at(qq).text("感觉你没他老婆厉害，他对他老婆的好感度+" + i).build();
                                    }
                                }
                                break;
                            case 2:
                                wifeMapper.deleteWife(qq);
                                if (random.nextInt(2) == 0) {
                                    wifeMapper.marry(event.getUserId(), qq);
                                } else {
                                    wifeMapper.marry(qq, event.getUserId());
                                }
                                msg = MsgUtils.builder().at(event.getUserId()).text("你成功强暴了").at(qq).text("!")
                                        .img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + qq + "&s=640"))
                                        .at(qq).text("选择和你私奔。恭喜你!").build();
                                break;
                        }
                        bot.sendGroupMsg(event.getGroupId(), msg, false);
                        return returnType.IGNORE_FALSE(event.getMessageId());
                    } else {
                        if (random.nextInt(2) == 0) {
                            wifeMapper.deleteWife(event.getUserId());
                            if (random.nextInt(2) == 0) {
                                wifeMapper.marry(event.getUserId(), qq);
                            } else {
                                wifeMapper.marry(qq, event.getUserId());
                            }
                            msg = MsgUtils.builder().at(event.getUserId()).text("你成功强暴了").at(qq).text("!")
                                    .img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + qq + "&s=640"))
                                    .at(qq).text("选择和你私奔。恭喜你!").build();
                        } else {
                            msg = MsgUtils.builder().at(event.getUserId()).text("你成功强暴了").at(qq).text("!")
                                    .img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + qq + "&s=640"))
                                    .text("你提起裤子转身就走了").build();
                        }
                        bot.sendGroupMsg(event.getGroupId(), msg, false);
                        return returnType.IGNORE_FALSE(event.getMessageId());
                    }
                }
            } else {
                switch (random.nextInt(2)) {
                    case 0:
                        int i = random.nextInt(10000) + 1000;
                        moneyMapper.addMoney(qq, i);
                        moneyMapper.deductionMoney(event.getUserId(), i);
                        msg = MsgUtils.builder().at(event.getUserId()).text("你成功强暴了").at(qq).text("!")
                                .img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + qq + "&s=640"))
                                .at(qq).text("报警,你赔偿了").at(qq).text(i + "积分").build();
                        if (moneyMapper.selectMoney(event.getUserId()) < -30000) {
                            // 获取绝对值
                            int money = Math.abs(moneyMapper.selectMoney(event.getUserId()));
                            int workTime = money / 10 + 1;
                            redisTemplate.opsForValue().set("work:SewingMachine:" + event.getUserId(), event.getUserId(), workTime, TimeUnit.MINUTES);
                            msg = MsgUtils.builder().at(event.getUserId()).text("因为你欠债太多，你被抓去踩缝纫机了。\n")
                                    .text("只需要" + workTime + "分钟，就能把债务还清哦。").build();
                        }
                        break;
                    case 1:
                        redisTemplate.opsForValue().set("join:" + event.getUserId(), 1, 30, TimeUnit.MINUTES);
                        msg = MsgUtils.builder().at(event.getUserId()).text("你成功强暴了").at(qq).text("!")
                                .img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + qq + "&s=640"))
                                .at(qq).text("报警,你被关进大牢30分钟").build();
                        break;
                }
                bot.sendGroupMsg(event.getGroupId(), msg, false);
                if (random.nextInt(2) == 0) {
                    if (isMarry(event.getUserId())) {
                        wifeMapper.deleteWife(event.getUserId());
                        if (wifeMapper.isWife(event.getUserId()) != null) {
                            msg = MsgUtils.builder().at(event.getUserId()).text("你的老公和你离婚了").text("!").build();
                            bot.sendGroupMsg(event.getGroupId(), msg, false);
                        } else {
                            msg = MsgUtils.builder().at(event.getUserId()).text("你的老婆和你离婚了").text("!").build();
                            bot.sendGroupMsg(event.getGroupId(), msg, false);
                        }
                    }
                }
                return returnType.IGNORE_FALSE(event.getMessageId());
            }
        }
        return MESSAGE_IGNORE;
    }

    private boolean isMarry(Bot bot, GroupMessageEvent event) {
        Long companion = wifeMapper.isWife(event.getUserId());
        if (companion != null) {
            StrangerInfoResp wifeInfo = bot.getStrangerInfo(companion, true).getData();
            String msg = MsgUtils.builder().at(event.getUserId()).text("你已经有老公了!你的老公是").at(wifeInfo.getUserId())
                    .img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + wifeInfo.getUserId() + "&s=640"))
                    .text("不要朝三暮四哦。").build();
            bot.sendGroupMsg(event.getGroupId(), msg, false);
            return true;
        }
        companion = wifeMapper.isHusband(event.getUserId());
        if (companion != null) {
            StrangerInfoResp husbandInfo = bot.getStrangerInfo(companion, true).getData();
            String msg = MsgUtils.builder().at(event.getUserId()).text("你已经有老婆了!你的老婆是").at(husbandInfo.getUserId())
                    .img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + husbandInfo.getUserId() + "&s=640"))
                    .text("不要朝三暮四哦。").build();
            bot.sendGroupMsg(event.getGroupId(), msg, false);
            return true;
        }
        return false;
    }

    private boolean isMarry(Long qq) {
        Long companion = wifeMapper.isWife(qq);
        if (companion != null) {
            return true;
        }
        companion = wifeMapper.isHusband(qq);
        if (companion != null) {
            return true;
        }
        return false;
    }
}
