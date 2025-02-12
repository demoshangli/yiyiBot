package com.bot.yiyi.plugin;

import com.bot.yiyi.Pojo.Model;
import com.bot.yiyi.Pojo.ReturnType;
import com.bot.yiyi.Pojo.Role;
import com.bot.yiyi.config.AIConfig;
import com.bot.yiyi.config.BotConfig;
import com.bot.yiyi.mapper.AIMapper;
import com.bot.yiyi.utils.AtUtil;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.action.common.ActionData;
import com.mikuac.shiro.dto.action.response.GroupMemberInfoResp;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

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

    public AiPlugin(ChatClient chatClient, ChatClient.Builder builder) {
        this.chatClient = chatClient;
        this.builder = builder;
        createChatClient();
    }

    @Override
    public int onGroupMessage(Bot bot, GroupMessageEvent event) {
        if (AtUtil.isAt(bot.getLoginInfo().getData(), event)) {
            if (event.getMessage().contains("切换角色") || event.getMessage().contains("当前角色")) {
                String role = bot.getGroupMemberInfo(event.getGroupId(), event.getUserId(), true).getData().getRole();
                if (Objects.equals(event.getUserId(), botConfig.getOwnerQQ()) || role.equals("owner") || role.equals("admin")) {
                    if (event.getMessage().contains("切换角色默认")) {
                        aiMapper.updateRole(event.getGroupId(), 0);
                        bot.sendGroupMsg(event.getGroupId(), "依依已经切换到默认啦。", true);
                        return ReturnType.BLOCK_FALSE();
                    } else if (event.getMessage().contains("切换角色老婆")) {
                        aiMapper.updateRole(event.getGroupId(), 1);
                        bot.sendGroupMsg(event.getGroupId(), "依依已经切换到老婆啦。", true);
                        return ReturnType.BLOCK_FALSE();
                    } else if (event.getMessage().contains("切换角色女仆")) {
                        aiMapper.updateRole(event.getGroupId(), 2);
                        bot.sendGroupMsg(event.getGroupId(), "依依已经切换到女仆啦。", true);
                        return ReturnType.BLOCK_FALSE();
                    } else if (event.getMessage().contains("切换角色魅魔")) {
                        aiMapper.updateRole(event.getGroupId(), 3);
                        bot.sendGroupMsg(event.getGroupId(), "依依已经切换到魅魔啦。", true);
                        return ReturnType.BLOCK_FALSE();
                    } else if (event.getMessage().contains("切换角色贴吧老哥")) {
                        aiMapper.updateRole(event.getGroupId(), 4);
                        bot.sendGroupMsg(event.getGroupId(), "依依已经切换到贴吧老哥啦。", true);
                        return ReturnType.BLOCK_FALSE();
                    } else if (event.getMessage().contains("切换角色美少女")) {
                        aiMapper.updateRole(event.getGroupId(), 5);
                        bot.sendGroupMsg(event.getGroupId(), "依依已经切换到美少女啦。", true);
                        return ReturnType.BLOCK_FALSE();
                    } else if (event.getMessage().contains("切换角色傲娇猫娘")) {
                        aiMapper.updateRole(event.getGroupId(), 6);
                        bot.sendGroupMsg(event.getGroupId(), "依依已经切换到傲娇猫娘啦。", true);
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
                    String msg = MsgUtils.builder().at(event.getUserId()).text("只有群主、管理员才能切换依依的角色哦。").build();
                    bot.sendGroupMsg(event.getGroupId(), msg, false);
                    return ReturnType.BLOCK_FALSE();
                }
            }
            if (ReturnType.getMatch()) {
                event.setMessage(event.getMessage().replaceAll("\\[CQ:reply,id=\\d+\\]", "").trim());
                Pattern pattern = Pattern.compile("\\[CQ:at,qq=(\\d+)]");
                Matcher matcher = pattern.matcher(event.getMessage());
                List<String> qqList = new ArrayList<>();

                // 提取所有匹配的 qq 号
                while (matcher.find()) {
                    qqList.add(matcher.group(1));  // 获取第一个捕获组（即 qq 号）
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
                Flux<String> context = null;
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
                        updateRole(Role.CAT);
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
                        context = getContext(beforeText + afterText);
                    }
                } else {
                    context = getContext(event.getMessage());
                }
                if (context != null) {
                    context.collectList()
                            .map(list -> String.join(" ", list)) // 转换为 String
                            .subscribe(message -> {
                                String msg = MsgUtils.builder()
                                        .reply(event.getMessageId())
                                        .at(event.getUserId())
                                        .text(message.replaceAll("\\s*", ""))
                                        .build();
                                bot.sendGroupMsg(event.getGroupId(), msg, false);
                            });
                }
            }
        }
        return MESSAGE_BLOCK;
    }

    public synchronized void updateRole(Role newRole) {
        // 更新 role 变量
        AIConfig.setRole(newRole);
        // 重新创建 ChatClient 实例
        createChatClient();
    }

    public synchronized void updateModel(Model newModel) {
        // 更新 model 变量
        AIConfig.setModel(newModel);
        // 重新创建 ChatClient 实例
        createChatClient();
    }

    private void createChatClient() {
        this.chatClient = builder.defaultSystem(AIConfig.getRole())
//                .defaultOptions(ChatOptions.builder().model(AIConfig.getModel()).build())
                .build();
    }

    @Retryable(retryFor = Exception.class, maxAttempts = 5, backoff = @Backoff(delay = 3000, multiplier = 0))
    private Flux<String> getContext(String message) {
        return chatClient.prompt().user(message).stream().content();
    }
}
