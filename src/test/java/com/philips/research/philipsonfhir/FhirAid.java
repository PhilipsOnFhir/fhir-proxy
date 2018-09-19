package com.philips.research.philipsonfhir;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.parser.StrictErrorHandler;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.dstu3.model.Parameters;
import org.hl7.fhir.dstu3.model.ReferralRequest;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.dstu3.model.StringType;
import org.junit.Assert;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FhirAid {
    private final FhirContext ourCtx;
    private final IParser jsonParser;
    private List<Resource> resources = new ArrayList();
    IGenericClient ourClient;
//    private static String fhirUrl = "http://130.145.227.123:9002/cqf-ruler/baseDstu3";
//    private static String fhirUrl = "http://localhost:9500/baseDstu3";
//    private static String fhirUrl = "http://localhost:9002/baseDstu3";
    private static String fhirUrl = "http://localhost:8080/fhir";

    public FhirAid(){
        ourCtx = FhirContext.forDstu3();
        ourCtx.setParserErrorHandler(new StrictErrorHandler() );
//        ourClient = ourCtx.newRestfulGenericClient("http://130.145.227.123:9002/cqf-ruler/baseDstu3");
        ourClient = ourCtx.newRestfulGenericClient(fhirUrl);
        jsonParser = ourCtx.newJsonParser();
        jsonParser.setPrettyPrint(true);
        jsonParser.setParserErrorHandler(new StrictErrorHandler());
    }

    public void printResource(Resource resource) {
        System.out.println( jsonParser.encodeResourceToString(resource ));
        this.resources.add(resource);
    }


    public void storeAllResources() throws IOException {

        File exampleDir = new File("example");
        if ( !exampleDir.exists() ){
            exampleDir.mkdir();
        }

        for ( Resource resource: this.resources ){
            File rscFile = new File("example/"+resource.getId()+".json");
            FileWriter writer = new FileWriter( rscFile );
            writer.write( jsonParser.encodeResourceToString(resource ));
            writer.close();

        }
    }

    public void putResource( Resource resource ){
//        IGenericClient ourClient = ourCtx.newRestfulGenericClient("http://localhost:9001/cqf-ruler/baseDstu3");
//        IGenericClient ourClient = ourCtx.newRestfulGenericClient("http://130.145.227.123:9002/cqf-ruler/baseDstu3");
        ourClient.update().resource(resource).execute();
    }

    public void putAllResources(){
//        IGenericClient ourClient = ourCtx.newRestfulGenericClient("http://localhost:9001/cqf-ruler/baseDstu3");
        resources.stream()
                .forEach( resource -> {
                    ourClient.update().resource(resource).execute();
                });
    }

    public ReferralRequest applyActivitydefinition(String id, String patientId){
        Parameters parameters = new Parameters();
        parameters.addParameter().setName("patient").setValue(new StringType("Patient/"+patientId));


        Parameters outParams = ourClient
                .operation()
                .onInstance(new IdDt("ActivityDefinition", id ))
                .named("$apply")
                .withParameters( parameters )
                .useHttpGet()
                .execute();

        List<Parameters.ParametersParameterComponent> response = outParams.getParameter();

        Assert.assertTrue(!response.isEmpty());

        Resource resource = response.get(0).getResource();

        Assert.assertTrue(resource instanceof ReferralRequest);

        ReferralRequest referralRequest = (ReferralRequest) resource;

        return referralRequest;
    }

    public void deleteResource(String resourceType, String id) {
        ourClient.delete().resourceById(resourceType, id).execute();
    }

    public IGenericClient getClient() {
        return this.ourClient;
    }
}
