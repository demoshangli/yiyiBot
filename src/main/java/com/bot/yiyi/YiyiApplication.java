package com.bot.yiyi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

@SpringBootApplication
@EnableRetry
public class YiyiApplication {

	public static void main(String[] args) {
		SpringApplication.run(YiyiApplication.class, args);
	}

}
