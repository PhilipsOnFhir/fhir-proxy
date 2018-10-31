package com.philips.research.philipsonfhir.fhirproxy.dstu3.applications.memoryserver;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import org.eclipse.jetty.server.Server;
import org.hl7.fhir.dstu3.model.Patient;
import org.hl7.fhir.instance.model.api.IIdType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = MemoryFhirServerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration
public class MemoryFhirServerApplicationTest {
    TestRestTemplate restTemplate = new TestRestTemplate();
    @LocalServerPort private long port;

    private static final org.slf4j.Logger ourLog = org.slf4j.LoggerFactory.getLogger(MemoryFhirServerApplicationTest.class);
    private static IGenericClient ourClient;
    private static FhirContext ourCtx = FhirContext.forDstu3();
    @LocalServerPort private int ourPort;

    private static Server ourServer;
    private static String ourServerBase;
    private static String path;

    @Test
    public void testAccessibility(){
        System.out.println( port );

        javax.xml.bind.JAXBException a;

        HttpHeaders httpHeaders = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>( null, httpHeaders );
        ResponseEntity<String> response = restTemplate.exchange(
                "http://localhost:"+port+"/hapi/Patient" ,
                HttpMethod.GET, entity, String.class
        );
        System.out.println( response.getBody() );
        assertEquals( HttpStatus.OK, response.getStatusCode() );
    }

    @Test
    public void testCreateAndRead() throws IOException {
        ourServerBase = "http://localhost:" + ourPort + "/hapi";
        ourClient = ourCtx.newRestfulGenericClient(ourServerBase);
        ourClient.registerInterceptor(new LoggingInterceptor(true));

//        ourLog.info("Base URL is: http://localhost:" + ourPort + "/baseDstu3");
        String methodName = "testCreateResourceConditional";

        Patient pt = new Patient();
        pt.addName().setFamily(methodName);
        IIdType id = ourClient.create().resource(pt).execute().getId();

        Patient pt2 = ourClient.read().resource(Patient.class).withId(id).execute();
        assertEquals(methodName, pt2.getName().get(0).getFamily());
    }

//    @AfterClass
//    public static void afterClass() throws Exception {
//        ourServer.stop();
//    }
//
//    @BeforeClass
//    public static void beforeClass() throws Exception {
//        /*
//         * This runs under maven, and I'm not sure how else to figure out the target directory from code..
//         */
//        path = ExampleServerIT.class.getClassLoader().getResource(".keep_hapi-fhir-jpaserver-example").getPath();
//        path = new File(path).getParent();
//        path = new File(path).getParent();
//        path = new File(path).getParent();
//
//        ourLog.info("Project base path is: {}", path);
//
//        if (ourPort == 0) {
//            ourPort = RandomServerPortProvider.findFreePort();
//        }
//        ourServer = new Server(ourPort);
//
//        WebAppContext webAppContext = new WebAppContext();
//        webAppContext.setContextPath("/");
//        webAppContext.setDescriptor(path + "/src/main/webapp/WEB-INF/web.xml");
//        webAppContext.setResourceBase(path + "/target/hapi-fhir-jpaserver-example");
//        webAppContext.setParentLoaderPriority(true);
//
//        ourServer.setHandler(webAppContext);
//        ourServer.start();
//
//        ourCtx.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
//        ourCtx.getRestfulClientFactory().setSocketTimeout(1200 * 1000);
//        ourServerBase = "http://localhost:" + ourPort + "/baseDstu3";
//        ourClient = ourCtx.newRestfulGenericClient(ourServerBase);
//        ourClient.registerInterceptor(new LoggingInterceptor(true));
//
//    }
}