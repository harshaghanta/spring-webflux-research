package com.sharshag.springwebfluxresearch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import reactor.blockhound.BlockHound;

@SpringBootApplication
public class SpringWebfluxResearchApplication {

	static {
		BlockHound.install();
	}

	public static void main(String[] args) {
		SpringApplication.run(SpringWebfluxResearchApplication.class, args);
	}

}
