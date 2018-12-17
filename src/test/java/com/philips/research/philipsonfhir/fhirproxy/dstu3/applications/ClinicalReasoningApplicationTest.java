package com.philips.research.philipsonfhir.fhirproxy.dstu3.applications;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.philips.research.philipsonfhir.builders.EncounterBuilder;
import com.philips.research.philipsonfhir.builders.PatientBuilder;
import com.philips.research.philipsonfhir.builders.PractitionerBuilder;
import com.philips.research.philipsonfhir.builders.ProcedureBuilder;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.applications.bulkdata.BulkDataTestApplication;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.applications.clinicalreasoning.ClinicalReasoningTestApplication;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.NotImplementedException;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.bulkdata.client.BulkDataClient;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.bulkdata.fhir.BundleRetriever;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.bulkdata.fhir.FhirServerBulkdata;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.service.FhirServer;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.service.IFhirServer;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.json.simple.JSONArray;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ClinicalReasoningTestApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@WebAppConfiguration
public class ClinicalReasoningApplicationTest {

    TestRestTemplate restTemplate = new TestRestTemplate();
    @LocalServerPort private long port;

    @Test
    public void testFhirServerExistence(){
        FhirContext ourCtx = FhirContext.forDstu3();
        IGenericClient ourClient = ourCtx.newRestfulGenericClient( createURLWithPort( "/fhir" ) );
        assertNotNull( ourClient.capabilities() );
    }

    @Test
    public void testPlandefinitionApplyCapability(){
        FhirContext ourCtx = FhirContext.forDstu3();
        IGenericClient ourClient = ourCtx.newRestfulGenericClient( createURLWithPort( "/fhir" ) );
        CapabilityStatement capabilityStatement =
                ourClient.capabilities().ofType(CapabilityStatement.class).execute();
        assertNotNull( capabilityStatement );
        assertTrue( capabilityStatement.hasRest() );
        assertTrue( capabilityStatement.getRest().size()>0  );
        assertTrue( capabilityStatement.getRest().get(0).hasOperation());

        Optional<CapabilityStatement.CapabilityStatementRestOperationComponent> optOperation = capabilityStatement.getRest().get(0).getOperation().stream()
                .filter(capabilityStatementRestOperationComponent ->
                        ( capabilityStatementRestOperationComponent.getName().equals("apply") &&
                                capabilityStatementRestOperationComponent.getDefinition().getReference().contains("PlanDefinition")
                        )
                )
                .findFirst();
        assertTrue( optOperation.isPresent() );
        CapabilityStatement.CapabilityStatementRestOperationComponent operation = optOperation.get();
        assertTrue( operation.hasDefinition() );
        assertEquals( operation.getDefinition().getReference(), "OperationDefinition/PlanDefinition-apply");
    }

    private String createURLWithPort(String uri) {
        return "http://localhost:" + port + uri;
    }
}