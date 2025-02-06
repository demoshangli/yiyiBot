package com.bot.yiyi.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class OperationUtils {

    /**
     * 根据传入的值，返回一个Map，包含不同操作对应的计算结果。
     *
     * @param input 初始值
     * @return 返回包含操作和结果的Map
     */
    public static double calculateResult(double input, String operation) {
        switch (operation) {
            case "+10":
                return input + 10;
            case "+50":
                return input + 50;
            case "+100":
                return input + 100;
            case "+1000":
                return input + 1000;
            case "+10000":
                return input + 10000;
            case "+5%":
                return input + input * 0.05;
            case "+10%":
                return input + input * 0.10;
            case "+20%":
                return input + input * 0.20;
            case "+50%":
                return input + input * 0.50;
            case "+100%":
                return input + input * 1.00;
            case "-10":
                return input - 10;
            case "-50":
                return input - 50;
            case "-100":
                return input - 100;
            case "-1000":
                return input - 1000;
            case "-10000":
                return input - 10000;
            case "-5%":
                return input - input * 0.05;
            case "-10%":
                return input - input * 0.10;
            case "-20%":
                return input - input * 0.20;
            case "-50%":
                return input - input * 0.50;
            case "-100%":
                return input - input * 1.00;
            default:
                throw new IllegalArgumentException("Invalid operation: " + operation);
        }
    }

    /**
     * 执行抽奖操作，根据概率随机选择一个操作
     *
     * @param input 初始值
     * @return 返回一个Map，包含抽奖操作及其结果
     */
    public static Map<String, Integer> luckyDraw(double input) {
        Map<String, Integer> resultMap = new HashMap<>();

        // 所有操作
        String[] possibleResults = {
            "+10", "+50", "+100", "+1000", "+10000", "+5%", "+10%", "+20%", "+50%", "+100%",
            "-10", "-50", "-100", "-1000", "-10000", "-5%", "-10%", "-20%", "-50%", "-100%"
        };

        // 概率区间：概率之和为 9.5 + 7.5 + 5 + 2.5 + 0.5 = 25
        double[] probabilities = {
            9.5, 7.5, 5, 2.5, 0.5,  // +10, +50, +100, +1000, +10000
            9.5, 7.5, 5, 2.5, 0.5,  // +5%, +10%, +20%, +50%, +100%
            9.5, 7.5, 5, 2.5, 0.5,  // -10, -50, -100, -1000, -10000
            9.5, 7.5, 5, 2.5, 0.5   // -5%, -10%, -20%, -50%, -100%
        };

        // 计算总概率值
        double totalWeight = 0;
        for (double prob : probabilities) {
            totalWeight += prob;
        }

        // 生成一个随机数，范围在 0 到 totalWeight 之间
        Random random = new Random();
        double randomIndex = random.nextDouble() * totalWeight;

        // 查找抽中的操作
        double cumulativeWeight = 0;
        String selectedOperation = null;
        for (int i = 0; i < possibleResults.length; i++) {
            cumulativeWeight += probabilities[i];
            if (randomIndex < cumulativeWeight) {
                selectedOperation = possibleResults[i];
                break;
            }
        }

        // 获取抽奖操作的计算结果
        double result = calculateResult(input, selectedOperation);

        // 将选中的操作和对应结果存入Map
        int roundedResult = (int) Math.round(result);
        resultMap.put(selectedOperation, roundedResult);

        return resultMap;
    }

}
