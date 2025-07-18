package com.bot.yiyi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "bot")
@Data
public class BotConfig {
    private Long[] ownerQQ;

    public boolean isOwnerQQ(Long userId) {
        for (Long ownerQQ : ownerQQ) {
            if (ownerQQ.equals(userId)) {
                return true;
            }
        }
        return false;
    }
}
