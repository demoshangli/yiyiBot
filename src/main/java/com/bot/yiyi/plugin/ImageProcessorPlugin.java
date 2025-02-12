package com.bot.yiyi.plugin;

import com.bot.yiyi.Pojo.ReturnType;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.imageio.ImageIO;

@Component
public class ImageProcessorPlugin extends BotPlugin {
    private static final List<String> el1 = Arrays.asList("废墟", "深海", "反应堆", "学园", "腐烂", "东京", "三维", "四次元", "少管所", "流星", "闪光", "南极", "消极", "幽浮", "网路", "暗狱", "离子态", "液态", "黑色", "抱抱", "暴力", "垃圾", "社会", "残暴", "残酷", "工口", "戮尸", "原味", "毛茸茸", "香香", "霹雳", "午夜", "美工刀", "爆浆", "机关枪", "无响应", "手术台", "麻风病", "虚拟", "速冻", "智能", "2000", "甜味", "华丽", "反社会", "玛利亚", "无", "梦之", "蔷薇", "无政府", "酷酷", "西伯利亚", "人造", "法外", "追杀", "通缉", "女子", "微型", "男子", "超", "毁灭", "大型", "绝望", "阴间", "死亡", "坟场", "高科技", "奇妙", "魔法", "极限", "社会主义", "无聊");
    private static final List<String> el2 = Arrays.asList("小丑", "仿生", "纳米", "原子", "丧", "电子", "十字架", "咩咩", "赛博", "野猪", "外星", "窒息", "变态", "触手", "小众", "悲情", "飞行", "绿色", "电动", "铁锈", "碎尸", "电音", "蠕动", "酸甜", "虚构", "乱码", "碳水", "内脏", "脑浆", "血管", "全裸", "绷带", "不合格", "光滑", "标本", "酸性", "碱性", "404", "变身", "反常", "樱桃", "碳基", "矫情", "病娇", "进化", "潮湿", "砂糖", "高潮", "变异", "复合盐", "伏特加", "抑郁", "暴躁", "不爱说话", "废物", "失败", "幻想型", "社恐", "苦涩", "粘液", "浓厚", "快乐", "强制", "中二病", "恶魔", "emo", "激光", "发射", "限量版", "迷因", "堕落", "放射性");
    private static final List<String> el3 = Arrays.asList("天使", "精灵", "女孩", "男孩", "宝贝", "小妈咪", "虫", "菇", "公主", "少女", "少年", "1号机", "子", "恐龙", "蜈蚣", "蟑螂", "食人鱼", "小飞船", "舞女", "桃子", "团子", "精", "酱", "废料", "生物", "物质", "奶茶", "搅拌机", "液", "火锅", "祭司", "体", "实验品", "试验体", "小猫咪", "样本", "颗粒", "血块", "汽水", "蛙", "软体", "机器人", "人质", "小熊", "圣母", "胶囊", "乙女", "主义者", "屑", "垢", "污渍", "废人", "毛血旺", "怪人", "肉", "河豚", "豚", "藻类", "唾沫", "咒语", "建筑", "球", "小狗", "碳", "元素", "少先队员", "博士", "糖", "八八鱼");
    private static final Color[] colors = {new Color(0, 0, 255), new Color(255, 0, 247), new Color(0, 204, 102)};
    private static final Random random = new Random();

    @Override
    public int onGroupMessage(Bot bot, GroupMessageEvent event) {
        if (event.getMessage().equals("亚名")) {
            processAndSendImage("https://q1.qlogo.cn/g?b=qq&nk=" + event.getUserId() + "&s=640", bot, event);
            return ReturnType.IGNORE_FALSE();
        }
        Pattern pattern = Pattern.compile("亚名\\[CQ:at,qq=(\\d+)]|\\[CQ:at,qq=(\\d+)]\\s*亚名");
        Matcher matcher = pattern.matcher(event.getMessage());
        if (matcher.matches()) {
            String userId = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
                processAndSendImage("https://q1.qlogo.cn/g?b=qq&nk=" + userId + "&s=640", bot, event);
            return ReturnType.IGNORE_FALSE();
        }
        return ReturnType.IGNORE_TRUE();
    }

    public static void processAndSendImage(String imgUrl, Bot bot, GroupMessageEvent event) {
        File tempInput = null;
        File tempOutput = null;
        
        try {
            tempInput = File.createTempFile("input-", ".jpg");
            ImageIO.write(ImageIO.read(new URL(imgUrl)), "jpg", tempInput);

            BufferedImage original = ImageIO.read(tempInput);
            BufferedImage processed = processImage(original);

            tempOutput = File.createTempFile("output-", ".jpg");
            ImageIO.write(processed, "jpg", tempOutput);

            String msg = MsgUtils.builder().img(tempOutput.getAbsolutePath()).build();
            bot.sendGroupMsg(event.getGroupId(), msg, false);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (tempInput != null) tempInput.delete();
            if (tempOutput != null) tempOutput.delete();
        }
    }

    private static BufferedImage processImage(BufferedImage original) {
        int canvasWidth = 900;
        int canvasHeight = 900;

        BufferedImage canvas = new BufferedImage(canvasWidth, canvasHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = canvas.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setColor(Color.WHITE);
        g.fillRect(0, 0, canvasWidth, canvasHeight);

        int imgWidth = 600;
        int imgHeight = 600;
        int imgX = 260;
        int imgY = (canvasWidth - imgWidth) / 2;
        g.drawImage(original, imgX, imgY, imgWidth, imgHeight, null);

        // 绘制固定文字
        drawSubtitle(g, "您的亚名是", 20, 520);

        String name = generateName();
        drawName(g, name, 20, 660);

        g.dispose();
        return canvas;
    }

    private static String generateName() {
        Collections.shuffle(el1);
        Collections.shuffle(el2);
        Collections.shuffle(el3);
        return el1.get(0) + el2.get(0) + el3.get(0);
    }

    private static void drawName(Graphics2D g, String text, int x, int y) {
        int size = 100;
        if (text.length() > 8) {
            size = 80;
        }
        Font font = new Font("Microsoft YaHei", Font.BOLD, size);
        g.setFont(font);

        g.setColor(Color.WHITE);
        g.setStroke(new BasicStroke(4));
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                g.drawString(text, x + i, y + j);
            }
        }

        g.setColor(colors[random.nextInt(colors.length)]);
        g.drawString(text, x, y);
    }

    private static void drawSubtitle(Graphics2D g, String text, int x, int y) {
        Font subtitleFont = new Font("Microsoft YaHei", Font.BOLD, 40);
        g.setFont(subtitleFont);
        g.setColor(new Color(105, 105, 105)); // 深灰色
        g.drawString(text, x, y);
    }
}
