package com.bot.yiyi.config;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@MapperScan("com.bot.yiyi.mapper")  // 扫描 Mapper
public class MyBatisConfig {
}