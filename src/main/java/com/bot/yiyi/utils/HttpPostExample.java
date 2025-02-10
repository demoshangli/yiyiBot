package com.bot.yiyi.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Random;


public class HttpPostExample {
    public static String crazy() {
        // 目标URL
        String url = "https://whiteverse.com/scripts/php/crazylg.php";

        // 请求参数
        Random random = new Random();
        int num = random.nextInt(34) + 1;
        String id = String.valueOf(num); // 示例ID参数

        // 构造HTTP请求
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded") // POST请求头
                .POST(HttpRequest.BodyPublishers.ofString("id=" + id)) // 参数
                .build();

        try {
            // 发送请求并接收响应
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // 将响应解析为JSONArray
            JSONArray jsonArray = JSON.parseArray(response.body());
            // 遍历响应数组
            for (Object obj : jsonArray) {
                return String.valueOf(obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "我现在不想发电";
    }

    public static String rainbow(String name) {
        // 目标URL
        String url = "https://tools.kalvinbg.cn/txt/rainbowfart";

        // 请求参数
        Random random = new Random();
        int num = random.nextInt(100000) + 1;
        String value = String.valueOf(num); // 示例ID参数

        // 构造带查询参数的 URL
        String urlWithParams = String.format("%s?random=%s&name=%s", url, value, name);

        // 创建 HttpClient 和 GET 请求
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlWithParams)) // URL 中已包含查询参数
                .GET() // 使用 GET 方法
                .build();

        try {
            // 发送请求并接收响应
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println("原始响应: " + response.body());
            // 将响应解析为JSONArray
//            JSONArray jsonArray = JSON.parseArray(response.body());
//            // 遍历响应数组
//            for (Object obj : jsonArray) {
//                return String.valueOf(obj);
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "我现在不想发电";
    }

    public static String joinUrl(String baseUrl, String path) {
        // 确保 baseUrl 末尾有 '/'，确保 path 开头没有 '/'
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        return baseUrl + path;
    }
}
