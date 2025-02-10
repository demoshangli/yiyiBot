package com.bot.yiyi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "bot.owner")
@Data
public class BotConfig {
    private Long qq;
}
