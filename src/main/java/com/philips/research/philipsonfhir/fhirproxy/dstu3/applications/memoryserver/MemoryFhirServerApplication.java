package com.philips.research.philipsonfhir.fhirproxy.dstu3.applications.memoryserver;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@ServletComponentScan
@SpringBootApplication
public class MemoryFhirServerApplication {

    public static void main(String[] args) {
        SpringApplication sa = new SpringApplication( MemoryFhirServerApplication.class );
        sa.setLogStartupInfo(false);
        sa.setBannerMode(Banner.Mode.OFF);
        sa.run(args);
    }
}