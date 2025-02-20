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
import com.mikuac.shiro.dto.action.common.ActionData;
import com.mikuac.shiro.dto.action.response.GroupMemberInfoResp;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.bot.yiyi.plugin.RegisterPlugin.atBot;

@Component
public class AiPlugin extends BotPlugin {

    private ChatClient chatClient;
    private final ChatClient.Builder builder;
    @Autowired
    public AIMapper aiMapper;
    @Autowired
    private BotConfig botConfig;
    @Autowired
    private RedisConversationService redisConversationService;

    public AiPlugin(ChatClient chatClient, ChatClient.Builder builder) {
        this.chatClient = chatClient;
        this.builder = builder;
        createChatClient();
    }

    @Override
    public int onPrivateMessage(Bot bot, PrivateMessageEvent event) {
        if (!ReturnType.getMatch()) {
            return MESSAGE_BLOCK;
        }
        if (event.getMessage().equals("清空记忆")) {
            redisConversationService.clearHistory(event.getUserId());
            bot.sendPrivateMsg(event.getUserId(), "记忆已清空。", true);
            return ReturnType.BLOCK_FALSE();
        }
        if (event.getMessage().contains("切换角色") || event.getMessage().contains("当前角色")) {
            if (event.getMessage().contains("切换角色默认")) {
                aiMapper.updateUserRole(event.getUserId(), 0);
                redisConversationService.clearHistory(event.getUserId());
                bot.sendPrivateMsg(event.getUserId(), "依依已经切换到默认啦。", true);
                return ReturnType.BLOCK_FALSE();
            } else if (event.getMessage().contains("切换角色老婆")) {
                aiMapper.updateUserRole(event.getUserId(), 1);
                redisConversationService.clearHistory(event.getUserId());
                bot.sendPrivateMsg(event.getUserId(), "依依已经切换到老婆啦。", true);
                return ReturnType.BLOCK_FALSE();
            } else if (event.getMessage().contains("切换角色女仆")) {
                aiMapper.updateUserRole(event.getUserId(), 2);
                redisConversationService.clearHistory(event.getUserId());
                bot.sendPrivateMsg(event.getUserId(), "依依已经切换到女仆啦。", true);
                return ReturnType.BLOCK_FALSE();
            } else if (event.getMessage().contains("切换角色魅魔")) {
                aiMapper.updateUserRole(event.getUserId(), 3);
                redisConversationService.clearHistory(event.getUserId());
                bot.sendPrivateMsg(event.getUserId(), "依依已经切换到魅魔啦。", true);
                return ReturnType.BLOCK_FALSE();
            } else if (event.getMessage().contains("切换角色贴吧老哥")) {
                aiMapper.updateUserRole(event.getUserId(), 4);
                redisConversationService.clearHistory(event.getUserId());
                bot.sendPrivateMsg(event.getUserId(), "依依已经切换到贴吧老哥啦。", true);
                return ReturnType.BLOCK_FALSE();
            } else if (event.getMessage().contains("切换角色美少女")) {
                aiMapper.updateUserRole(event.getUserId(), 5);
                redisConversationService.clearHistory(event.getUserId());
                bot.sendPrivateMsg(event.getUserId(), "依依已经切换到美少女啦。", true);
                return ReturnType.BLOCK_FALSE();
            } else if (event.getMessage().contains("切换角色傲娇猫娘")) {
                aiMapper.updateUserRole(event.getUserId(), 6);
                redisConversationService.clearHistory(event.getUserId());
                bot.sendPrivateMsg(event.getUserId(), "依依已经切换到傲娇猫娘啦。", true);
                return ReturnType.BLOCK_FALSE();
            } else if (event.getMessage().contains("切换角色白丝猫娘")) {
                aiMapper.updateUserRole(event.getUserId(), 7);
                redisConversationService.clearHistory(event.getUserId());
                bot.sendPrivateMsg(event.getUserId(), "依依已经切换到白丝猫娘啦。", true);
                return ReturnType.BLOCK_FALSE();
            } else if (event.getMessage().contains("切换角色病娇老婆")) {
                aiMapper.updateUserRole(event.getUserId(), 8);
                redisConversationService.clearHistory(event.getUserId());
                bot.sendPrivateMsg(event.getUserId(), "依依已经切换到病娇老婆啦。", true);
                return ReturnType.BLOCK_FALSE();
            } else if (event.getMessage().contains("切换角色病娇学姐")) {
                aiMapper.updateUserRole(event.getUserId(), 9);
                redisConversationService.clearHistory(event.getUserId());
                bot.sendPrivateMsg(event.getUserId(), "依依已经切换到病娇学姐啦。", true);
                return ReturnType.BLOCK_FALSE();
            } else if (event.getMessage().contains("切换角色雌小鬼")) {
                aiMapper.updateUserRole(event.getUserId(), 10);
                redisConversationService.clearHistory(event.getUserId());
                bot.sendPrivateMsg(event.getUserId(), "依依已经切换到雌小鬼啦。", true);
                return ReturnType.BLOCK_FALSE();
            } else if(event.getMessage().contains("切换角色卡芙卡")) {
                aiMapper.updateUserRole(event.getUserId(), 11);
                redisConversationService.clearHistory(event.getUserId());
                bot.sendPrivateMsg(event.getUserId(), "依依已经切换到卡芙卡啦。", true);
                return ReturnType.BLOCK_FALSE();
            } else if (event.getMessage().contains("切换角色爱莉希雅")) {
                aiMapper.updateUserRole(event.getUserId(), 12);
                redisConversationService.clearHistory(event.getUserId());
                bot.sendPrivateMsg(event.getUserId(), "依依已经切换到爱莉希雅啦。", true);
                return ReturnType.BLOCK_FALSE();
            } else if (event.getMessage().contains("切换角色花火")) {
                aiMapper.updateUserRole(event.getUserId(), 13);
                redisConversationService.clearHistory(event.getUserId());
                bot.sendPrivateMsg(event.getUserId(), "依依已经切换到花火啦。", true);
                return ReturnType.BLOCK_FALSE();
            } else if (event.getMessage().contains("当前角色")) {
                int roleType = aiMapper.selectUserRole(event.getUserId());
                switch (roleType) {
                    case 0:
                        bot.sendPrivateMsg(event.getUserId(), "当前角色为默认。", true);
                        break;
                    case 1:
                        bot.sendPrivateMsg(event.getUserId(), "当前角色为老婆。", true);
                        break;
                    case 2:
                        bot.sendPrivateMsg(event.getUserId(), "当前角色为女仆。", true);
                        break;
                    case 3:
                        bot.sendPrivateMsg(event.getUserId(), "当前角色为魅魔。", true);
                        break;
                    case 4:
                        bot.sendPrivateMsg(event.getUserId(), "当前角色为贴吧老哥。", true);
                        break;
                    case 5:
                        bot.sendPrivateMsg(event.getUserId(), "当前角色为美少女。", true);
                        break;
                    case 6:
                        bot.sendPrivateMsg(event.getUserId(), "当前角色为傲娇猫娘。", true);
                        break;
                    case 7:
                        bot.sendPrivateMsg(event.getUserId(), "当前角色为白丝猫娘。", true);
                        break;
                    case 8:
                        bot.sendPrivateMsg(event.getUserId(), "当前角色为病娇老婆。", true);
                        break;
                    case 9:
                        bot.sendPrivateMsg(event.getUserId(), "当前角色为病娇学姐。", true);
                        break;
                    case 10:
                        bot.sendPrivateMsg(event.getUserId(), "当前角色为雌小鬼。", true);
                        break;
                    case 11:
                        bot.sendPrivateMsg(event.getUserId(), "当前角色为卡芙卡。", true);
                        break;
                    case 12:
                        bot.sendPrivateMsg(event.getUserId(), "当前角色为爱莉希雅。", true);
                        break;
                    case 13:
                        bot.sendPrivateMsg(event.getUserId(), "当前角色为花火。", true);
                        break;
                }
            }
            return ReturnType.BLOCK_FALSE();
        }
        Mono<String> context = null;
        int roleType = aiMapper.selectUserRole(event.getUserId());
        switch (roleType) {
            case 0:
                updateRole(Role.DEFAULT);
                break;
            case 1:
                updateRole(Role.WIFE);
                break;
            case 2:
                updateRole(Role.MAID);
                break;
            case 3:
                updateRole(Role.SUCCUBUS);
                break;
            case 4:
                updateRole(Role.ATTACK);
                break;
            case 5:
                updateRole(Role.GIRL);
                break;
            case 6:
                updateRole(Role.TSUNDERE_CAT);
                break;
            case 7:
                updateRole(Role.CAT_GIRL);
                break;
            case 8:
                updateRole(Role.YANDERE_WIFE);
                break;
            case 9:
                updateRole(Role.YANDERE_SENIOR);
                break;
            case 10:
                updateRole(Role.FEMALE_IMP);
                break;
            case 11:
                updateRole(Role.KAFKA);
                break;
            case 12:
                updateRole(Role.ELYSIA);
                break;
            case 13:
                updateRole(Role.SPARKLE);
                break;
        }
        Pattern pattern = Pattern.compile("^(.*?)(\\[CQ:image,[^]]*?url=([^,\\]]+)])(.*)?");
        Matcher matcher = pattern.matcher(event.getMessage());
        if (matcher.find()) {
            String beforeText = matcher.group(1) != null ? matcher.group(1) : "";
            String url = matcher.group(3);
            String afterText = matcher.group(4) != null ? matcher.group(4) : "";
            if (url != null) {
//                    UserMessage userMessage = new UserMessage(beforeText + afterText, List.of(new Media(new MimeType(), url)));
//                    ChatResponse response = chatClient.call(new Prompt(userMessage));
            } else {
                context = getContext(beforeText + afterText, event.getUserId());
            }
        } else {
            context = getContext(event.getMessage(), event.getUserId());
        }
        if (context != null) {
            context.subscribe(msg -> {
                bot.sendPrivateMsg(event.getUserId(), msg, false);
            });
        }
        return MESSAGE_BLOCK;
    }

    @Override
    public int onGroupMessage(Bot bot, GroupMessageEvent event) {
        if (event.getGroupId() == 176282339L) {
            return MESSAGE_BLOCK;
        }
        if (AtUtil.isAt(bot.getLoginInfo().getData(), event)) {
            if (event.getMessage().contains("切换角色") || event.getMessage().contains("当前角色") || event.getMessage().contains("清空记忆")) {
                if (event.getMessage().contains("清空记忆")) {
                    redisConversationService.clearHistory(event.getGroupId());
                    bot.sendGroupMsg(event.getGroupId(), "记忆已清空。", true);
                    return ReturnType.BLOCK_FALSE();
                }
                String role = bot.getGroupMemberInfo(event.getGroupId(), event.getUserId(), true).getData().getRole();
                if (Objects.equals(event.getUserId(), botConfig.getOwnerQQ()) || role.equals("owner") || role.equals("admin")) {
                    if (event.getMessage().contains("切换角色默认")) {
                        aiMapper.updateRole(event.getGroupId(), 0);
                        redisConversationService.clearHistory(event.getGroupId());
                        bot.sendGroupMsg(event.getGroupId(), "依依已经切换到默认啦。", true);
                        return ReturnType.BLOCK_FALSE();
                    } else if (event.getMessage().contains("切换角色老婆")) {
                        aiMapper.updateRole(event.getGroupId(), 1);
                        redisConversationService.clearHistory(event.getGroupId());
                        bot.sendGroupMsg(event.getGroupId(), "依依已经切换到老婆啦。", true);
                        return ReturnType.BLOCK_FALSE();
                    } else if (event.getMessage().contains("切换角色女仆")) {
                        aiMapper.updateRole(event.getGroupId(), 2);
                        redisConversationService.clearHistory(event.getGroupId());
                        bot.sendGroupMsg(event.getGroupId(), "依依已经切换到女仆啦。", true);
                        return ReturnType.BLOCK_FALSE();
                    } else if (event.getMessage().contains("切换角色魅魔")) {
                        aiMapper.updateRole(event.getGroupId(), 3);
                        redisConversationService.clearHistory(event.getGroupId());
                        bot.sendGroupMsg(event.getGroupId(), "依依已经切换到魅魔啦。", true);
                        return ReturnType.BLOCK_FALSE();
                    } else if (event.getMessage().contains("切换角色贴吧老哥")) {
                        aiMapper.updateRole(event.getGroupId(), 4);
                        redisConversationService.clearHistory(event.getGroupId());
                        bot.sendGroupMsg(event.getGroupId(), "依依已经切换到贴吧老哥啦。", true);
                        return ReturnType.BLOCK_FALSE();
                    } else if (event.getMessage().contains("切换角色美少女")) {
                        aiMapper.updateRole(event.getGroupId(), 5);
                        redisConversationService.clearHistory(event.getGroupId());
                        bot.sendGroupMsg(event.getGroupId(), "依依已经切换到美少女啦。", true);
                        return ReturnType.BLOCK_FALSE();
                    } else if (event.getMessage().contains("切换角色傲娇猫娘")) {
                        aiMapper.updateRole(event.getGroupId(), 6);
                        redisConversationService.clearHistory(event.getGroupId());
                        bot.sendGroupMsg(event.getGroupId(), "依依已经切换到傲娇猫娘啦。", true);
                        return ReturnType.BLOCK_FALSE();
                    } else if (event.getMessage().contains("切换角色白丝猫娘")) {
                        aiMapper.updateRole(event.getGroupId(), 7);
                        redisConversationService.clearHistory(event.getGroupId());
                        bot.sendGroupMsg(event.getGroupId(), "依依已经切换到白丝猫娘啦。", true);
                        return ReturnType.BLOCK_FALSE();
                    } else if (event.getMessage().contains("切换角色病娇老婆")) {
                        aiMapper.updateRole(event.getGroupId(), 8);
                        redisConversationService.clearHistory(event.getGroupId());
                        bot.sendGroupMsg(event.getGroupId(), "依依已经切换到病娇老婆啦。", true);
                        return ReturnType.BLOCK_FALSE();
                    } else if (event.getMessage().contains("切换角色病娇学姐")) {
                        aiMapper.updateRole(event.getGroupId(), 9);
                        redisConversationService.clearHistory(event.getGroupId());
                        bot.sendGroupMsg(event.getGroupId(), "依依已经切换到病娇学姐啦。", true);
                        return ReturnType.BLOCK_FALSE();
                    } else if (event.getMessage().contains("切换角色雌小鬼")) {
                        aiMapper.updateRole(event.getGroupId(), 10);
                        redisConversationService.clearHistory(event.getGroupId());
                        bot.sendGroupMsg(event.getGroupId(), "依依已经切换到雌小鬼啦。", true);
                        return ReturnType.BLOCK_FALSE();
                    } else if(event.getMessage().contains("切换角色卡芙卡")) {
                        aiMapper.updateRole(event.getGroupId(), 11);
                        redisConversationService.clearHistory(event.getGroupId());
                        bot.sendGroupMsg(event.getGroupId(), "依依已经切换到卡芙卡啦。", true);
                        return ReturnType.BLOCK_FALSE();
                    }  else if (event.getMessage().contains("切换角色爱莉希雅")) {
                        aiMapper.updateRole(event.getGroupId(), 12);
                        redisConversationService.clearHistory(event.getGroupId());
                        bot.sendGroupMsg(event.getGroupId(), "依依已经切换到爱莉希雅啦。", true);
                        return ReturnType.BLOCK_FALSE();
                    } else if (event.getMessage().contains("切换角色花火")) {
                        aiMapper.updateRole(event.getGroupId(), 13);
                        redisConversationService.clearHistory(event.getGroupId());
                        bot.sendGroupMsg(event.getGroupId(), "依依已经切换到花火啦。", true);
                        return ReturnType.BLOCK_FALSE();
                    } else if (event.getMessage().contains("当前角色")) {
                        int roleType = aiMapper.selectRole(event.getGroupId());
                        switch (roleType) {
                            case 0:
                                bot.sendGroupMsg(event.getGroupId(), "当前角色为默认。", true);
                                break;
                            case 1:
                                bot.sendGroupMsg(event.getGroupId(), "当前角色为老婆。", true);
                                break;
                            case 2:
                                bot.sendGroupMsg(event.getGroupId(), "当前角色为女仆。", true);
                                break;
                            case 3:
                                bot.sendGroupMsg(event.getGroupId(), "当前角色为魅魔。", true);
                                break;
                            case 4:
                                bot.sendGroupMsg(event.getGroupId(), "当前角色为贴吧老哥。", true);
                                break;
                            case 5:
                                bot.sendGroupMsg(event.getGroupId(), "当前角色为美少女。", true);
                                break;
                            case 6:
                                bot.sendGroupMsg(event.getGroupId(), "当前角色为傲娇猫娘。", true);
                                break;
                            case 7:
                                bot.sendGroupMsg(event.getGroupId(), "当前角色为白丝猫娘。", true);
                                break;
                            case 8:
                                bot.sendGroupMsg(event.getGroupId(), "当前角色为病娇老婆。", true);
                                break;
                            case 9:
                                bot.sendGroupMsg(event.getGroupId(), "当前角色为病娇学姐。", true);
                                break;
                            case 10:
                                bot.sendGroupMsg(event.getGroupId(), "当前角色为雌小鬼。", true);
                                break;
                            case 11:
                                bot.sendGroupMsg(event.getGroupId(), "当前角色为卡芙卡。", true);
                                break;
                            case 12:
                                bot.sendGroupMsg(event.getGroupId(), "当前角色为爱莉希雅。", true);
                                break;
                            case 13:
                                bot.sendGroupMsg(event.getGroupId(), "当前角色为花火。", true);
                        }
                        return ReturnType.BLOCK_FALSE();
                    }
//                if (event.getMessage().contains("切换模型")) {
//                    if (event.getMessage().contains("deepseek-chat") || event.getMessage().contains("V3")) {
//                        modelType = 1;
//                        updateModel(Model.V3);
//                        bot.sendGroupMsg(event.getGroupId(), "已切换到deepseek-chat模型。", true);
//                        return ReturnType.BLOCK_FALSE();
//                    } else if (event.getMessage().contains("deepseek-reasoner") || event.getMessage().contains("R1")) {
//                        modelType = 0;
//                        updateModel(Model.R1);
//                        bot.sendGroupMsg(event.getGroupId(), "已切换到deepseek-reasoner模型。", true);
//                        return ReturnType.BLOCK_FALSE();
//                    } else if (event.getMessage().contains("当前模型")) {
//                        switch (modelType) {
//                            case 0:
//                                bot.sendGroupMsg(event.getGroupId(), "当前模型为deepseek-reasoner。", true);
//                                break;
//                            case 1:
//                                bot.sendGroupMsg(event.getGroupId(), "当前模型为deepseek-chat。", true);
//                                break;
//                        }
//                        return ReturnType.BLOCK_FALSE();
//                    }
//                }
                } else {
                    String msg = MsgUtils.builder().at(event.getGroupId()).text("只有群主、管理员才能操作依依的角色哦。").build();
                    bot.sendGroupMsg(event.getGroupId(), msg, false);
                    return ReturnType.BLOCK_FALSE();
                }
            }
            if (ReturnType.getMatch()) {
                event.setMessage(event.getMessage().replaceAll("\\[CQ:reply,id=\\d+]", "").trim());
                Pattern pattern = Pattern.compile("\\[CQ:at,qq=(\\d+)]");
                Matcher matcher = pattern.matcher(event.getMessage());
                List<String> qqList = new ArrayList<>();
                while (matcher.find()) {
                    qqList.add(matcher.group(1));
                }
                if (!qqList.isEmpty()) {
                    for (String qq : qqList) {
                        if (qq.equals(String.valueOf(bot.getSelfId()))) {
                            event.setMessage(event.getMessage().replaceAll(atBot, ""));
                        } else {
                            ActionData<GroupMemberInfoResp> groupMemberInfo = bot.getGroupMemberInfo(event.getGroupId(), Long.parseLong(qq), false);
                            event.setMessage(event.getMessage().replaceAll("[CQ:at,qq=" + qq + "]", " " + groupMemberInfo.getData().getCard() + "(群友的名字) "));
                        }
                    }
                }
                Mono<String> context = null;
                int roleType = aiMapper.selectRole(event.getGroupId());
                switch (roleType) {
                    case 0:
                        updateRole(Role.DEFAULT);
                        break;
                    case 1:
                        updateRole(Role.WIFE);
                        break;
                    case 2:
                        updateRole(Role.MAID);
                        break;
                    case 3:
                        updateRole(Role.SUCCUBUS);
                        break;
                    case 4:
                        updateRole(Role.ATTACK);
                        break;
                    case 5:
                        updateRole(Role.GIRL);
                        break;
                    case 6:
                        updateRole(Role.TSUNDERE_CAT);
                        break;
                    case 7:
                        updateRole(Role.CAT_GIRL);
                        break;
                    case 8:
                        updateRole(Role.YANDERE_WIFE);
                        break;
                    case 9:
                        updateRole(Role.YANDERE_SENIOR);
                        break;
                    case 10:
                        updateRole(Role.FEMALE_IMP);
                        break;
                    case 11:
                        updateRole(Role.KAFKA);
                        break;
                    case 12:
                        updateRole(Role.ELYSIA);
                        break;
                    case 13:
                        updateRole(Role.SPARKLE);
                        break;
                }
                pattern = Pattern.compile("^(.*?)(\\[CQ:image,[^]]*?url=([^,\\]]+)])(.*)?");
                matcher = pattern.matcher(event.getMessage());
                if (matcher.find()) {
                    String beforeText = matcher.group(1) != null ? matcher.group(1) : "";
                    String url = matcher.group(3);
                    String afterText = matcher.group(4) != null ? matcher.group(4) : "";
                    if (url != null) {
//                    UserMessage userMessage = new UserMessage(beforeText + afterText, List.of(new Media(new MimeType(), url)));
//                    ChatResponse response = chatClient.call(new Prompt(userMessage));
                    } else {
                        context = getContext(beforeText + afterText, event.getGroupId());
                    }
                } else {
                    context = getContext(event.getMessage(), event.getGroupId());
                }
                if (context != null) {
                    context.subscribe(s -> {
                        String msg = MsgUtils.builder().reply(event.getMessageId()).at(event.getUserId()).text(s).build();
                        bot.sendGroupMsg(event.getGroupId(), msg, false);
                    });
                }
            }
        }
        return MESSAGE_BLOCK;
    }

    public synchronized void updateRole(Role newRole) {
        AIConfig.setRole(newRole);
        createChatClient();
    }

    public synchronized void updateModel(Model newModel) {
        AIConfig.setModel(newModel);
        createChatClient();
    }

    private void createChatClient() {
        this.chatClient = builder.defaultSystem(AIConfig.getRole())
//                .defaultOptions(ChatOptions.builder().model(AIConfig.getModel()).build())
                .build();
    }

    @Retryable(retryFor = Exception.class, maxAttempts = 5, backoff = @Backoff(delay = 3000, multiplier = 0))
    private Mono<String> getContext(String message, Long sessionId) {
        redisConversationService.addUserMessage(sessionId, message);
        String historyMessage = redisConversationService.getFormattedHistory(sessionId);
        Flux<String> content = chatClient.prompt(historyMessage).user(message).stream().content();
        return content.collectList()
                .map(list -> String.join(" ", list).replaceAll("\\s*", ""))
                .doOnNext(msg -> redisConversationService.addAssistantMessage(sessionId, msg));
    }
}
