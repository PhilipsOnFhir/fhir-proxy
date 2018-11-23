package com.philips.research.philipsonfhir.fhirproxy.dstu3.applications.clinicalreasoning;

import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;


@Configuration
@SpringBootApplication
@EnableAutoConfiguration
@ServletComponentScan
public class ClinicalReasoningTestApplication implements ApplicationRunner{

    public static void main(String[] args) {
        SpringApplication.run( ClinicalReasoningTestApplication.class, args );
    }
    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ClinicalReasoningApplication.class);

    @Override
    public void run(ApplicationArguments args) throws Exception {
        logger.info("Application started with command-line arguments: {}", Arrays.toString(args.getSourceArgs()));
        logger.info("NonOptionArgs: {}", args.getNonOptionArgs());
        logger.info("OptionNames: {}", args.getOptionNames());

        for (String name : args.getOptionNames()){
            logger.info("arg-" + name + "=" + args.getOptionValues(name));
        }
    }
}