package com.bot.yiyi.plugin;

import com.bot.yiyi.Pojo.ReturnType;
import com.bot.yiyi.Pojo.Role;
import com.bot.yiyi.config.AIConfig;
import com.bot.yiyi.config.BotConfig;
import com.bot.yiyi.mapper.AIMapper;
import com.bot.yiyi.utils.RedisConversationService;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.response.GroupMemberInfoResp;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.bot.yiyi.Pojo.AtBot.AT_BOT;

@Component
public class HumanPlugin extends BasePlugin {

    private final String PLUGIN_NAME = "HumanPlugin";
    @Autowired
    private RedisConversationService redisConversationService;
    @Autowired
    private AIMapper aiMapper;
    @Autowired
    private ReturnType returnType;

    private static final Random RANDOM = new Random();
    private static final Pattern DIGIT_PATTERN = Pattern.compile("\\d+");
    private static final Pattern AT_PATTERN = Pattern.compile("\\[CQ:at,qq=(\\d+)]");
    private static final Pattern REPLY_PATTERN = Pattern.compile("\\[CQ:reply,id=\\d+]");

    private ChatClient chatClient;
    private final ChatClient.Builder builder;
    private BotConfig botConfig;

    public HumanPlugin(ChatClient chatClient, ChatClient.Builder builder) {
        this.builder = builder;
        this.chatClient = chatClient;
        createChatClient();
    }

    @Override
    public int onGroupMessage(Bot bot, GroupMessageEvent event) {

        if (shouldIgnore(event, PLUGIN_NAME)) return MESSAGE_IGNORE;

        String msg = event.getMessage();
        long groupId = event.getGroupId();
        long userId = event.getUserId();
        int messageId = event.getMessageId();

        if (!msg.contains(AT_BOT)) {
            // 非@机器人的消息只处理特定情况
            if (returnType.getMatch(messageId)) {
                return handleAIResponse(bot, event);
            }
            return MESSAGE_IGNORE;
        }

        // @机器人的消息，拆分指令处理
        if (botConfig.isGroupAdmin(event, bot)) {
            if (msg.contains("开启伪人模式")) {
                aiMapper.updateHumanState(1, groupId);
                sendGroupAt(bot, groupId, userId, "已开启伪人模式");
                return returnType.IGNORE_FALSE(messageId);
            }
            if (msg.contains("关闭伪人模式")) {
                aiMapper.updateHumanState(0, groupId);
                sendGroupAt(bot, groupId, userId, "已关闭伪人模式");
                return returnType.IGNORE_FALSE(messageId);
            }
            if (msg.contains("设置伪人概率")) {
                return handleSetProbability(bot, event);
            }
        } else {
            sendGroupAt(bot, groupId, userId, "只有群主和管理员可以设置哦~");
            return returnType.IGNORE_FALSE(messageId);
        }

        return MESSAGE_IGNORE;
    }

    private int handleSetProbability(Bot bot, GroupMessageEvent event) {
        String msg = event.getMessage().replace(AT_BOT, "");
        Matcher matcher = DIGIT_PATTERN.matcher(msg);
        long groupId = event.getGroupId();
        long userId = event.getUserId();
        int messageId = event.getMessageId();

        if (matcher.find()) {
            int pro = Integer.parseInt(matcher.group());
            if (pro < 1 || pro > 100) {
                sendGroupAt(bot, groupId, userId, "概率范围1-100");
            } else {
                aiMapper.updatePro(pro, groupId);
                sendGroupAt(bot, groupId, userId, "已设置概率为" + pro);
            }
        }
        return returnType.IGNORE_FALSE(messageId);
    }

    private int handleAIResponse(Bot bot, GroupMessageEvent event) {
        long groupId = event.getGroupId();
        long userId = event.getUserId();
        int messageId = event.getMessageId();

        GroupMemberInfoResp userData = bot.getGroupMemberInfo(groupId, userId, true).getData();
        String nickName = userData.getCard() == null || userData.getCard().isEmpty() ? userData.getNickname() : userData.getCard();

        String message = REPLY_PATTERN.matcher(event.getMessage()).replaceAll("").trim();

        // 处理@其他人转成昵称格式
        Matcher atMatcher = AT_PATTERN.matcher(message);
        List<String> qqList = new ArrayList<>();
        while (atMatcher.find()) {
            qqList.add(atMatcher.group(1));
        }

        for (String qq : qqList) {
            if (qq.equals(String.valueOf(bot.getSelfId()))) {
                message = message.replace(AT_BOT, "");
            } else {
                GroupMemberInfoResp data = bot.getGroupMemberInfo(groupId, Long.parseLong(qq), false).getData();
                String name = data.getCard() == null || data.getCard().isEmpty() ? data.getNickname() : data.getCard();
                message = message.replace("[CQ:at,qq=" + qq + "]", "@" + name + " ");
            }
        }

        nickName += "(" + userId + "):";

        // 保存历史消息，限制20条
        redisConversationService.addMessage(groupId + "msg", nickName + message, 20);

        int pro = aiMapper.selectPro(groupId);
        if (!isHit(pro)) {
            return returnType.IGNORE_FALSE(messageId);
        }

        String historyMessage = redisConversationService.getHistory(groupId + "msg");

        AIConfig.setRole(Role.HUMAN);
        createChatClient();

        Flux<String> content = chatClient.prompt(historyMessage).user(message).stream().content();

        content.collectList()
                .map(list -> String.join(" ", list).replaceAll("\\s*", ""))
                .subscribe(s -> {
                    int randomNum = RANDOM.nextInt(100);
                    String msgToSend;
                    if (randomNum < 50) {
                        msgToSend = MsgUtils.builder().text(s).build();
                    } else if (randomNum < 75) {
                        msgToSend = MsgUtils.builder().at(userId).text(s).build();
                    } else {
                        msgToSend = MsgUtils.builder().reply(messageId).at(userId).text(s).build();
                    }
                    bot.sendGroupMsg(groupId, msgToSend, false);
                }, error -> {
                    // 简单日志打印，可扩展为日志框架
                    System.err.println("AI回复异常: " + error.getMessage());
                });

        return MESSAGE_IGNORE;
    }

    private static boolean isHit(int percent) {
        if (percent <= 0) return false;
        if (percent >= 100) return true;
        return RANDOM.nextInt(100) < percent;
    }

    private void sendGroupAt(Bot bot, long groupId, long userId, String text) {
        String message = MsgUtils.builder().at(userId).text(text).build();
        bot.sendGroupMsg(groupId, message, false);
    }

    private void createChatClient() {
        this.chatClient = builder.defaultSystem(AIConfig.getRole())
                .build();
    }
}