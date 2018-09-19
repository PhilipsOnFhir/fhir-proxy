package com.philips.research.philipsonfhir.fhirproxy.applications.bulkdata;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.philips.research.philipsonfhir.builders.EncounterBuilder;
import com.philips.research.philipsonfhir.builders.PatientBuilder;
import com.philips.research.philipsonfhir.builders.PractitionerBuilder;
import com.philips.research.philipsonfhir.builders.ProcedureBuilder;
import com.philips.research.philipsonfhir.fhirproxy.support.bulkdata.client.BulkDataClient;
import com.philips.research.philipsonfhir.fhirproxy.support.bulkdata.fhir.BundleRetriever;
import com.philips.research.philipsonfhir.fhirproxy.support.bulkdata.fhir.FhirServerBulkdata;
import com.philips.research.philipsonfhir.fhirproxy.support.proxy.service.FhirServer;
import com.philips.research.philipsonfhir.fhirproxy.support.proxy.service.IFhirServer;
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
import java.util.Scanner;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = BulkDataApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@WebAppConfiguration
public class BulkDataApplicationTest {

    TestRestTemplate restTemplate = new TestRestTemplate();
    boolean resourcesCreated = false;
    @LocalServerPort
    private long port;
    private List<IBaseResource> resources = new ArrayList<>();
    private String patientId;
    private String groupId;
    private Bundle allPatientData;
    private FhirContext ourCtx;
    private Bundle allPatientDataPa2;
    private IFhirServer fhirServer;
    private IGenericClient ourClient;
    org.thymeleaf.spring5.ISpringTemplateEngine a;

    private void createResources() throws Exception {
        if ( !resourcesCreated ) {
            fhirServer = new FhirServerBulkdata( new FhirServer( createURLWithPort( "/fhir" ) ) );
            ourCtx = fhirServer.getCtx();
            ourClient = fhirServer.getCtx().newRestfulGenericClient( createURLWithPort( "/fhir" ) );

            {
                Practitioner practitioner = new PractitionerBuilder( "BD-Pr1", "1949-01-23" ).build();
                Patient patient = new PatientBuilder( "BD-Pa1", "1986-01-15", practitioner ).build();
                Encounter encounter = new EncounterBuilder( "BD-Enc-1", patient ).build();
                Procedure procedure = new ProcedureBuilder( "BD-Pr1", patient, practitioner, "2018-01-23" ).build();
                resources.add( practitioner );
                resources.add( patient );
                resources.add( encounter );
                resources.add( procedure );
            }
            {
                Practitioner practitioner = new PractitionerBuilder( "BD-Pr2", "1949-02-23" ).build();
                Patient patient = new PatientBuilder( "BD-Pa2", "1986-02-15", practitioner ).build();
                patientId = patient.getId();
                Encounter encounter = new EncounterBuilder( "BD-PA2", patient ).build();
                Procedure procedure = new ProcedureBuilder( "BD-Pr2", patient, practitioner, "2018-01-23" ).build();
                resources.add( practitioner );
                resources.add( patient );
                resources.add( encounter );
                resources.add( procedure );
            }
            {
                Practitioner practitioner = new PractitionerBuilder( "BD-Pr3", "1949-03-23" ).build();
                Patient patient1 = new PatientBuilder( "BD-Pa3", "1986-03-15", practitioner ).build();
                Encounter encounter = new EncounterBuilder( "BD-ENC3", patient1 ).build();
                Procedure procedure = new ProcedureBuilder( "BD-Pro3", patient1, practitioner, "2018-01-23" ).build();
                Patient patient2 = new PatientBuilder( "BD-Pa4", "1986-04-15", practitioner ).build();
                Patient patient3 = new PatientBuilder( "BD-Pa5", "1986-05-15", practitioner ).build();
                Group group = (Group) new Group()
                    .setActive( true )
                    .setType( Group.GroupType.PERSON )
                    .setName( "My bulk data test group" )
                    .addMember( new Group.GroupMemberComponent().setEntity( new Reference( patient1 ) ) )
                    .addMember( new Group.GroupMemberComponent().setEntity( new Reference( patient2 ) ) )
                    .addMember( new Group.GroupMemberComponent().setEntity( new Reference( patient3 ) ) )
                    .addMember( new Group.GroupMemberComponent().setEntity( new Reference( practitioner ) ) )
                    .setId( "Group-BD-Grp1" );
                resources.add( practitioner );
                resources.add( patient1 );
                resources.add( patient2 );
                resources.add( patient3 );
                resources.add( encounter );
                resources.add( procedure );
                resources.add( group );
                groupId = group.getId();
            }

            for ( IBaseResource baseResource : resources ) {
                putResource( baseResource );
            }

            resourcesCreated = true;
            allPatientData = retrieveAllPatientEverything( null );
            assertNotNull( allPatientData );
            assertTrue( allPatientData.getEntry().size() > 1 );

            allPatientDataPa2 = retrieveAllPatientEverything( patientId );
            assertNotNull( allPatientDataPa2 );
            assertTrue( allPatientDataPa2.getEntry().size() > 1 );
        }
    }

    private void putResource(IBaseResource baseResource) throws FHIRException {
//        fhourClient.update().resource(baseResource).execute();
        fhirServer.putResource( baseResource );
    }

    private String createURLWithPort(String uri) {
        return "http://localhost:" + port + uri;
    }

    @Test
    public void checkIfServerHasStarted() throws Exception {
        System.out.println( port );
        createResources();

        HttpHeaders httpHeaders = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>( null, httpHeaders );
        ResponseEntity<String> response = restTemplate.exchange(
            createURLWithPort( "/fhir/Patient" ),
            HttpMethod.GET, entity, String.class
        );
        System.out.println( response.getBody() );

    }

    @Test
    public void callPatientExport() throws Exception {
        createResources();

        BulkDataClient bulkDataClient = new BulkDataClient( fhirServer.getCtx(), ourClient );

        // Call patient/$export
        BulkDataClient.BulkDataSession bulkDataSession = bulkDataClient.callPatientExport();

        // wait for completeness
        bulkDataSession.waitForCompleteness( 30000 );

        // retrieve result linkes
        List<String> links = bulkDataSession.getLinks();

        for ( String link : links ) {
            checkLink( link, allPatientData );
        }
    }

    @Test
    public void callPatientExportWithOutcomeFilter() throws Exception {
        createResources();

        BulkDataClient bulkDataClient = new BulkDataClient( ourCtx, ourClient );

        String filterType = "Encounter";

        // Call patient/$export
        BulkDataClient.BulkDataSession bulkDataSession = bulkDataClient.callPatientExport( filterType );

        // wait for completeness
        bulkDataSession.waitForCompleteness( 30000 );

        // retrieve result linkes
        List<String> links = bulkDataSession.getLinks();

        // check absense of
        links.stream()
            .forEach( link ->
                assertTrue( filterType + " is the only one to be present: " + link, link.endsWith( filterType ) )
            );
        // check presence of Patient
        assertEquals( "Check count of " + filterType, 1, links.stream().filter( link -> link.endsWith( filterType ) ).count() );

        for ( String link : links ) {
            checkLink( link, allPatientData );
        }
    }

    @Test
    public void callBulkDataServerOutcomeFilter2() throws Exception {
        createResources();

        String filterType1 = "Procedure";
        String filterType2 = "Encounter";
        BulkDataClient bulkDataClient = new BulkDataClient( ourCtx, ourClient );

        // Call patient/$export
        BulkDataClient.BulkDataSession bulkDataSession = bulkDataClient.callPatientExport( filterType1 + "," + filterType2 );

        // wait for completeness
        bulkDataSession.waitForCompleteness( 30000 );

        // retrieve result links
        List<String> links = bulkDataSession.getLinks();

        // check absense of
        links.stream()
            .forEach( link ->
                assertTrue( filterType1 + ", " + filterType2 + " are the only one to be present: " + link, link.endsWith( filterType1 ) || link.endsWith( filterType2 ) )
            );
        // check presence only occurs once
        assertEquals( "Check presence of " + filterType1, 1, links.stream().filter( link -> link.endsWith( filterType1 ) ).count() );
        assertEquals( "Check presence of " + filterType2, 1, links.stream().filter( link -> link.endsWith( filterType2 ) ).count() );

        for ( String link : links ) {
            checkLink( link, allPatientData );
        }
    }

    @Test
    public void callGroupExport() throws Exception {
        createResources();

        BulkDataClient bulkDataClient = new BulkDataClient( ourCtx, ourClient );

        JSONArray context = new JSONArray();

        // Call patient/$export
        BulkDataClient.BulkDataSession bulkDataSession = bulkDataClient.callGroupExport( null, groupId );

        // wait for completeness
        bulkDataSession.waitForCompleteness( 30000 );

        // retrieve result links
        List<String> links = bulkDataSession.getLinks();


        assertTrue( "at least two links are expected", links.size() > 1 );
    }

    //    TODO trigger errors and check correct handling
    @Test
    public void callBulkDataServerPatientSpecific() throws Exception {
        createResources();
        BulkDataClient bulkDataClient = new BulkDataClient( ourCtx, ourClient );

        // Call patient/$export
        BulkDataClient.BulkDataSession bulkDataSession = bulkDataClient.callPatientExport( null, patientId );

        // wait for completeness
        bulkDataSession.waitForCompleteness( 30000 );

        // retrieve result links
        List<String> links = bulkDataSession.getLinks();


        assertTrue( "at least two links are expected", links.size() > 1 );

        for ( String link : links ) {
            checkLink( link, allPatientDataPa2 );
        }
    }

    @Test
    public void testBulkDataServerDelete() throws Exception {
        createResources();
        BulkDataClient bulkDataClient = new BulkDataClient( ourCtx, ourClient );
        BulkDataClient.BulkDataSession bulkDataSession = bulkDataClient.callPatientExport( "Procedure" );
        bulkDataSession.delete();

        try {
            bulkDataSession.getLinks();
            fail();
        } catch ( FHIRException fhirException ) {
        }
    }


    /**
     * Check link against bundle.
     */
    private void checkLink(String link, Bundle patientData) throws IOException {

        List<Resource> resourcesNdjson = new ArrayList<>();
        String resourceTypeNdjson = getResourcesFromLink( link, resourcesNdjson );

//        List<Resource> resourcesBundleList = new ArrayList<>();
//        getResourcesFromLink(link, resourcesBundleList, false);
//        assertEquals( 1, resourcesBundleList.size());
//        assertTrue( resourcesBundleList.get(0) instanceof  Bundle );
//        Bundle resourceBundle = (Bundle)resourcesBundleList.get(0);

        // resources contains ndjson resources.
        String resourceType = resourceTypeNdjson;
        List<Resource> resourceTypeInPatientData = patientData.getEntry().stream()
            .map( Bundle.BundleEntryComponent::getResource )
            .filter( bdleResource -> bdleResource.fhirType() == resourceType )
            .collect( Collectors.toList() );

        long numberOfResources = resourceTypeInPatientData.size();
        assertEquals( "number of " + resourceType + " resources must be equal.", resourcesNdjson.size(), numberOfResources );
//        assertEquals( "number of "+resourceType+" resources must be equal.", resourceBundle.getEntry().size(), numberOfResources );

        for ( Resource resource : resourcesNdjson ) {
            assertTrue( resource.fhirType() + " not present", patientData.getEntry().stream()
                .map( Bundle.BundleEntryComponent::getResource )
                .filter( res -> res.fhirType() == resourceType )
                .filter( res2 -> res2.getId().endsWith( resource.getId() ) )
                .findFirst().isPresent()
            );
        }

//        for ( Bundle.BundleEntryComponent bundleEntryComponent : resourceBundle.getEntry()) {
//            Resource resource = bundleEntryComponent.getResource();
//            assertTrue( patientData.getEntry().stream()
//                .map(allPatientBundleEntryComponent -> allPatientBundleEntryComponent.getResource())
//                .filter( res -> res.fhirType()==resourceType)
//                .filter( res2 -> res2.getId().endsWith(resource.getId()))
//                .findFirst().isPresent());
//            assertEquals( resourceType, resource.fhirType());
//        }
    }

    private String getResourcesFromLink(String link, List<Resource> resources) throws IOException {
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet get = new HttpGet( link );
//        if ( ndjson ) {
//            get.setHeader("Content-type", "application/fhir+ndjson");
//        }
//        else{
//            get.setHeader("Content-type", "application/json");
//        }
        HttpResponse response = httpClient.execute( get );
        assertNotNull( response );

        InputStream inputStream = response.getEntity().getContent();
        Scanner s = new Scanner( inputStream ).useDelimiter( "\\A" );
        String result = s.hasNext() ? s.next() : "";

        String resourceType = null;
        for ( String str : result.split( "\\n" ) ) {
            Resource resource = (Resource) ourCtx.newJsonParser().parseResource( str );
            if ( resourceType == null ) {
                resourceType = resource.fhirType();
            } else {
                assertEquals( resourceType, resource.fhirType() );
            }
            resources.add( resource );
        }
        return resourceType;
    }

//    public static HttpResponse callBulkDataService() throws Exception {
//        return callBulkDataService(null, null);
//    }
//    public static HttpResponse callBulkDataService(String outcome) throws Exception {
//        return callBulkDataService(outcome, null);
//    }
//    public static HttpResponse callBulkDataService(String outcome, String patientId) throws Exception {
//
//        TestUtil.startServer();
//
//        JSONArray context = new JSONArray();
//
//        String url = ourClient.getServerBase()+"/Patient/$export";
//        if ( patientId!=null ){
//            url = ourClient.getServerBase()+"/Patient/"+patientId+"/$export";
//        }
//
//        URIBuilder uriBuilder = new URIBuilder(url);
//        if ( outcome!=null ){
//            uriBuilder.addParameter("type", outcome);
//        }
//
//        HttpClient httpClient = HttpClientBuilder.create().build();
//        HttpGet get = new HttpGet( uriBuilder.build() );
//        get.setHeader("Content-type", "application/fhir+ndjson");
//        get.setHeader("Prefer", "respond-async");
//        get.setHeader("Accept", "application/fhir+json");
//
//
//        HttpResponse response = httpClient.execute(get);
//
//        return response;
//    }

//    private HttpResponse waitForCompleteness(HttpResponse response, String sessionUrl ) throws InterruptedException, IOException {
//        int i=0;
//        while( response.getStatusLine().getStatusCode()==202 && i<3000) {
//            Thread.sleep(100);
//            i++;
//            response = getSessionStatus(sessionUrl);
//        }
//        assertEquals(200, response.getStatusLine().getStatusCode());
//        assertNotEquals("Session not ready in time.", 3000, i);
//        return response;
//    }

//    private static HttpResponse getSessionStatus(String sessionUrl) throws IOException {
//        HttpClient httpClient = HttpClientBuilder.create().build();
//        Gson gson             = new Gson();
////        HttpGet get         = new HttpGet( ourClient.getServerBase()+"/Patient/"+ patientId +"" );
//        HttpGet get         = new HttpGet( sessionUrl );
//
//        HttpResponse response = httpClient.execute(get);
//        return response;
//    }

//    private static String getSessionUrl(HttpResponse response) throws Exception {
//        InputStream inputStream = response.getEntity().getContent();
//
//        Scanner s = new Scanner(inputStream).useDelimiter("\\A");
//        String result = s.hasNext() ? s.next() : "";
//
//        IBaseResource iBaseResource = ourCtx.newJsonParser().parseResource(result);
//        assertTrue( iBaseResource instanceof OperationOutcome);
//        OperationOutcome operationOutcome = (OperationOutcome)iBaseResource;
//        assertNotNull( response.getFirstHeader("Content-Location") );
//        String contentLocation = response.getFirstHeader("Content-Location").getValue();
//
//        return  contentLocation;
//    }


    private Bundle retrieveAllPatientEverything(String patientId) throws Exception {
        HttpClient httpClient = HttpClientBuilder.create().build();

        String url = ourClient.getServerBase() + (patientId == null ? "/Patient/$everything" : "/Patient/" + patientId + "/$everything");
        Parameters result = null;

        if ( patientId != null ) {
            result = ourClient.operation()
                .onInstance( new IdType( "Patient", patientId ) )
                .named( "$everything" )
                .withParameters( new Parameters() )
                .useHttpGet()
                .execute();
        } else {
            result = ourClient.operation()
                .onType( Patient.class )
                .named( "$everything" )
                .withParameters( new Parameters() )
                .useHttpGet()
                .execute();
        }

        IBaseResource iBaseResource = result.getParameterFirstRep().getResource();
//        HttpGet get        = new HttpGet( url );
//
//        HttpResponse response = httpClient.execute(get);
//        InputStream inputStream = response.getEntity().getContent();
//
//        Scanner s = new Scanner(inputStream).useDelimiter("\\A");
//        String result = s.hasNext() ? s.next() : "";
//
//        IBaseResource iBaseResource = ourCtx.newJsonParser().parseResource(result);
        assertTrue( iBaseResource instanceof Bundle );
        Bundle bundle = (Bundle) iBaseResource;

        BundleRetriever bundleRetriever = new BundleRetriever( fhirServer, bundle );
        Bundle newBundle = bundleRetriever.addAllResourcesToBundle();


        return newBundle;
    }
}