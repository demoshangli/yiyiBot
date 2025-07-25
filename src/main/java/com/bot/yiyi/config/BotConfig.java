package com.bot.yiyi.config;

import com.bot.yiyi.mapper.AdminMapper;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.MessageEvent;
import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Data
public class BotConfig {

    private final String OWNER = "owner";
    private final String ADMIN = "admin";
    private List<Map<String, Object>> ownerQQ;

    @Autowired
    private AdminMapper adminMapper;

    @PostConstruct
    public void init() {
        // 启动时加载数据库中所有管理员
        this.ownerQQ = adminMapper.getAllAdmins();
    }

    public boolean isAdmin(Long userId) {
        for (Map<String, Object> ownerQQ : ownerQQ) {
            if (ownerQQ.get("id").equals(userId)) {
                return true;
            }
        }
        return false;
    }

    public boolean isOwner(Long userId) {
        for (Map<String, Object> ownerQQ : ownerQQ) {
            if (ownerQQ.get("id").equals(userId) && OWNER.equals(ownerQQ.get("role"))) {
                return true;
            }
        }
        return false;
    }

    public boolean isGroupAdmin(MessageEvent event, Bot bot) {
        String role = bot.getGroupMemberInfo(((GroupMessageEvent) event).getGroupId(), event.getUserId(), true).getData().getRole();
        return isAdmin(event.getUserId()) || role.equals("owner") || role.equals("admin");
    }

    @Data
    class AdminInfo {
        private Long id;
        private String role;
    }
}
