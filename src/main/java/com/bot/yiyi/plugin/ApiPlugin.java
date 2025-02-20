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
import org.springframework.stereotype.Component;

import java.util.*;

import static com.bot.yiyi.plugin.RegisterPlugin.atBot;

@Component
public class ApiPlugin extends BotPlugin {

//    @Override
//    public int onPrivateMessage(Bot bot, PrivateMessageEvent event) {
//        if ("测试".equals(event.getMessage())) {
//            // 构建消息
//            String sendMsg = MsgUtils.builder()
//                    .at(event.getUserId())
//                    .text(" 测试成功啦！")
//                    .build();
//            bot.sendPrivateMsg(event.getUserId(), sendMsg, false);
//        }
//        // 返回 MESSAGE_IGNORE 执行 plugin-list 下一个插件，返回 MESSAGE_BLOCK 则不执行下一个插件
//        return MESSAGE_IGNORE;
//    }

    @Override
    public int onGroupMessage(Bot bot, GroupMessageEvent event) {
        if (event.getGroupId() == 176282339L) {
            return ReturnType.IGNORE_TRUE();
        }
        LoginInfoResp data = bot.getLoginInfo().getData();
        StrangerInfoResp strangerInfo = bot.getStrangerInfo(event.getUserId(), true).getData();
        List<String> msgList = new ArrayList<>();
        if (AtUtil.onlyAt(data, event)) {
            String crazy = HttpPostExample.crazy().replaceAll("745689", strangerInfo.getNickname());
            String img = MsgUtils.builder().img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + event.getUserId() + "&s=640").summary("快来操死我")).build();
            msgList.add(crazy);
            msgList.add(img);
            bot.sendGroupForwardMsg(event.getGroupId(), AtUtil.toForward(data.getNickname(), data.getUserId(), msgList));
            return ReturnType.IGNORE_FALSE();
        }
        if (event.getMessage().contains("发电") || event.getMessage().contains("发癫") || event.getMessage().contains("爱你")) {
            List<Long> qqList = AtUtil.extractQQs(event.getMessage());
            if (qqList.isEmpty()) {
                return ReturnType.IGNORE_FALSE();
            }
            if (qqList.size() == 1) {
                StrangerInfoResp toStrangerInfo = bot.getStrangerInfo(qqList.get(0), true).getData();
                String crazy = HttpPostExample.crazy().replaceAll("745689", toStrangerInfo.getNickname());
                String img = MsgUtils.builder().img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + qqList.get(0) + "&s=640").summary("快来操死我")).build();
                msgList.add(crazy);
                msgList.add(img);
                bot.sendGroupForwardMsg(event.getGroupId(), AtUtil.toForward(strangerInfo.getNickname(), event.getUserId(), msgList));
                if (qqList.get(0) != bot.getSelfId())
                    return ReturnType.IGNORE_TRUE();
                else
                    return ReturnType.IGNORE_FALSE();
            } else if (qqList.size() == 2) {
                StrangerInfoResp toStrangerInfo = bot.getStrangerInfo(qqList.get(1), true).getData();
                StrangerInfoResp fromStrangerInfo = bot.getStrangerInfo(qqList.get(0), true).getData();
                String crazy = HttpPostExample.crazy().replaceAll("745689", toStrangerInfo.getNickname());
                String img = MsgUtils.builder().img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + qqList.get(1) + "&s=640").summary("快来操死我")).build();
                msgList.add(crazy);
                msgList.add(img);
                bot.sendGroupForwardMsg(event.getGroupId(), AtUtil.toForward(fromStrangerInfo.getNickname(), fromStrangerInfo.getUserId(), msgList));
                if (qqList.get(0) != bot.getSelfId() && qqList.get(1) != bot.getSelfId())
                    return ReturnType.IGNORE_TRUE();
                else
                    return ReturnType.IGNORE_FALSE();
            } else {
                String msg = MsgUtils.builder().at(event.getUserId()).text("你还想要多少？渣男！").build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
                return ReturnType.IGNORE_FALSE();
            }
        }
        Set<String> apiSet = new HashSet<>(Arrays.asList("随机古诗", atBot + "随机古诗"));
        if (apiSet.contains(event.getMessage())) {
            String s = HttpPostExample.getAncientPoetry();
            String msg = MsgUtils.builder().at(event.getUserId()).text(s).build();
            bot.sendGroupMsg(event.getGroupId(), msg, false);
            return ReturnType.IGNORE_FALSE();
        }
        apiSet = new HashSet<>(Arrays.asList("随机一言", atBot + "随机一言"));
        if (apiSet.contains(event.getMessage())) {
            String s = HttpPostExample.getRandomOne();
            String msg = MsgUtils.builder().at(event.getUserId()).text(s).build();
            bot.sendGroupMsg(event.getGroupId(), msg, false);
            return ReturnType.IGNORE_FALSE();
        }
        apiSet = new HashSet<>(Arrays.asList("随机语录", atBot + "随机语录"));
        if (apiSet.contains(event.getMessage())) {
            String s = HttpPostExample.society();
            String msg = MsgUtils.builder().at(event.getUserId()).text(s).build();
            bot.sendGroupMsg(event.getGroupId(), msg, false);
            return ReturnType.IGNORE_FALSE();
        }
        apiSet = new HashSet<>(Arrays.asList("毒鸡汤", atBot + "毒鸡汤"));
        if (apiSet.contains(event.getMessage())) {
            String s = HttpPostExample.duTang();
            String msg = MsgUtils.builder().at(event.getUserId()).text(s).build();
            bot.sendGroupMsg(event.getGroupId(), msg, false);
            return ReturnType.IGNORE_FALSE();
        }
        apiSet = new HashSet<>(Arrays.asList("舔狗日记", atBot + "舔狗日记"));
        if (apiSet.contains(event.getMessage())) {
            String s = HttpPostExample.loveDog();
            msgList.add(s);
            bot.sendGroupForwardMsg(event.getGroupId(), AtUtil.toForward(strangerInfo.getNickname(), event.getUserId(), msgList));
            return ReturnType.IGNORE_FALSE();
        }
        apiSet = new HashSet<>(Arrays.asList("随机图片", atBot + "随机图片"));
        if (apiSet.contains(event.getMessage())) {
            String s = HttpPostExample.getRandomPic();
            String msg = MsgUtils.builder().img(OneBotMedia.builder().file(s)).build();
            bot.sendGroupMsg(event.getGroupId(), msg, false);
            return ReturnType.IGNORE_FALSE();
        }
        return ReturnType.IGNORE_TRUE();
    }

}

