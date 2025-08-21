package dev.muho.batch.pricing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PricingBatchApplication {

	public static void main(String[] args) {
		SpringApplication.run(PricingBatchApplication.class, args);
	}

}
