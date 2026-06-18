package com.role.net.gogather;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = "com.role.net")
@EnableAsync
public class GoGatherApplication {

	public static void main(String[] args) {
		SpringApplication.run(GoGatherApplication.class, args);
	}

}
