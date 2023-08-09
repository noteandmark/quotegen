package com.andmark.quotebot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackages = {
		"com.andmark.quotebot",
		"org.telegram.telegrambots"
})
public class QuoteBotApplication {

	public static void main(String[] args) {
		SpringApplication.run(QuoteBotApplication.class, args);
	}

}
