package com.platform.workerservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.kafka.listener.auto-startup=false"
})
class WorkerServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
