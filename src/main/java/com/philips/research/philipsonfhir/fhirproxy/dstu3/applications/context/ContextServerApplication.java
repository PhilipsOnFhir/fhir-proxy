package com.philips.research.philipsonfhir.fhirproxy.dstu3.applications.context;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@ServletComponentScan
@SpringBootApplication
public class ContextServerApplication {

    public static void main(String[] args) {
        SpringApplication sa = new SpringApplication( ContextServerApplication.class );
        sa.setLogStartupInfo(false);
        sa.setBannerMode(Banner.Mode.OFF);
        sa.run(args);
    }
}