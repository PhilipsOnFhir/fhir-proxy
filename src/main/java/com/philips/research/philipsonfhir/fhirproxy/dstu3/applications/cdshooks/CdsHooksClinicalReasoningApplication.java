package com.philips.research.philipsonfhir.fhirproxy.dstu3.applications.cdshooks;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAutoConfiguration
public class CdsHooksClinicalReasoningApplication {

    public static void main(String[] args) {
        SpringApplication.run( CdsHooksClinicalReasoningApplication.class, args );
    }
}