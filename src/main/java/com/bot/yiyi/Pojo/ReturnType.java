package com.bot.yiyi.Pojo;

import com.mikuac.shiro.core.BotPlugin;

public class ReturnType extends BotPlugin {

    private static Boolean match;

    public static Boolean getMatch() {
        return match;
    }

    public static int IGNORE_TRUE() {
        match = true;
        return MESSAGE_IGNORE;
    }

    public static int BLOCK_TRUE() {
        match = true;
        return MESSAGE_BLOCK;
    }

    public static int IGNORE_FALSE() {
        match = false;
        return MESSAGE_IGNORE;
    }

    public static int BLOCK_FALSE() {
        match = false;
        return MESSAGE_BLOCK;
    }
}
