package com.philips.research.philipsonfhir.fhirproxy.dstu3.applications.clinicalreasoning.controller;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.NotImplementedException;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.clinicalreasoning.*;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.controller.SampleFhirGateway;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.service.FhirServer;
import org.hl7.fhir.exceptions.FHIRException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Configuration
public class MyController extends SampleFhirGateway {
    private static final String url = "http://localhost:9500/baseDstu3";
    private String prop = null;

    public MyController() throws FHIRException, NotImplementedException {
        super();
//        setFhirServer(url);
    }

    @EventListener
    public void onWebServerInitialized(WebServerInitializedEvent event) throws FHIRException {
        System.out.println(event);
        int port = event.getWebServer().getPort();
        setFhirServer( "http://localhost:"+port+"/hapi" );
    }

    private void setFhirServer(String urlll) throws FHIRException {
        System.out.println("\n==Set fhirserver=============== " + urlll + "++++++++++++++++++");

        this.fhirServer = new FhirServer(urlll);
        FhirContext ourCtx = this.fhirServer.getCtx();
        IGenericClient client = this.fhirServer.getCtx().newRestfulGenericClient(url);

        this.fhirServer.getFhirOperationRepository().registerOperation(new MeasureEvaluationOperation(client));
        this.fhirServer.getFhirOperationRepository().registerOperation(new StructureMapTransformOperation(url, client));
        this.fhirServer.getFhirOperationRepository().registerOperation(new PlanDefinitionApplyOperation(url));
        this.fhirServer.getFhirOperationRepository().registerOperation(new ActivityDefinitionApplyOperation(url));
        this.fhirServer.getFhirOperationRepository().registerOperation(new QuestionnairePopulateOperation(url));
        System.out.println("\n==Set fhirserver=============== " + urlll + "+++++DONE+++++++++++++");
    }

}

