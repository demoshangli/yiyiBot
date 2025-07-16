package com.bot.yiyi.utils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class HttpPostExample {
    public static String crazy() {
        String url = "https://whiteverse.com/scripts/php/crazylg.php";

        Random random = new Random();
        int num = random.nextInt(34) + 1;
        String id = String.valueOf(num); // 示例ID参数

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/x-www-form-urlencoded") // POST请求头
                .POST(HttpRequest.BodyPublishers.ofString("id=" + id)) // 参数
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JSONArray jsonArray = JSON.parseArray(response.body());
            for (Object obj : jsonArray) {
                return String.valueOf(obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getAncientPoetry() {
        try {
            URL url = new URL("https://api.apiopen.top/api/sentences");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            if (conn.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream())
                );
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject json = JSONObject.parseObject(response.toString());
                if (json.getIntValue("code") == 200) {
                    JSONObject result = json.getJSONObject("result");

                    String name = result.getString("name");
                    String from = result.getString("from");
                    return name + "\n——" + from;
                } else {
                    System.out.println("API 返回错误: " + json.getString("message"));
                }
            } else {
                System.out.println("HTTP 请求失败: " + conn.getResponseCode());
            }

            conn.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getRandomOne() {
        try {
            URL url = new URL("https://api.oick.cn/yiyan/api.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // 2. 处理响应
            if (conn.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), "UTF-8"));
                String response = reader.readLine(); // 直接读取单行文本
                reader.close();

                return response;
            } else {
                System.out.println("请求失败，状态码: " + conn.getResponseCode());
            }

            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String society() {
        try {
            URL url = new URL("https://api.oick.cn/yulu/api.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // 2. 处理响应
            if (conn.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), "UTF-8"));
                String response = reader.readLine(); // 直接读取单行文本
                reader.close();

                return response;
            } else {
                System.out.println("请求失败，状态码: " + conn.getResponseCode());
            }

            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String loveDog() {
        try {
            URL url = new URL("https://api.oick.cn/dog/api.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // 2. 处理响应
            if (conn.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), "UTF-8"));
                String response = reader.readLine(); // 直接读取单行文本
                reader.close();

                return response;
            } else {
                System.out.println("请求失败，状态码: " + conn.getResponseCode());
            }

            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String duTang() {
        try {
            URL url = new URL("https://api.oick.cn/dutang/api.php");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // 2. 处理响应
            if (conn.getResponseCode() == 200) {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream(), "UTF-8"));
                String response = reader.readLine(); // 直接读取单行文本
                reader.close();

                return response;
            } else {
                System.out.println("请求失败，状态码: " + conn.getResponseCode());
            }

            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String joinUrl(String baseUrl, String path) {
        if (!baseUrl.endsWith("/")) {
            baseUrl += "/";
        }
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        return baseUrl + path;
    }

    public static String joinUrl(String path) {
        if (path == null || path.isEmpty()) {
            return "/";
        }
        path = path.replaceAll("^/+", ""); // 去掉前导 /
        return "/" + path;
    }


    public static String getRandomPic() {
        String apiUrl = "https://api.apiopen.top/api/getImages?page=" + 1 + "&size=" + 1 + "&type=comic";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .GET()
                .build();

        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }

        if (response.statusCode() == 200) {
            JSONObject jsonObject = JSON.parseObject(response.body());
            if (jsonObject.getInteger("code") == 200) {
                JSONObject result = jsonObject.getJSONObject("result");
                JSONArray list = result.getJSONArray("list");

                List<String> urls = new ArrayList<>();
                for (int i = 0; i < list.size(); i++) {
                    JSONObject item = list.getJSONObject(i);
                    urls.add(item.getString("url"));
                }
                return urls.get(0);
            }
        }
        return null;
    }
}
