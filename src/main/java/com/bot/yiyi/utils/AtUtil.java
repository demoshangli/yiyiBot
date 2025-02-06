package com.bot.yiyi.utils;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.common.ActionData;
import com.mikuac.shiro.dto.action.response.LoginInfoResp;
import com.mikuac.shiro.dto.action.response.StrangerInfoResp;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AtUtil {

    public static boolean isAt(LoginInfoResp data, GroupMessageEvent event) {
        if (onlyAt(data, event))
            return false;
        String botQQ = data.getUserId().toString();
        return event.getMessage().contains("[CQ:at,qq=" + botQQ + "]");
    }

    public static boolean onlyAt(LoginInfoResp data, GroupMessageEvent event) {
        String botQQ = data.getUserId().toString();
        return event.getMessage().equals("[CQ:at,qq=" + botQQ + "]");
    }

    public static List<Map<String, Object>> toForward(String name, Long nick, List<String> msgList) {
        List<Map<String, Object>> m = new ArrayList<>();
        for (String msg : msgList) {
            Map<String, Object> node = new HashMap<>();
            node.put("type", "node");
            Map<String, Object> data = new HashMap<>();
            data.put("uin", nick); // 机器人自己发送
            data.put("name", name);
            data.put("content", msg);
            node.put("data", data);
            m.add(node);
        }
        return m;
    }

    public static List<Long> extractQQs(String message) {
        List<Long> qqList = new ArrayList<>();
        // 定义正则表达式
        String regex = "CQ:at,qq=(\\d+)";
        // 创建 Pattern 对象
        Pattern pattern = Pattern.compile(regex);
        // 创建 Matcher 对象
        Matcher matcher = pattern.matcher(message);

        // 查找匹配的结果
        while (matcher.find()) {
            // 提取 QQ 号并加入到列表中
            qqList.add(Long.valueOf(matcher.group(1)));
        }

        return qqList;
    }
}
