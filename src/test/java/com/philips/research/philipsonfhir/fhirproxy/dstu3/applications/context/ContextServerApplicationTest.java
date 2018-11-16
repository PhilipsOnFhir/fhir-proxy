package com.philips.research.philipsonfhir.fhirproxy.dstu3.applications.context;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.applications.memoryserver.MemoryFhirServerApplicationTest;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.HumanName;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.dstu3.model.Resource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;

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

//    @Test
//    public void initializeServerTest(){
//        String sessionId = "";
//        String fhirUrl = "http://localhost:" + port + "/hapi";
//        sessionId = createSession( fhirUrl );
//        String contextUrl = "http://localhost:" + port + "/context/"+sessionId+"/fhir";
//
//        IGenericClient hapiClient = ourCtx.newRestfulGenericClient( fhirUrl );
//        {
//            Bundle hapiBundle = hapiClient.search().forResource(Patient.class).returnBundle(Bundle.class).execute();
//            assertNotNull(hapiBundle);
//            assertEquals(0, hapiBundle.getTotal());
//        }
//        IGenericClient contextClient = ourCtx.newRestfulGenericClient( contextUrl );
//        {
//            Bundle bundle = contextClient.search().forResource(Patient.class).returnBundle(Bundle.class).execute();
//            assertNotNull(bundle);
//            assertEquals(0, bundle.getTotal());
//        }
//
//    }

    @Test
    public void createResourceTest(){
        String sessionId = "";
        String fhirUrl = "http://localhost:" + port + "/hapi";
        sessionId = createSession( fhirUrl );
        String contextUrl = "http://localhost:" + port + "/context/"+sessionId+"/fhir";

        Patient patient1 = new Patient().addName(new HumanName().setFamily("SomethingGood1"));
        Patient patient2 = new Patient().addName(new HumanName().setFamily("SomethingGood2"));

        IGenericClient hapiClient = ourCtx.newRestfulGenericClient( fhirUrl );
        IGenericClient contextClient = ourCtx.newRestfulGenericClient( contextUrl );

        int initialSize;
        {
            Bundle bundle = contextClient.search().forResource(Patient.class).returnBundle(Bundle.class).execute();
            assertNotNull(bundle);
            initialSize =  bundle.getTotal();
        }
        hapiClient.create().resource(patient1).execute();
        {
            Bundle bundle = contextClient.search().forResource(Patient.class).returnBundle(Bundle.class).execute();
            assertNotNull(bundle);
            assertEquals(initialSize+1, bundle.getTotal());
        }
        contextClient.create().resource(patient2).execute();
//        contextClient.create().resource(patient).execute();
        {
            Bundle bundle = contextClient.search().forResource(Patient.class).returnBundle(Bundle.class).execute();
            assertNotNull(bundle);
            assertEquals(initialSize+2, bundle.getTotal());
        }

    }

    @Test
    public void updateContextResourceTest(){
        String sessionId = "";
        String fhirUrl = "http://localhost:" + port + "/hapi";
        sessionId = createSession( fhirUrl );
        String contextUrl = "http://localhost:" + port + "/context/"+sessionId+"/fhir";

        Patient patient3 = (Patient) new Patient().addName(new HumanName().setFamily("SomethingGood3")).setId("context1");

        IGenericClient hapiClient = ourCtx.newRestfulGenericClient( fhirUrl );
        IGenericClient contextClient = ourCtx.newRestfulGenericClient( contextUrl );

        contextClient.update().resource(patient3).execute();

        {
            Bundle bundle = contextClient.search().forResource(Patient.class).returnBundle(Bundle.class).execute();
            assertNotNull(bundle);
            Optional<Resource> p = bundle.getEntry().stream()
                    .map(bundleEntryComponent -> bundleEntryComponent.getResource())
                    .filter(resource -> resource.getIdElement().getIdPart().equals(patient3.getIdElement().getIdPart()))
                    .findFirst();
            assertTrue( p.isPresent() );
        }

        Patient patient4 = (Patient) new Patient().addName(new HumanName().setFamily("SomethingGood4")).setId("context1");
        contextClient.update().resource(patient4).execute();

        {
            Bundle bundle = contextClient.search().forResource(Patient.class).returnBundle(Bundle.class).execute();
            assertNotNull(bundle);
            Optional<Resource> p = bundle.getEntry().stream()
                    .map(bundleEntryComponent -> bundleEntryComponent.getResource())
                    .filter(resource -> resource.getIdElement().getIdPart().equals(patient4.getIdElement().getIdPart()))
                    .findFirst();
            assertTrue( p.isPresent() );

            assertEquals( patient4.getName().get(0).getFamily(), ((Patient)p.get()).getName().get(0).getFamily() );
        }
    }
    @Test
    public void updateResourceTest(){
        String sessionId = "";
        String fhirUrl = "http://localhost:" + port + "/hapi";
        sessionId = createSession( fhirUrl );
        String contextUrl = "http://localhost:" + port + "/context/"+sessionId+"/fhir";

        Patient patient3 = (Patient) new Patient().addName(new HumanName().setFamily("SomethingGood3")).setId("context3");

        IGenericClient hapiClient = ourCtx.newRestfulGenericClient( fhirUrl );
        IGenericClient contextClient = ourCtx.newRestfulGenericClient( contextUrl );

        hapiClient.update().resource(patient3).execute();

        Patient patient4 = (Patient) new Patient().addName(new HumanName().setFamily("SomethingGood4")).setId("context3");
        contextClient.update().resource(patient4).execute();
        {
            Bundle bundle = contextClient.search().forResource(Patient.class).returnBundle(Bundle.class).execute();
            assertNotNull(bundle);
            Optional<Resource> p = bundle.getEntry().stream()
                    .map(bundleEntryComponent -> bundleEntryComponent.getResource())
                    .filter(resource -> resource.getIdElement().getIdPart().equals(patient4.getIdElement().getIdPart()))
                    .findFirst();
            assertTrue( p.isPresent() );

            assertEquals( patient4.getName().get(0).getFamily(), ((Patient)p.get()).getName().get(0).getFamily() );
        }
    }
}