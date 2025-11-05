package com.irctc_backend.irctc;

import com.irctc_backend.irctc.config.TestKafkaConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@SpringBootTest
@Import(TestKafkaConfig.class)
class IrctcApplicationTests {

	@Test
	void contextLoads() {
	}

}
