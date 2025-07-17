package com.bot.yiyi.plugin;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.bot.yiyi.Pojo.ReturnType;
import com.bot.yiyi.utils.AtUtil;
import com.bot.yiyi.utils.HttpPostExample;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.SneakyThrows;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.*;
import java.util.concurrent.CountDownLatch;

import static com.bot.yiyi.Pojo.AtBot.AT_BOT;

@Component
public class ServerPlugin extends BasePlugin {

    @Autowired
    private ReturnType returnType;

    @SneakyThrows
    @Override
    public int onGroupMessage(Bot bot, GroupMessageEvent event) {
        Set<String> serverSet = new HashSet<>(Arrays.asList("!teams", AT_BOT + "!teams", "！teams", AT_BOT + "！teams"));
        if (serverSet.contains(event.getMessage())) {
            CountDownLatch latch = new CountDownLatch(1);
            String[] serverInfo = new String[1];
            WebSocketClient webSocketClient = new WebSocketClient(new URI("ws://139.224.195.142:1001/")) {
                @Override
                public void onOpen(ServerHandshake serverHandshake) {

                }

                @Override
                public void onMessage(String s) {
                    serverInfo[0] = s;
                    latch.countDown();
                }

                @Override
                public void onClose(int i, String s, boolean b) {

                }

                @Override
                public void onError(Exception e) {

                }
            };
            webSocketClient.connect();
            latch.await();
            webSocketClient.close();
            String[] serverData = serverInfo[0].split("\\|@\\|");
            int validLength = Math.max(0, 8);
            List<String> msgList = new ArrayList<>();
            for (int i = 0; i < validLength; i++) {
                JSONObject server = JSONObject.parseObject(serverData[i]);

                // 获取服务器信息
                String imagePath = HttpPostExample.joinUrl("http://139.224.195.142", server.getString("imagePath"));
                String name = server.getString("name");
                String map = server.getString("map_cn");
                int maxPlayers = server.getIntValue("maxplayers");
                int numPlayers = server.getIntValue("numplayers");
                String ip = server.getString("ip_address");

                // 获取玩家列表并输出
                JSONArray players = server.getJSONArray("players");
                StringBuilder playerInfo = new StringBuilder();
                for (int j = 0; j < players.size(); j++) {
                    JSONObject player = players.getJSONObject(j);
                    String playerName = player.getString("name");
                    int score = player.getIntValue("score");
                    String time = player.getString("time");
                    playerInfo.append("\n").append(score).append("|").append(playerName).append("|").append(time);
                }

                String msg = MsgUtils.builder().img(imagePath).text("\n服务器名称: " + name)
                        .text("\n地图: " + map).text("\n最大玩家数: " + maxPlayers).text("\n当前玩家数: " + numPlayers)
                        .text("\nIP 地址: " + ip).text("\n" + playerInfo).build();
                msgList.add(msg);
            }
            bot.sendGroupForwardMsg(event.getGroupId(), AtUtil.toForward("依依", bot.getSelfId(), msgList));
            return returnType.IGNORE_FALSE(event.getMessageId());
        }
        return MESSAGE_IGNORE;
    }
}
