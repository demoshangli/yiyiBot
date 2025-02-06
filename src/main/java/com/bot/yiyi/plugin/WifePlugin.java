package com.bot.yiyi.plugin;

import com.bot.yiyi.utils.AtUtil;
import com.bot.yiyi.utils.HttpPostExample;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.common.utils.OneBotMedia;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.action.response.GroupMemberInfoResp;
import com.mikuac.shiro.dto.action.response.StrangerInfoResp;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class WifePlugin extends BotPlugin{

    @Override
    public int onGroupMessage(Bot bot, GroupMessageEvent event) {
        if(event.getMessage().contains("娶群友") || event.getMessage().contains("娶老婆"))
        {
            List<GroupMemberInfoResp> memberList = bot.getGroupMemberList(event.getGroupId()).getData();
            Random random = new Random();
            GroupMemberInfoResp wifeInfo = memberList.get(random.nextInt(memberList.size()));
            if (wifeInfo.getUserId() == event.getUserId())
                wifeInfo = memberList.get(random.nextInt(memberList.size()));
            String msg = MsgUtils.builder().at(event.getUserId()).text("恭喜你，成功娶到了").at(wifeInfo.getUserId())
                    .img(OneBotMedia.builder().file("https://q1.qlogo.cn/g?b=qq&nk=" + wifeInfo.getUserId() + "&s=640"))
                    .text("记得好好珍惜她哦。").build();
            bot.sendGroupMsg(event.getGroupId(), msg, false);
        }
        return MESSAGE_IGNORE;
    }
}
