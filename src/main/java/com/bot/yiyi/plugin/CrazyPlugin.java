package com.bot.yiyi.plugin;

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

import java.util.ArrayList;
import java.util.List;

@Component
public class CrazyPlugin extends BotPlugin {

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
        System.out.println(event.getMessage());
        LoginInfoResp data = bot.getLoginInfo().getData();
        StrangerInfoResp strangerInfo = bot.getStrangerInfo(event.getUserId(), false).getData();
        List<String> msgList = new ArrayList<>();
        if (AtUtil.onlyAt(data, event)) {
            String crazy = HttpPostExample.crazy().replaceAll("745689", strangerInfo.getNickname());
            String img = MsgUtils.builder().img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + event.getUserId() + "&s=640").summary("快来操死我")).build();
            msgList.add(crazy);
            msgList.add(img);
            bot.sendGroupForwardMsg(event.getGroupId(), AtUtil.toForward(data.getNickname(), data.getUserId(), msgList));
            return MESSAGE_IGNORE;
        }
        if (event.getMessage().contains("发电") || event.getMessage().contains("发癫") || event.getMessage().contains("爱你")) {
            List<Long> qqList = AtUtil.extractQQs(event.getMessage());
            if (qqList.isEmpty()) {
                return MESSAGE_IGNORE;
            }
            if (qqList.size() == 1) {
                StrangerInfoResp toStrangerInfo = bot.getStrangerInfo(qqList.get(0), false).getData();
                String crazy = HttpPostExample.crazy().replaceAll("745689", toStrangerInfo.getNickname());
                String img = MsgUtils.builder().img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + qqList.get(0) + "&s=640").summary("快来操死我")).build();
                msgList.add(crazy);
                msgList.add(img);
                bot.sendGroupForwardMsg(event.getGroupId(), AtUtil.toForward(strangerInfo.getNickname(), event.getUserId(), msgList));
                return MESSAGE_IGNORE;
            } else if (qqList.size() == 2) {
                StrangerInfoResp toStrangerInfo = bot.getStrangerInfo(qqList.get(1), false).getData();
                StrangerInfoResp fromStrangerInfo = bot.getStrangerInfo(qqList.get(0), false).getData();
                String crazy = HttpPostExample.crazy().replaceAll("745689", toStrangerInfo.getNickname());
                String img = MsgUtils.builder().img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + qqList.get(1) + "&s=640").summary("快来操死我")).build();
                msgList.add(crazy);
                msgList.add(img);
                bot.sendGroupForwardMsg(event.getGroupId(), AtUtil.toForward(fromStrangerInfo.getNickname(), fromStrangerInfo.getUserId(), msgList));
                return MESSAGE_IGNORE;
            } else {
                String msg = MsgUtils.builder().at(event.getUserId()).text("你还想要多少？渣男！").build();
                bot.sendGroupMsg(event.getGroupId(), msg, false);
                return MESSAGE_IGNORE;
            }
        }
        // 返回 MESSAGE_IGNORE 执行 plugin-list 下一个插件，返回 MESSAGE_BLOCK 则不执行下一个插件
        return MESSAGE_IGNORE;
    }

}

