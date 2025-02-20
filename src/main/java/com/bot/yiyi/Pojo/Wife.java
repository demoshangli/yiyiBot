package com.bot.yiyi.Pojo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Wife {

    private String id;
    private Long husband;
    private Long wife;
    private Integer wifeFavorAbility;
    private Integer husbandFavorAbility;
    private LocalDateTime MarryTime;

}
