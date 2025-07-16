package com.bot.yiyi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


@SpringBootApplication
@EnableRetry
@EnableScheduling
public class YiyiApplication {

	public static final String token = "Ex79x_crDXU4";

	public static void main(String[] args) {
		SpringApplication.run(YiyiApplication.class, args);
//		Starter starter = new Starter("102708012", "Q0izdn1d8NBXEHaytE0CkdbfPRAZ2Ckt", "4vmdULD5xphZSLE70tmgaUOIC61wrmhc");
//		//===================================公域推荐订阅===============↓群聊/好友 事件订阅
//		starter.getConfig().setCode(Intents.PUBLIC_INTENTS.and(Intents.GROUP_INTENTS));
//		starter.run();
//		starter.registerListenerHost(new ListenerHost() {
//
//			@EventReceiver
//			public void onMessage(MessageChannelReceiveEvent event) {
//				MessageAsyncBuilder builder = new MessageAsyncBuilder();
//				builder.append("?");
//				event.send(builder.build());
//			}
//
//			/**
//			 * 因为是公域 所以仅当bot被at时才能触发事件
//			 * @param event
//			 */
//			@EventReceiver
//			public void onMessage(GroupMessageEvent event) throws InterruptedException, URISyntaxException {
//				CountDownLatch latch = new CountDownLatch(1);
//				String[] serverInfo = new String[1];
//				WebSocketClient webSocketClient = new WebSocketClient(new URI("ws://139.224.195.142:1001/")) {
//					@Override
//					public void onOpen(ServerHandshake serverHandshake) {
//
//					}
//
//					@Override
//					public void onMessage(String s) {
//						serverInfo[0] = s;
//						latch.countDown();
//					}
//
//					@Override
//					public void onClose(int i, String s, boolean b) {
//
//					}
//
//					@Override
//					public void onError(Exception e) {
//
//					}
//				};
//				webSocketClient.connect();
//				latch.await();
//				webSocketClient.close();
//				String serverName = null;
//				boolean b = false;
//				String[] serverData = serverInfo[0].split("\\|@\\|");
//				System.out.println(event.getMessage().get(0));
//				MessageAsyncBuilder builder = new MessageAsyncBuilder();
//				String shell = event.getMessage().get(0).toString().trim();
//				if (shell.equals("/teams1")) {
//					serverName = "1服";
//					b = true;
//				} else if (shell.equals("/teams2")) {
//					serverName = "2服";
//					b = true;
//				} else if (shell.equals("/teams3")) {
//					serverName = "3服";
//					b = true;
//				} else if (shell.equals("/teams4")) {
//					serverName = "4服";
//					b = true;
//				} else if (shell.equals("/teams5")) {
//					serverName = "5服";
//					b = true;
//				} else if (shell.equals("/teams6")) {
//					serverName = "6服";
//					b = true;
//				} else if (shell.equals("/teams7")) {
//					serverName = "7服";
//					b = true;
//				} else if (shell.equals("/teams8")) {
//					serverName = "8服";
//					b = true;
//				}
//				if (b) {
//					for (int i = 0; i < serverData.length; i++) {
//						JSONObject server = JSONObject.parseObject(serverData[i]);
//						String name = server.getString("name");
//						if (serverName != null) {
//							if (!name.contains(serverName)) continue;
//						}
//						downloadImageIfNotExists(HttpPostExample.joinUrl(server.getString("imagePath")));
//						String imagePath = HttpPostExample.joinUrl("https://wuyao.love/qcimg", server.getString("imagePath"));
//						String map = server.getString("map_cn");
//						int maxPlayers = server.getIntValue("maxplayers");
//						int numPlayers = server.getIntValue("numplayers");
//						String ip = server.getString("ip_address");
//
//						// 获取玩家列表并输出
//						JSONArray players = server.getJSONArray("players");
//						StringBuilder playerInfo = new StringBuilder();
//						for (int j = 0; j < players.size(); j++) {
//							JSONObject player = players.getJSONObject(j);
//							String playerName = player.getString("name");
//							int score = player.getIntValue("score");
//							String time = player.getString("time");
//							playerInfo.append("\n").append(score).append("|").append(playerName).append("|").append(time);
//						}
//						String msg = MsgUtils.builder().text("\n服务器名称: " + name)
//								.text("\n地图: " + map).text("\n最大玩家数: " + maxPlayers).text("\n当前玩家数: " + numPlayers)
//								.text("\nIP 地址: " + ip).text("\n" + playerInfo).build();
//						builder.append(msg);
//						builder.append(new Image(imagePath));
//						event.send(builder.build());
//						return;
//					}
//				}
//				StringBuilder msg = new StringBuilder();
//				for (int i = 0; i < serverData.length; i++) {
//					JSONObject server = JSONObject.parseObject(serverData[i]);
//					String name = server.getString("name");
//					if (!name.contains("14人")) continue;
//					String map = server.getString("map_cn");
//					int maxPlayers = server.getIntValue("maxplayers");
//					int numPlayers = server.getIntValue("numplayers");
//					msg.append("\n").append(name).append("\n").append(map).append("  ").append(numPlayers).append("/").append(maxPlayers);
//				}
//				builder.append(String.valueOf(msg));
//				event.send(builder.build());
//			}
//		});
	}

	static String Url = "http://139.224.195.142"; // 替换为实际的图片URL
	static String saveDir = "/usr/local/nginx/html/qcimg"; // 指定保存目录


	public static void downloadImageIfNotExists(String fileName) {
		String imageUrl = Url + fileName;
		Path savePath = Paths.get(saveDir, fileName);

		// 检查文件是否存在
		if (Files.exists(savePath)) {
			return;
		}

		// 创建目录（如果不存在）
		try {
			Files.createDirectories(Paths.get(saveDir));
		} catch (IOException e) {
			System.err.println("无法创建目录: " + e.getMessage());
			return;
		}

		// 下载文件
		try {
			URL url = new URL(imageUrl);
			Files.copy(url.openStream(), savePath);
			System.out.println("下载成功: " + savePath);
		} catch (IOException e) {
			System.err.println("下载失败: " + e.getMessage());
		}
	}

}
