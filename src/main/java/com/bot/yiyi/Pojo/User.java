package com.bot.yiyi.Pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {
    private String id;
    private int money;
    private LocalDate lastCheckTime;
    private int bank;
    private String lotteryTime;
}
