package com.bot.yiyi.api;

import com.alibaba.fastjson2.JSONObject;
import com.bot.yiyi.utils.OkhttpUtil;

/**
 * 消息模块
 */
public class MessageApi {

    /**
     * 发送文字消息
     */
    public static JSONObject postText(String ats, String content, boolean isRoom) {
        JSONObject param = new JSONObject();
        param.put("to", ats);
        param.put("content", content);
        param.put("isRoom", isRoom);
        param.put("type", "text");
        return OkhttpUtil.postJSON("/webhook/msg", param);
    }

    /**
     * 发送文件消息
     */
    public static JSONObject postFile(String ats, String fileUrl, boolean isRoom) {
        JSONObject param = new JSONObject();
        param.put("to", ats);
        param.put("content", fileUrl);
        param.put("isRoom", isRoom);
        param.put("type", "fileUrl");
        return OkhttpUtil.postJSON("/webhook/msg", param);
    }

}
