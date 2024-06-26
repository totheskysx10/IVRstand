package com.good.ivrstand;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class IvrStandApplication {

	public static void main(String[] args) {
		SpringApplication.run(IvrStandApplication.class, args);
	}

}
