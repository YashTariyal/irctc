package com.irctc_backend.irctc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableAspectJAutoProxy
@EnableCaching
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
public class IrctcApplication {

	public static void main(String[] args) {
		SpringApplication.run(IrctcApplication.class, args);
	}
}