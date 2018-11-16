package com.philips.research.philipsonfhir.fhirproxy.dstu3.applications.bulkdata.testapp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@ServletComponentScan
@SpringBootApplication
public class BulkDataTestApplication {

    public static void main(String[] args) {
        SpringApplication.run( BulkDataTestApplication.class, args );
    }
}