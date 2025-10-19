package com.wtplanner.wtbooking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WtBookingApplication {

    public static void main(String[] args) {
        SpringApplication.run(WtBookingApplication.class, args);
    }

}
