package com.philips.research.philipsonfhir.fhirproxy.dstu3.applications.clinicalreasoning.servlet;

import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.hapi.JpaServerDemo;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.annotation.WebServlet;

@WebServlet(urlPatterns = "/hapi/*")
public class HapiServlet extends JpaServerDemo {
    public HapiServlet(WebApplicationContext myAppCtx) {
        super(myAppCtx);
    }
}
