package com.bot.yiyi.plugin;


import com.alibaba.fastjson2.JSONObject;
import com.bot.yiyi.Pojo.MessageReceiver;
import com.bot.yiyi.utils.RedisConversationService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WechatPlugin {

    @Autowired
    private RedisConversationService redisConversationService;
    private ChatClient chatClient;
    private final ChatClient.Builder builder;

    public WechatPlugin(ChatClient chatClient, ChatClient.Builder builder) {
        this.chatClient = chatClient;
        this.builder = builder;
        createChatClient();
    }

    @PostMapping("/wx/call")
    public Object call(String type, String content, String source, String isMentioned, String isMsgFromSelf) {
        if (type.equals("system_event_push_notify")) {
            return new JSONObject().put("success", false);
        } else if (type.equals("friendship")) {
            return new JSONObject().put("success", true);
        }
        MessageReceiver msg = new MessageReceiver(type, content, null, Boolean.parseBoolean(isMentioned), Boolean.parseBoolean(isMsgFromSelf));
        msg.setSource(source);
        if (msg.getSource().getFrom().getId() != null && msg.getSource().getFrom().getId().equals("weixin")) {
            return new JSONObject().put("success", false);
        }
        if (msg.getSource().getTo().getId() != null || (msg.getSource().getRoom().getId() != null && isMentioned.equals("1"))) {
            msg.setContent(msg.getContent().replace("@离", "").replace("@依依", ""));
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("success", true);
            JSONObject data = new JSONObject();
            data.put("type", "text");
            if (type.equals("text")) {
                String msgString = msg.getContent();
                if (msgString.contains("清空记忆")) {
                    redisConversationService.clearHistory(msg.getSource().getFrom().getId());
                    data.put("content", "记忆已清空");
                } else {
                    createChatClient();
                    String context = getContext(msgString, msg.getSource().getFrom().getId());
                    data.put("content", context);
                }
            } else {
                data.put("content", "暂不支持该类型消息哦~");
            }
            jsonObject.put("data", data);
            return jsonObject;
        } else {
            return new JSONObject().put("success", false);
        }
    }

    private void createChatClient() {
        this.chatClient = builder.defaultSystem("在用户没有要求的情况下,默认使用中文回答。特别注意不要使用括号加入动作。").build();
    }

    private String getContext(String message, String sessionId) {
        redisConversationService.addUserMessage(sessionId, message);
        String historyMessage = redisConversationService.getFormattedHistory(sessionId);
        String content = chatClient.prompt(historyMessage).user(message).call().content();
        redisConversationService.addAssistantMessage(sessionId, content);
        return content;
    }
}
