package com.philips.research.philipsonfhir.fhirproxy.applications.bulkdata.interceptor;

import ca.uhn.fhir.context.FhirContext;
import org.jboss.logging.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class InteractionLogger extends HandlerInterceptorAdapter {
    private static Logger logger = Logger.getLogger( InteractionLogger.class );
    FhirContext ourCtx = FhirContext.forDstu3();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        System.out.println("\n-------- LogInterception.preHandle --- ");
        System.out.println("Request URL: " + request.getRequestURL());
        System.out.println("Start Time: " + System.currentTimeMillis());

        String accept = request.getHeader("Accept");

        return true;
    }
}
