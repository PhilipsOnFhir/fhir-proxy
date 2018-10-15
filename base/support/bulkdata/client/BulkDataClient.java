package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.bulkdata.client;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.json.simple.JSONArray;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BulkDataClient {
    private final FhirContext fhirContext;
    private final IGenericClient client;

    public BulkDataClient(FhirContext fhirContext, IGenericClient iGenericClient ) {
        this.fhirContext = fhirContext;
        this.client  = iGenericClient;
    }

    public BulkDataSession callPatientExport() throws Exception {
        return callPatientExport(null, null);
    }
    public BulkDataSession callPatientExport(String types) throws Exception {
        return callPatientExport(types, null);
    }

    public BulkDataSession callPatientExport(String types, String patientId) throws Exception {


        JSONArray context = new JSONArray();


        String url = client.getServerBase()+"/Patient/$export";
        if ( patientId!=null ){
            url = client.getServerBase()+"/Patient/"+patientId+"/$export";
        }

        URIBuilder uriBuilder = new URIBuilder(url);
        if ( types!=null ){
            uriBuilder.addParameter("type", types);
        }

        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet get = new HttpGet( uriBuilder.build() );
        get.setHeader("Content-type", "application/fhir+ndjson");
        get.setHeader("Prefer", "respond-async");
        get.setHeader("Accept", "application/fhir+json");


        HttpResponse response = httpClient.execute(get);

        BulkDataSession bulkDataSession = new BulkDataSession( response );


        return bulkDataSession;
    }

    public BulkDataSession callGroupExport(String types, String groupId) throws Exception {
        String url = client.getServerBase()+"/Group/"+groupId+"/$export";

        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet get = new HttpGet( url );
        get.setHeader("Content-type", "application/fhir+ndjson");
        get.setHeader("Prefer", "respond-async");
        get.setHeader("Accept", "application/fhir+json");

        HttpResponse response = httpClient.execute(get);

        BulkDataSession bulkDataSession = new BulkDataSession( response );

        return bulkDataSession;
    }

    public  class BulkDataSession {
        private final String sessionUrl;
        private HttpResponse response;

        BulkDataSession(HttpResponse response ) throws IOException, FHIRException {
            this.response = response;
            sessionUrl = getSessionUrl(response);

        }

        public void waitForCompleteness(int timeOutMSec) throws InterruptedException, FHIRException, IOException {
            int i=0;
            int waitTime = 100;
            int timeOutIterations = Math.floorDiv( timeOutMSec, waitTime );
            while( response.getStatusLine().getStatusCode()==202 && i<timeOutIterations) {
                Thread.sleep(waitTime);
                i++;
                response = getSessionStatus(sessionUrl);
            }
            if ( i>=timeOutIterations  ){
                throw new FHIRException("Response not received in time.");
            }
            if ( response.getStatusLine().getStatusCode()!=200 ){
                throw new FHIRException("Status code should be 200");
            }

        }

        private HttpResponse getSessionStatus(String sessionUrl) throws IOException {
            HttpClient httpClient = HttpClientBuilder.create().build();
            Gson gson             = new Gson();
//        HttpGet get         = new HttpGet( TestUtil.getOurClient().getServerBase()+"/Patient/"+ patientId +"" );
            HttpGet get         = new HttpGet( sessionUrl );

            HttpResponse response = httpClient.execute(get);
            return response;
        }

        private String getSessionUrl(HttpResponse response) throws FHIRException, IOException {
            InputStream inputStream = response.getEntity().getContent();

            Scanner s = new Scanner(inputStream).useDelimiter("\\A");
            String result = s.hasNext() ? s.next() : "";

            IBaseResource iBaseResource = fhirContext.newJsonParser().parseResource(result);
            if( !(iBaseResource instanceof OperationOutcome)){
                throw new FHIRException("Expected OperationOutcome but got "+iBaseResource.getClass());
            }
            OperationOutcome operationOutcome = (OperationOutcome)iBaseResource;
            if( response.getFirstHeader("Content-Location")==null){
                throw new FHIRException("Header value Content-Location note present");
            }
            String contentLocation = response.getFirstHeader("Content-Location").getValue();

            return  contentLocation;
        }


        public List<String> getLinks() throws InterruptedException, FHIRException, IOException {
            waitForCompleteness(10000);
            InputStream inputStream = response.getEntity().getContent();
            Scanner s = new Scanner(inputStream).useDelimiter("\\A");
            String result = s.hasNext() ? s.next() : "";

            JsonObject jobject = (JsonObject) new JsonParser().parse(result);
            JsonArray jsonArray = (JsonArray) jobject.get("output");
            ArrayList<String> links = new ArrayList<>();
            for ( int i=0; i<jsonArray.size();i++){
                String linkProto =  ((JsonObject) jsonArray.get(i)).get("url").toString();
                links.add(linkProto.substring(1, linkProto.length()-1)); // remove quotes
            }

            return links;
        }

        public void delete() throws IOException {

            HttpClient httpClient = HttpClientBuilder.create().build();
            Gson gson = new Gson();
            HttpDelete delete = new HttpDelete( sessionUrl );

            HttpResponse response = httpClient.execute( delete );
        }


    }


}
