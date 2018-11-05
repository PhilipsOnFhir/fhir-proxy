package com.philips.research.philipsonfhir.fhirproxy.dstu3.applications.clinicalreasoning;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
public class ClinicalReasoningApplication {

    public static void main(String[] args) {
        SpringApplication.run( ClinicalReasoningApplication.class, args );
    }
}