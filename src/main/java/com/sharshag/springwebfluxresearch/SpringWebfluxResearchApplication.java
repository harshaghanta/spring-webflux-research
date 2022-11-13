package com.sharshag.springwebfluxresearch;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;

import reactor.blockhound.BlockHound;

@SpringBootApplication
public class SpringWebfluxResearchApplication {

	static {
		BlockHound.install(
			builder -> {
				builder.allowBlockingCallsInside("java.util.UUID", "randonUUID")
				.allowBlockingCallsInside("java.util.zip.InflaterInputStream", "read")			
				// .allowBlockingCallsInside("java.io.InputStream","readNBytes")
				.allowBlockingCallsInside("org.springdoc.core.OpenAPIService","initializeHiddenRestController");
			}
		);
	}

	public static void main(String[] args) {
		System.out.println(PasswordEncoderFactories.createDelegatingPasswordEncoder().encode("devdojo"));
		SpringApplication.run(SpringWebfluxResearchApplication.class, args);

		// new DevDojoUser().getu
	}

}
