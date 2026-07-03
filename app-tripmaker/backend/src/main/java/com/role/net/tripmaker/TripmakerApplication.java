package com.role.net.tripmaker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
    "com.role.net.tripmaker",
    "gogather.framework.group.jpa"
})
@EntityScan(basePackages = {
    "com.role.net.tripmaker.entity",
    "gogather.framework.group.jpa.domain"
})
@EnableJpaRepositories(basePackages = {
    "com.role.net.tripmaker.repository",
    "gogather.framework.group.jpa.repository"
})
public class TripmakerApplication {

	public static void main(String[] args) {
		SpringApplication.run(TripmakerApplication.class, args);
	}

}