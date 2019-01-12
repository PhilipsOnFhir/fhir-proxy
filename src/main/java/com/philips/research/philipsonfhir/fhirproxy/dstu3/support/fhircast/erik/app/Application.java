package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.fhircast.erik.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
        App.sendSubscribeRequest("http://127.0.0.1:80/callback", "subscribe","123","open-chart");    }
}
