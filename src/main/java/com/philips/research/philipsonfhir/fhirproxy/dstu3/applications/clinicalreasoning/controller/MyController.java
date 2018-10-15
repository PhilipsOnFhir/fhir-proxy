package com.philips.research.philipsonfhir.fhirproxy.dstu3.applications.clinicalreasoning.controller;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.NotImplementedException;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.clinicalreasoning.ActivityDefinitionApplyOperation;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.clinicalreasoning.MeasureEvaluationOperation;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.clinicalreasoning.PlanDefinitionApplyOperation;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.clinicalreasoning.StructureMapTransformOperation;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.controller.SampleFhirGateway;
import org.hl7.fhir.exceptions.FHIRException;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MyController extends SampleFhirGateway {
    private static final String url = "http://localhost:9500/baseDstu3";

    public MyController() throws FHIRException, NotImplementedException {
        super( url );
        FhirContext ourCtx = this.fhirServer.getCtx();
        IGenericClient client = this.fhirServer.getCtx().newRestfulGenericClient( url );

        this.fhirServer.getFhirOperationRepository().registerOperation(  new MeasureEvaluationOperation( client ) );
        this.fhirServer.getFhirOperationRepository().registerOperation(  new StructureMapTransformOperation( url, client )  );
        this.fhirServer.getFhirOperationRepository().registerOperation(  new PlanDefinitionApplyOperation( url )  );
        this.fhirServer.getFhirOperationRepository().registerOperation(  new ActivityDefinitionApplyOperation( url )  );
    }
}

