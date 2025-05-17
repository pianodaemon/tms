package com.agnux.tms.core.aipc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.agnux.tms"})
public class AIPCApplication {

    public static void main(String[] args) {
        SpringApplication.run(AIPCApplication.class, args);
    }

}
