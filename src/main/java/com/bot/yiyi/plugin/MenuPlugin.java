package com.bot.yiyi.plugin;

import com.bot.yiyi.Pojo.ReturnType;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import org.springframework.stereotype.Component;

@Component
public class MenuPlugin extends BotPlugin {

    @Override
    public int onGroupMessage(Bot bot, GroupMessageEvent event) {
        if (event.getMessage().contains("菜单")) {
            String msg = MsgUtils.builder().at(event.getUserId())
                    .text("\n依依菜单\n" +
                            "\n发癫模式\n" +
                            "随机输出各种有趣、魔性的内容，带来欢乐体验。\n" +
                            "\n积分系统\n" +
                            "记录并查询用户积分，完成任务可获得更多积分。\n" +
                            "\n结婚系统\n" +
                            "绑定关系，与指定用户建立虚拟婚姻。\n" +
                            "\n智能对话\n" +
                            "进行自由对话，提供信息和交流互动。\n" +
                            "\n发送对应插件，如 \"发癫模式\" 或 \"积分系统\" 来查看相应功能指令。").build();
            bot.sendGroupMsg(event.getGroupId(), msg, false);
            return ReturnType.BLOCK_TRUE();
        }
        if (event.getMessage().contains("发癫模式")) {
            String msg = MsgUtils.builder().at(event.getUserId())
                    .text("发癫模式\n" +
                            "@依依 —— 依依主动对你发癫。\n" +
                            "@+发癫 —— 你对@的人发癫。\n" +
                            "@+发癫+@ —— 指定某人对另一个人发癫。").build();
            bot.sendGroupMsg(event.getGroupId(), msg, false);
            return ReturnType.BLOCK_TRUE();
        }
        if (event.getMessage().contains("积分系统")) {
            String msg = MsgUtils.builder().at(event.getUserId())
                    .text("积分系统\n" +
                            "打卡 / 签到 —— 每日签到领取积分。\n" +
                            "积分抽奖 / 积分赌狗 —— 使用积分进行抽奖或赌积分。\n" +
                            "我的积分 / 积分 —— 查询自己的当前积分。\n" +
                            "存储积分+数量 —— 存入指定数量的积分。\n" +
                            "取出积分+数量 —— 取出存储的积分。\n" +
                            "赠送积分+数量+@ —— 将指定数量的积分赠送给某人。\n" +
                            "富豪榜 —— 查看全群积分最高的用户。\n" +
                            "负豪榜 —— 查看全群积分最低的用户。\n" +
                            "群富豪榜 —— 查看群内积分前几名的用户。\n" +
                            "群负豪榜 —— 查看群内积分最低的用户。"
                    ).build();
            bot.sendGroupMsg(event.getGroupId(), msg, false);
            return ReturnType.BLOCK_TRUE();
        }
        if (event.getMessage().contains("结婚系统")) {
            String msg = MsgUtils.builder().at(event.getUserId())
                    .text("结婚系统\n" +
                            "娶群友 / 娶老婆 —— 随机迎娶某人。\n" +
                            "嫁群友 / 嫁老公 —— 随机嫁给某人。\n" +
                            "娶+@ —— 向指定用户求婚。\n" +
                            "嫁+@ —— 向指定用户申请嫁过去。\n" +
                            "强娶+@ —— 不管对方同不同意，强行娶走。\n" +
                            "硬嫁+@ —— 不管对方愿不愿意，强行嫁过去。\n" +
                            "抢老婆+@你要抢的人 —— 抢走别人的老婆。\n" +
                            "抢老公+@你要抢的人 —— 抢走别人的老公。\n" +
                            "我的老婆 / 我的老公 —— 查询自己的配偶信息。\n" +
                            "闹离婚 / 闹分手 —— 解除关系。").build();
            bot.sendGroupMsg(event.getGroupId(), msg, false);
            return ReturnType.BLOCK_TRUE();
        }
        if (event.getMessage().contains("智能对话")) {
            String msg = MsgUtils.builder().at(event.getUserId())
                    .text("智能对话\n" +
                            "@依依 就可以和依依对话啦！\n" +
                            "@依依切换角色+角色 可以切换角色哦~\n" +
                            "目前角色有:\n" +
                            "-默认\n" +
                            "-老婆\n" +
                            "-女仆\n" +
                            "-魅魔\n" +
                            "-美少女\n" +
                            "-贴吧老哥\n" +
                            "-傲娇猫娘\n" +
                            "ps:只有群主和管理员才可以切换角色哦~"
                    ).build();
            bot.sendGroupMsg(event.getGroupId(), msg, false);
            return ReturnType.BLOCK_TRUE();
        }
        return ReturnType.IGNORE_TRUE();
    }

}
