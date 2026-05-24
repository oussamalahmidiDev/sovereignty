package com.oussama.sovereignty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SovereigntyApplication {

	public static void main(String[] args) {
		SpringApplication.run(SovereigntyApplication.class, args);
	}

}
