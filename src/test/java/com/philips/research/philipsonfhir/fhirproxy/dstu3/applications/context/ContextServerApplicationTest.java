package com.philips.research.philipsonfhir.fhirproxy.dstu3.applications.context;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.applications.memoryserver.MemoryFhirServerApplicationTest;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.Patient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = ContextServerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration
public class ContextServerApplicationTest {
    TestRestTemplate restTemplate = new TestRestTemplate();
    @LocalServerPort
    private long port;
    FhirContext ourCtx = FhirContext.forDstu3();

    private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(MemoryFhirServerApplicationTest.class);

    @Test
    public void createSessionTest(){
        String sessionId = "";
        String fhirUrl = "http://localhost:" + port + "/hapi";
        sessionId = createSession(fhirUrl);
        {   // check creation
            HttpHeaders httpHeaders = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(null, httpHeaders);
            ResponseEntity<String> response = restTemplate.exchange(
                    "http://localhost:" + port + "/context",
                    HttpMethod.GET, entity, String.class
            );
            assertEquals(HttpStatus.OK, response.getStatusCode());
            System.out.println( response.getBody() );
            String body = response.getBody();
            assertTrue( body.contains(sessionId));
        }
        {   // remove session
            HttpHeaders httpHeaders = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(null, httpHeaders);
            ResponseEntity<String> response = restTemplate.exchange(
                    "http://localhost:" + port + "/context/"+sessionId,
                    HttpMethod.DELETE, entity, String.class
            );
            System.out.println( response.getBody() );
            String body = response.getBody();
            assertEquals(HttpStatus.OK, response.getStatusCode());
        }
        {   // check deletion
            HttpHeaders httpHeaders = new HttpHeaders();
            HttpEntity<String> entity = new HttpEntity<>(null, httpHeaders);
            ResponseEntity<String> response = restTemplate.exchange(
                    "http://localhost:" + port + "/context",
                    HttpMethod.GET, entity, String.class
            );
            assertEquals(HttpStatus.OK, response.getStatusCode());
            System.out.println( response.getBody() );
            String body = response.getBody();
            assertTrue( !body.contains(sessionId));
        }
    }

    private String createSession(String fhirUrl) {
        String sessionId;// create session
        HttpHeaders httpHeaders = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(fhirUrl, httpHeaders);
        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:" + port + "/context",
                HttpMethod.POST, entity, String.class
        );
        System.out.println( response.getBody() );
        assertEquals(HttpStatus.OK, response.getStatusCode());
        sessionId = response.getBody();
        assertNotNull( response );
        return sessionId;
    }

    @Test
    public void initializeServerTest(){
        String sessionId = "";
        String fhirUrl = "http://localhost:" + port + "/hapi";
        sessionId = createSession( fhirUrl );
        String contextUrl = "http://localhost:" + port + "/context/"+sessionId;

        IGenericClient hapiClient = ourCtx.newRestfulGenericClient( fhirUrl );
        {
            Bundle hapiBundle = hapiClient.search().forResource(Patient.class).returnBundle(Bundle.class).execute();
            assertNotNull(hapiBundle);
            assertEquals(0, hapiBundle.getTotal());
        }
        IGenericClient contextClient = ourCtx.newRestfulGenericClient( contextUrl );
        {
            Bundle bundle = contextClient.search().forResource(Patient.class).returnBundle(Bundle.class).execute();
            assertNotNull(bundle);
            assertEquals(0, bundle.getTotal());
        }

    }

    @Test
    public void createResourceTest(){
        String sessionId = "";
        String fhirUrl = "http://localhost:" + port + "/hapi";
        sessionId = createSession( fhirUrl );
        String contextUrl = "http://localhost:" + port + "/context/"+sessionId;

        Patient patient = new Patient();
        patient.addName(new HumanName().setFamily("SomethingGood"));

        IGenericClient hapiClient = ourCtx.newRestfulGenericClient( fhirUrl );
        IGenericClient contextClient = ourCtx.newRestfulGenericClient( contextUrl );
        {
            Bundle bundle = contextClient.search().forResource(Patient.class).returnBundle(Bundle.class).execute();
            assertNotNull(bundle);
            assertEquals(0, bundle.getTotal());
        }
        contextClient.create().resource(patient).execute();
//        contextClient.create().resource(patient).execute();
        {
            Bundle bundle = contextClient.search().forResource(Patient.class).returnBundle(Bundle.class).execute();
            assertNotNull(bundle);
            assertEquals(1, bundle.getTotal());
        }

    }
}