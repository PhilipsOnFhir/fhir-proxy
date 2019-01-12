package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.fhircast.erik.hub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
        Hub.runVerification();
    }
}
