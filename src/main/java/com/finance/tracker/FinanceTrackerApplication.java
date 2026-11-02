
package com.finance.tracker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent;
import org.springframework.context.ApplicationListener;

@SpringBootApplication
public class FinanceTrackerApplication {

    public static void main(String[] args) {

        SpringApplication app =
                new SpringApplication(FinanceTrackerApplication.class);

        app.addListeners(
                (ApplicationListener<ApplicationEnvironmentPreparedEvent>) event -> {
                    String uri = event.getEnvironment()
                            .getProperty("spring.data.mongodb.uri");

                    System.out.println("===== MONGODB CONFIG CHECK =====");
                    System.out.println("URI present: " +
                            (uri != null && !uri.isBlank()));
                    System.out.println("Atlas URI: " +
                            (uri != null && uri.startsWith("mongodb+srv://")));
                    System.out.println("===== END CONFIG CHECK =====");
                }
        );

        app.run(args);
    }
}
