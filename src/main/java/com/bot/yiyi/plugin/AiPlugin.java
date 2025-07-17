package com.bot.yiyi.plugin;

import com.bot.yiyi.Pojo.Model;
import com.bot.yiyi.Pojo.ReturnType;
import com.bot.yiyi.Pojo.Role;
import com.bot.yiyi.config.AIConfig;
import com.bot.yiyi.config.BotConfig;
import com.bot.yiyi.mapper.AIMapper;
import com.bot.yiyi.utils.AtUtil;
import com.bot.yiyi.utils.RedisConversationService;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.action.response.GroupMemberInfoResp;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.MessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.bot.yiyi.Pojo.AtBot.AT_BOT;

@Component
public class AiPlugin extends BotPlugin {

    private final ChatClient.Builder builder;
    private ChatClient chatClient;

    @Autowired private AIMapper aiMapper;
    @Autowired private BotConfig botConfig;
    @Autowired private RedisConversationService redisConversationService;
    @Autowired private ReturnType returnType;

    private static final Map<String, Integer> ROLE_MAP = new LinkedHashMap<>();
    private static final Pattern IMAGE_PATTERN = Pattern.compile("^(.*?)(\\[CQ:image,[^]]*?url=([^,\\]]+)])(.*)?");

    static {
        ROLE_MAP.put("默认", 0);
        ROLE_MAP.put("老婆", 1);
        ROLE_MAP.put("女仆", 2);
        ROLE_MAP.put("魅魔", 3);
        ROLE_MAP.put("贴吧老哥", 4);
        ROLE_MAP.put("美少女", 5);
        ROLE_MAP.put("傲娇猫娘", 6);
        ROLE_MAP.put("白丝猫娘", 7);
        ROLE_MAP.put("病娇老婆", 8);
        ROLE_MAP.put("病娇学姐", 9);
        ROLE_MAP.put("雌小鬼", 10);
        ROLE_MAP.put("卡芙卡", 11);
        ROLE_MAP.put("爱莉希雅", 12);
        ROLE_MAP.put("花火", 13);
        ROLE_MAP.put("女儿", 14);
    }

    public AiPlugin(ChatClient chatClient, ChatClient.Builder builder) {
        this.chatClient = chatClient;
        this.builder = builder;
        createChatClient();
    }

    @Override
    public int onPrivateMessage(Bot bot, PrivateMessageEvent event) {
        if (!returnType.getMatch(event.getMessageId())) return returnType.BLOCK(event.getMessageId());
        String msg = event.getMessage();

        if (msg.equals("清空记忆")) {
            redisConversationService.clearHistory(event.getUserId());
            bot.sendPrivateMsg(event.getUserId(), "记忆已清空。", true);
            return returnType.BLOCK(event.getMessageId());
        }

        if (msg.contains("切换角色") || msg.contains("当前角色")) {
            return handleRoleCommand(bot, event, false, 1, event.getMessageId());
        }

        return handleChatMessage(bot, event.getUserId(), msg, false, event.getMessageId());
    }

    @Override
    public int onGroupMessage(Bot bot, GroupMessageEvent event) {
        if (event.getGroupId() == 176282339L) return returnType.BLOCK(event.getMessageId());

        if (!AtUtil.isAt(bot.getLoginInfo().getData(), event)) return returnType.BLOCK(event.getMessageId());

        String msg = event.getMessage();
        long groupId = event.getGroupId();
        long userId = event.getUserId();
        int aiType = aiMapper.selectAiType(groupId);

        if (msg.contains("清空记忆")) {
            redisConversationService.clearHistory(aiType == 0 ? groupId : userId);
            bot.sendGroupMsg(groupId, "记忆已清空。", true);
            return returnType.BLOCK(event.getMessageId());
        }

        if (msg.contains("切换角色") || msg.contains("当前角色")) {
            return handleRoleCommand(bot, event, true, aiType, event.getMessageId());
        }

        if (msg.contains("切换模式群聊")) {
            if (!isAdmin(bot, event, aiType)) {
                return returnType.BLOCK(event.getMessageId());
            }
            aiMapper.updateAiType(0, groupId);
            bot.sendGroupMsg(groupId, "已切换为群聊模式。", true);
            return returnType.BLOCK(event.getMessageId());
        }
        if (msg.contains("切换模式个人")) {
            if (!isAdmin(bot, event, aiType)) {
                return returnType.BLOCK(event.getMessageId());
            }
            aiMapper.updateAiType(1, groupId);
            bot.sendGroupMsg(groupId, "已切换为个人模式。", true);
            return returnType.BLOCK(event.getMessageId());
        }
        if (msg.contains("当前模式")) {
            String mode = aiType == 0 ? "群聊" : "个人";
            bot.sendGroupMsg(groupId, "当前模式为" + mode + "模式。", true);
            return returnType.BLOCK(event.getMessageId());
        }

        if (returnType.getMatch(event.getMessageId())) {
            return handleChatMessage(bot, aiType == 0 ? groupId : userId, cleanGroupMessage(bot, event), true, event.getMessageId());
        }
        return returnType.BLOCK(event.getMessageId());
    }

    private int handleRoleCommand(Bot bot, MessageEvent event, boolean isGroup, int aiType, int messageId) {
        String msg = event.getMessage();
        long id = isGroup ? ((GroupMessageEvent) event).getGroupId() : event.getUserId();
        for (Map.Entry<String, Integer> entry : ROLE_MAP.entrySet()) {
            if (msg.contains("切换角色" + entry.getKey())) {
                if (isAdmin(bot, event, aiType)) {
                    return returnType.BLOCK(messageId);
                }
                if (isGroup && aiType == 0) aiMapper.updateRole(id, entry.getValue());
                else aiMapper.updateUserRole(event.getUserId(), entry.getValue());

                redisConversationService.clearHistory(id);
                sendMsg(bot, id, isGroup, "依依已经切换到" + entry.getKey() + "啦。");
                return returnType.BLOCK(messageId);
            }
        }

        if (msg.contains("当前角色")) {
            int roleId = isGroup && aiType == 0 ? aiMapper.selectRole(id) : aiMapper.selectUserRole(event.getUserId());
            String roleName = ROLE_MAP.entrySet().stream().filter(e -> e.getValue() == roleId).map(Map.Entry::getKey).findFirst().orElse("未知角色");
            sendMsg(bot, id, isGroup, "当前角色为" + roleName + "。");
            return returnType.BLOCK(messageId);
        }

        sendMsg(bot, id, isGroup, "依依没有这个角色哦。");
        return returnType.BLOCK(messageId);
    }

    private int handleChatMessage(Bot bot, long sessionId, String msg, boolean isGroup, int replyId) {
        ParsedMessage parsed = parseMessage(msg);
        int roleType = isGroup ? aiMapper.selectRole(sessionId) : aiMapper.selectUserRole(sessionId);
        updateRoleById(roleType);

        Mono<String> context = getContext(parsed.text, sessionId);
        context.subscribe(s -> {
            String message = isGroup ? MsgUtils.builder().reply(replyId).at(sessionId).text(s).build() : s;
            if (isGroup) bot.sendGroupMsg(sessionId, message, false);
            else bot.sendPrivateMsg(sessionId, message, false);
        });
        return returnType.BLOCK(replyId);
    }

    private String cleanGroupMessage(Bot bot, GroupMessageEvent event) {
        String msg = event.getMessage().replaceAll("\\[CQ:reply,id=\\d+]", "").trim();
        Matcher matcher = Pattern.compile("\\[CQ:at,qq=(\\d+)]").matcher(msg);
        while (matcher.find()) {
            String qq = matcher.group(1);
            if (!qq.equals(String.valueOf(bot.getSelfId()))) {
                GroupMemberInfoResp data = bot.getGroupMemberInfo(event.getGroupId(), Long.parseLong(qq), false).getData();
                String name = (data.getCard() == null || data.getCard().isEmpty()) ? data.getNickname() : data.getCard();
                msg = msg.replace("[CQ:at,qq=" + qq + "]", name + "(群友的名字) ");
            } else {
                msg = msg.replace(AT_BOT, "");
            }
        }
        return msg;
    }

    private static class ParsedMessage {
        String text;
        String imageUrl;
    }

    private ParsedMessage parseMessage(String message) {
        ParsedMessage result = new ParsedMessage();
        Matcher matcher = IMAGE_PATTERN.matcher(message);
        if (matcher.find()) {
            result.text = (matcher.group(1) != null ? matcher.group(1) : "") + (matcher.group(4) != null ? matcher.group(4) : "");
            result.imageUrl = matcher.group(3);
        } else {
            result.text = message;
        }
        return result;
    }

    private void sendMsg(Bot bot, long id, boolean isGroup, String msg) {
        if (isGroup) bot.sendGroupMsg(id, msg, true);
        else bot.sendPrivateMsg(id, msg, true);
    }

    public synchronized void updateRole(Role newRole) {
        AIConfig.setRole(newRole);
        createChatClient();
    }

    public synchronized void updateModel(Model newModel) {
        AIConfig.setModel(newModel);
        createChatClient();
    }

    private void updateRoleById(int roleType) {
        switch (roleType) {
            case 0 -> updateRole(Role.DEFAULT);
            case 1 -> updateRole(Role.WIFE);
            case 2 -> updateRole(Role.MAID);
            case 3 -> updateRole(Role.SUCCUBUS);
            case 4 -> updateRole(Role.ATTACK);
            case 5 -> updateRole(Role.GIRL);
            case 6 -> updateRole(Role.TSUNDERE_CAT);
            case 7 -> updateRole(Role.CAT_GIRL);
            case 8 -> updateRole(Role.YANDERE_WIFE);
            case 9 -> updateRole(Role.YANDERE_SENIOR);
            case 10 -> updateRole(Role.FEMALE_IMP);
            case 11 -> updateRole(Role.KAFKA);
            case 12 -> updateRole(Role.ELYSIA);
            case 13 -> updateRole(Role.SPARKLE);
            case 14 -> updateRole(Role.DAUGHTER);
        }
    }

    private void createChatClient() {
        this.chatClient = builder.defaultSystem(AIConfig.getRole()).build();
    }

    @Retryable(retryFor = Exception.class, maxAttempts = 5, backoff = @Backoff(delay = 3000, multiplier = 0))
    private Mono<String> getContext(String message, Long sessionId) {
        redisConversationService.addUserMessage(sessionId, message);
        String history = redisConversationService.getFormattedHistory(sessionId);
        Flux<String> content = chatClient.prompt(history).user(message).stream().content();
        return content.collectList().map(list -> String.join(" ", list).replaceAll("\\s*", ""))
                .doOnNext(msg -> redisConversationService.addAssistantMessage(sessionId, msg));
    }

    private boolean isAdmin(Bot bot, MessageEvent event, int aiType) {
        if (aiType == 0) {
            String role = bot.getGroupMemberInfo(((GroupMessageEvent) event).getGroupId(), event.getUserId(), true).getData().getRole();
            return botConfig.isOwnerQQ(event.getUserId()) || role.equals("owner") || role.equals("admin");
        }
        return false;
    }
}
