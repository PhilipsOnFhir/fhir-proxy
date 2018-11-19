package com.philips.research.philipsonfhir.fhirproxy.dstu3.applications.bulkdata;

import com.philips.research.philipsonfhir.fhirproxy.dstu3.applications.bulkdata.interceptor.InteractionLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class BulkDataAppConfig implements WebMvcConfigurer {
    @Autowired
    InteractionLogger interactionLogger;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor( interactionLogger )
                .addPathPatterns("/**/fhir/**/");
    }
}
