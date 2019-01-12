package com.philips.research.philipsonfhir.fhirproxy.dstu3.applications.bulkdata.servlet;

import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement()
public class FhirServerConfig extends com.philips.research.philipsonfhir.fhirproxy.dstu3.support.hapi.FhirServerConfig {
}