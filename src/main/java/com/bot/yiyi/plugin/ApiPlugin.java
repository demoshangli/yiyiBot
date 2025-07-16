package com.bot.yiyi.plugin;

import com.bot.yiyi.Pojo.ReturnType;
import com.bot.yiyi.utils.AtUtil;
import com.bot.yiyi.utils.HttpPostExample;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.common.utils.OneBotMedia;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.action.response.LoginInfoResp;
import com.mikuac.shiro.dto.action.response.StrangerInfoResp;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.bot.yiyi.Pojo.AtBot.AT_BOT;

@Component
public class ApiPlugin extends BotPlugin {

    @Autowired
    private ReturnType returnType;

    @Override
    public int onGroupMessage(Bot bot, GroupMessageEvent event) {
        // 忽略指定群
        if (event.getGroupId() == 176282339L) {
            return returnType.IGNORE_TRUE(event.getMessageId());
        }

        // 获取登录信息和发送者信息
        LoginInfoResp data = bot.getLoginInfo().getData();
        StrangerInfoResp strangerInfo = bot.getStrangerInfo(event.getUserId(), true).getData();
        List<String> msgList = new ArrayList<>();

        // 仅@机器人时触发 AI 回复（crazy 模式）
        if (AtUtil.onlyAt(data, event)) {
            String crazy = HttpPostExample.crazy().replaceAll("745689", strangerInfo.getNickname());
            String img = MsgUtils.builder().img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + event.getUserId() + "&s=640").summary("快来操死我")).build();
            msgList.add(crazy);
            msgList.add(img);
            bot.sendGroupForwardMsg(event.getGroupId(), AtUtil.toForward(data.getNickname(), data.getUserId(), msgList));
            return returnType.IGNORE_FALSE(event.getMessageId());
        }

        // 特定关键词触发带@某人的 AI 回复（crazy 模式）
        if (event.getMessage().contains("发电") || event.getMessage().contains("发癫") || event.getMessage().contains("爱你")) {
            List<Long> qqList = AtUtil.extractQQs(event.getMessage());
            if (qqList.isEmpty()) {
                return returnType.IGNORE_FALSE(event.getMessageId());
            }
            if (qqList.size() == 1) {
                StrangerInfoResp toStrangerInfo = bot.getStrangerInfo(qqList.get(0), true).getData();
                String crazy = HttpPostExample.crazy().replaceAll("745689", toStrangerInfo.getNickname());
                String img = MsgUtils.builder().img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + qqList.get(0) + "&s=640").summary("快来操死我")).build();
                msgList.add(crazy);
                msgList.add(img);
                bot.sendGroupForwardMsg(event.getGroupId(), AtUtil.toForward(strangerInfo.getNickname(), event.getUserId(), msgList));
                return returnType.IGNORE_FALSE(event.getMessageId());
            } else if (qqList.size() == 2) {
                StrangerInfoResp toStrangerInfo = bot.getStrangerInfo(qqList.get(1), true).getData();
                StrangerInfoResp fromStrangerInfo = bot.getStrangerInfo(qqList.get(0), true).getData();
                String crazy = HttpPostExample.crazy().replaceAll("745689", toStrangerInfo.getNickname());
                String img = MsgUtils.builder().img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + qqList.get(1) + "&s=640").summary("快来操死我")).build();
                msgList.add(crazy);
                msgList.add(img);
                bot.sendGroupForwardMsg(event.getGroupId(), AtUtil.toForward(fromStrangerInfo.getNickname(), fromStrangerInfo.getUserId(), msgList));
                if (qqList.get(0) != bot.getSelfId() && qqList.get(1) != bot.getSelfId())
                    return returnType.IGNORE_TRUE(event.getMessageId());
                else
                    return returnType.IGNORE_FALSE(event.getMessageId());
            } else {
                String msg = MsgUtils.builder().at(event.getUserId()).text("你还想要多少？渣男！").build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
                return returnType.IGNORE_FALSE(event.getMessageId());
            }
        }

        // 文本指令关键词集合
        Map<String, Runnable> apiHandlers = new LinkedHashMap<>();

        apiHandlers.put("随机古诗", () -> sendText(bot, event, HttpPostExample.getAncientPoetry()));
        apiHandlers.put("随机一言", () -> sendText(bot, event, HttpPostExample.getRandomOne()));
        apiHandlers.put("随机语录", () -> sendText(bot, event, HttpPostExample.society()));
        apiHandlers.put("毒鸡汤", () -> sendText(bot, event, HttpPostExample.duTang()));
        apiHandlers.put("舔狗日记", () -> sendForward(bot, event, Collections.singletonList(HttpPostExample.loveDog())));
        apiHandlers.put("随机图片", () -> sendImage(bot, event, HttpPostExample.getRandomPic()));

        // 统一处理带@机器人的命令
        for (String key : new ArrayList<>(apiHandlers.keySet())) {
            apiHandlers.put(AT_BOT + key, apiHandlers.get(key));
        }

        // 检查消息是否匹配关键词
        if (apiHandlers.containsKey(event.getMessage())) {
            apiHandlers.get(event.getMessage()).run();
            return returnType.IGNORE_FALSE(event.getMessageId());
        }

        return MESSAGE_IGNORE;
    }

    private void sendText(Bot bot, GroupMessageEvent event, String text) {
        String msg = MsgUtils.builder().at(event.getUserId()).text(text).build();
        bot.sendGroupMsg(event.getGroupId(), msg, false);
    }

    private void sendImage(Bot bot, GroupMessageEvent event, String url) {
        String msg = MsgUtils.builder().img(OneBotMedia.builder().file(url)).build();
        bot.sendGroupMsg(event.getGroupId(), msg, false);
    }

    private void sendForward(Bot bot, GroupMessageEvent event, List<String> contentList) {
        StrangerInfoResp info = bot.getStrangerInfo(event.getUserId(), true).getData();
        bot.sendGroupForwardMsg(event.getGroupId(), AtUtil.toForward(info.getNickname(), event.getUserId(), contentList));
    }
}