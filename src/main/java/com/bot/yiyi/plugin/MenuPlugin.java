package com.bot.yiyi.plugin;

import com.bot.yiyi.Pojo.ReturnType;
import com.bot.yiyi.utils.AtUtil;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

import static com.bot.yiyi.Pojo.AtBot.AT_BOT;

@Component
public class MenuPlugin extends BasePlugin {
    
    @Autowired
    private ReturnType returnType;

    @Override
    public int onAnyMessage(Bot bot, AnyMessageEvent event) {
        Set<String> gameSet = new HashSet<>(Arrays.asList("菜单", AT_BOT + "菜单"));
        if (gameSet.contains(event.getMessage())) {
            List<String> menu = new ArrayList<>();
            menu.add("依依菜单Ciallo～(∠・ω< )⌒☆");
            menu.add("开关\n" +
                    "开启/关闭 + 模块名称\n" +
                    "依依开机/关机"
            );
            menu.add("游戏模块\n" +
                    "随机古诗 —— 随机一句古诗。\n" +
                    "随机一言 —— 随机一句鸡汤。\n" +
                    "随机语录 —— 随机一句社会语录。\n" +
                    "毒鸡汤 —— 随机一句毒鸡汤。\n" +
                    "舔狗日记 —— 随机舔狗日记。\n" +
                    "亚名 —— 高科技亚文化取名机。\n" +
                    "曼波 —— 有小马的语音包哦。\n" +
                    "@依依 —— 依依主动对你发癫。\n" +
                    "@+发癫 —— 你对@的人发癫。\n" +
                    "@+发癫+@ —— 指定某人对另一个人发癫。");
            menu.add("积分模块\n" +
                            "打卡 / 签到 —— 每日签到领取积分。\n" +
                            "积分抽奖 / 积分赌狗 —— 使用积分进行抽奖或赌积分。\n" +
                            "我的积分 / 积分 —— 查询自己的当前积分。\n" +
                            "存储积分+数量 —— 存入指定数量的积分。\n" +
                            "取出积分+数量 —— 取出存储的积分。\n" +
                            "赠送积分+数量+@ —— 将指定数量的积分赠送给某人。\n" +
                            "富豪榜 —— 查看全群积分最高的用户。\n" +
                            "负豪榜 —— 查看全群积分最低的用户。\n" +
                            "群富豪榜 —— 查看群内积分前几名的用户。\n" +
                            "群负豪榜 —— 查看群内积分最低的用户。");
            menu.add("结婚模块\n" +
                            "娶群友 / 娶老婆 —— 随机迎娶某人。\n" +
                            "嫁群友 / 嫁老公 —— 随机嫁给某人。\n" +
                            "娶+@ —— 向指定用户求婚。\n" +
                            "嫁+@ —— 向指定用户申请嫁过去。\n" +
                            "强娶+@ —— 不管对方同不同意，强行娶走。\n" +
                            "硬嫁+@ —— 不管对方愿不愿意，强行嫁过去。\n" +
                            "抢老婆+@你要抢的人 —— 抢走别人的老婆。\n" +
                            "抢老公+@你要抢的人 —— 抢走别人的老公。\n" +
                            "我的老婆 / 我的老公 —— 查询自己的配偶信息。\n" +
                            "闹离婚 / 闹分手 —— 解除关系。");
            menu.add("好感度模块\n" +
                    "抱抱+@ —— 抱抱你的伴侣，当然你也可以抱别人。\n" +
                    "亲亲+@ —— 亲亲你的伴侣，当然你也可以亲别人。\n" +
                    "给零花钱 —— 可以给你的伴侣零花钱，但是不能给别人，依依不希望你变成散财童子。");
            menu.add("AI对话模块\n" +
                            "@依依 就可以和依依对话啦！\n" +
                            "@依依清空记忆 —— 清空对话记忆\n" +
                            "@依依+切换模式个人/群聊 切换ai模式\n" +
                            "@依依+当前模式 查看当前模式\n" +
                            "@依依切换角色+角色 可以切换角色哦~\n" +
                            "目前角色有:\n" +
                            "R18角色-\n" +
                            "病娇老婆 白丝猫娘 魅魔\n" +
                            "正常角色-\n" +
                            "老婆 女仆 美少女 病娇学姐 傲娇猫娘 雌小鬼\n" +
                            "-特殊角色\n" +
                            "默认 贴吧老哥 卡芙卡 爱莉希雅 花火\n" +
                            "ps:只有群主和管理员才可以切换角色哦~\n" +
                            "pps:切换角色会自动清空记忆哦~\n");
            menu.add("伪人模块\n" +
                    "@依依+设置伪人概率1-100 设置伪人模式回复概率");
            menu.add("如发现bug或提出建议，请使用 联系主人+内容 进行反馈");
            if (event.getGroupId() != null)
                bot.sendGroupForwardMsg(event.getGroupId(), AtUtil.toForward(bot.getLoginInfo().getData().getNickname(), bot.getLoginInfo().getData().getUserId(), menu));
            else
                bot.sendPrivateForwardMsg(event.getUserId(), AtUtil.toForward(bot.getLoginInfo().getData().getNickname(), bot.getLoginInfo().getData().getUserId(), menu));
            return returnType.IGNORE_FALSE(event.getMessageId());
        }
        return MESSAGE_IGNORE;
    }

}
