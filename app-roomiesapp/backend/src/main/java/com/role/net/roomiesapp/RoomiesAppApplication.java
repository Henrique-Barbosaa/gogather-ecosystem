package com.role.net.roomiesapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {
    "com.role.net.roomiesapp",
    "gogather.framework.group.jpa"
})
@EntityScan(basePackages = {
    "com.role.net.roomiesapp.entity",
    "gogather.framework.group.jpa.domain"
})
@EnableJpaRepositories(basePackages = {
    "com.role.net.roomiesapp.repository",
    "gogather.framework.group.jpa.repository"
})
public class RoomiesAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(RoomiesAppApplication.class, args);
	}

}