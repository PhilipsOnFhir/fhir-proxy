package com.philips.research.philipsonfhir.fhirproxy.dstu3.applications.cdshooks.controller;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.NotImplementedException;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.clinicalreasoning.*;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.controller.SampleFhirGateway;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.service.FhirServer;
import org.hl7.fhir.exceptions.FHIRException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;

@RestController
@Configuration
public class MyController extends SampleFhirGateway {
    private static final String url = "http://localhost:9500/baseDstu3";
    private String prop = null;

    public MyController() throws FHIRException, NotImplementedException {
        super();
//        setFhirServer(url);
    }

    @Value("${fhirserver.url}")
    public void setUrl( String urlll ) throws FHIRException {
        System.out.println("\n==fhirserver.url=============== " + urlll + "++++++++++++++++++");
        setFhirServer(urlll);
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

