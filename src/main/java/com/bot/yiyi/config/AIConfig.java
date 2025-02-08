package com.bot.yiyi.config;

import com.bot.yiyi.Pojo.Model;
import com.bot.yiyi.Pojo.Role;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static com.bot.yiyi.Pojo.Role.*;
import static com.bot.yiyi.Pojo.Model.*;

@Configuration
public class AIConfig {

    private static String role = DEFAULT.getRole();

    private static String model = R1.getModel();

    public static void setRole(Role role) {
        AIConfig.role = role.getRole();
    }

    public static String getRole() {
        return AIConfig.role;
    }

    public static void setModel(Model model) {
        AIConfig.model = model.getModel();
    }

    public static String getModel() {
        return AIConfig.model;
    }

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.defaultSystem(AIConfig.role)
//                .defaultOptions(ChatOptions.builder().model(AIConfig.model).build())
                .build();
    }
}