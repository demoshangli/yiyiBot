//package com.bot.yiyi.plugin;
//
//import javax.imageio.ImageIO;
//import java.awt.*;
//import java.awt.image.BufferedImage;
//import java.io.File;
//import java.text.SimpleDateFormat;
//import java.util.*;
//import java.util.List;
//
//public class GroupMenuImageGenerator {
//
//    // 模拟群信息
//    static class GroupInfo {
//        String groupId = "123456789";
//        String groupName = "测试群聊";
//        boolean isAdmin = true;
//    }
//
//    // 菜单项数据
//    static Map<String, List<String>> menuSections = new LinkedHashMap<>() {{
//        put("游戏模块", List.of(
//                "随机古诗 —— 随机一句古诗。",
//                "随机一言 —— 随机一句鸡汤。",
//                "随机语录 —— 随机一句社会语录。",
//                "毒鸡汤 —— 随机一句毒鸡汤。",
//                "舔狗日记 —— 随机舔狗日记。",
//                "亚名 —— 高科技亚文化取名机。",
//                "曼波 —— 有小马的语音包哦。",
//                "@依依 —— 依依主动对你发癫。",
//                "@+发癫 —— 你对@的人发癫。",
//                "@+发癫+@ —— 指定某人对另一个人发癫。"
//        ));
//        put("积分模块", List.of(
//                "打卡 / 签到 —— 每日签到领取积分。",
//                "积分抽奖 / 积分赌狗 —— 使用积分进行抽奖或赌积分。",
//                "我的积分 / 积分 —— 查询自己的当前积分。",
//                "存储积分+数量 —— 存入指定数量的积分。",
//                "取出积分+数量 —— 取出存储的积分。",
//                "赠送积分+数量+@ —— 将指定数量的积分赠送给某人。",
//                "富豪榜 —— 查看全群积分最高的用户。",
//                "负豪榜 —— 查看全群积分最低的用户。",
//                "群富豪榜 —— 查看群内积分前几名的用户。",
//                "群负豪榜 —— 查看群内积分最低的用户。"
//        ));
//        put("结婚模块", List.of(
//                "娶群友 / 娶老婆 —— 随机迎娶某人。",
//                "嫁群友 / 嫁老公 —— 随机嫁给某人。",
//                "娶+@ —— 向指定用户求婚。",
//                "嫁+@ —— 向指定用户申请嫁过去。",
//                "强娶+@ —— 不管对方同不同意，强行娶走。",
//                "硬嫁+@ —— 不管对方愿不愿意，强行嫁过去。",
//                "抢老婆+@ —— 抢走别人的老婆。",
//                "抢老公+@ —— 抢走别人的老公。",
//                "我的老婆 / 我的老公 —— 查询自己的配偶信息。",
//                "闹离婚 / 闹分手 —— 解除关系。"
//        ));
//    }};
//
//    // 各模块开关状态
//    static Map<String, Boolean> moduleStatus = new HashMap<>() {{
//        put("游戏模块", true);
//        put("积分模块", true);
//        put("结婚模块", false);
//    }};
//
//    public static void main(String[] args) throws Exception {
//        GroupInfo group = new GroupInfo();
//        generateGroupMenuImage(group, "group_menu.png");
//    }
//
//    public static void generateGroupMenuImage(GroupInfo group, String filePath) throws Exception {
//        int width = 1000;
//        int height = 2400;
//        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
//        Graphics2D g = image.createGraphics();
//
//        // 白底 + 抗锯齿
//        g.setColor(Color.WHITE);
//        g.fillRect(0, 0, width, height);
//        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//
//        Font titleFont = new Font("SansSerif", Font.BOLD, 32);
//        Font sectionFont = new Font("SansSerif", Font.BOLD, 26);
//        Font textFont = new Font("SansSerif", Font.PLAIN, 22);
//
//        int x = 50, y = 60;
//
//        // 群名称标题
//        g.setFont(titleFont);
//        g.setColor(new Color(0, 102, 204));
//        g.drawString(group.groupName + "（" + group.groupId + "）菜单", x, y);
//
//        y += 45;
//        g.setFont(textFont);
//        g.setColor(Color.GRAY);
//        g.drawString("生成时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()), x, y);
//
//        // 模块状态展示
//        y += 35;
//        g.setFont(textFont);
//        g.drawString("模块状态：", x, y);
//        int statusX = x + 100;
//        for (Map.Entry<String, Boolean> entry : moduleStatus.entrySet()) {
//            g.setColor(entry.getValue() ? new Color(0, 160, 0) : new Color(180, 0, 0));
//            g.drawString(entry.getKey(), statusX, y);
//            statusX += g.getFontMetrics().stringWidth(entry.getKey()) + 40;
//        }
//
//        y += 40;
//        g.setColor(group.isAdmin ? new Color(0, 160, 0) : new Color(200, 0, 0));
//        g.drawString(group.isAdmin ? "✅ 您是群主或管理员，可以切换模块状态" : "⚠️ 只有群主或管理员可以切换模块状态", x, y);
//
//        y += 50;
//        g.setFont(sectionFont);
//
//        for (Map.Entry<String, List<String>> entry : menuSections.entrySet()) {
//            String module = entry.getKey();
//            boolean enabled = moduleStatus.getOrDefault(module, true);
//
//            g.setColor(enabled ? new Color(0, 150, 0) : new Color(180, 0, 0));
//            g.drawString(module + (enabled ? "（开启）" : "（关闭）"), x, y);
//            y += 35;
//
//            g.setFont(textFont);
//            g.setColor(Color.BLACK);
//            for (String line : entry.getValue()) {
//                y = drawWrappedText(g, line, x + 20, y, 900, textFont);
//            }
//            y += 20;
//            g.setFont(sectionFont);
//        }
//
//        g.dispose();
//        ImageIO.write(image, "png", new File(filePath));
//        System.out.println("菜单图片已生成！");
//    }
//
//    // 绘制自动换行文本
//    private static int drawWrappedText(Graphics2D g, String text, int x, int y, int maxWidth, Font font) {
//        FontMetrics fm = g.getFontMetrics(font);
//        int lineHeight = fm.getHeight();
//        String[] words = text.split(" ");
//        StringBuilder line = new StringBuilder();
//
//        for (String word : words) {
//            String testLine = line + word + " ";
//            if (fm.stringWidth(testLine) > maxWidth) {
//                g.drawString(line.toString(), x, y);
//                y += lineHeight + 4;
//                line = new StringBuilder(word + " ");
//            } else {
//                line.append(word).append(" ");
//            }
//        }
//        g.drawString(line.toString(), x, y);
//        return y + lineHeight + 4;
//    }
//}
