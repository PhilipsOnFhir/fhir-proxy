package org.github.philipsonfhir.fhirproxy.clinicalreasoning.examples.bmi;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.model.primitive.IdDt;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.dstu3.model.*;
import org.junit.Assert;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class TestServer {
    private static String defaultUrl = "http://localhost:9404/hapi-fhir-jpaserver/fhir/";
    private final IGenericClient ourClient;
    private FhirContext ourCtx = FhirContext.forDstu3();

    public TestServer(  ){
        this(defaultUrl);
    }

    public TestServer( String serverUrl ){
        // Set how long to try and establish the initial TCP connection (in ms)
        ourCtx.getRestfulClientFactory().setConnectTimeout(30 * 1000);

        // Set how long to block for individual read/write operations (in ms)
        ourCtx.getRestfulClientFactory().setSocketTimeout(30 * 1000);

        ourClient = ourCtx.newRestfulGenericClient(serverUrl);

        // required classes
        org.slf4j.impl.StaticLoggerBinder a;
    }

    public void storeResource(Resource resource) throws IOException {
        IParser jsonParser = ourCtx.newJsonParser();
        jsonParser.setPrettyPrint(true);
        File exampleDir = new File("example");
        if ( !exampleDir.exists() ){
            exampleDir.mkdir();
        }

        File rscFile = new File("example/"+resource.getIdElement().getIdPart()+".json");
        FileWriter writer = new FileWriter( rscFile );
        writer.write( jsonParser.encodeResourceToString(resource ));
        writer.close();
    }

    public void putResource(Resource resource) {
        putResource( resource, resource.getId() );
    }

    public void putResource(Resource resource, String id) {
        if (resource instanceof Bundle) {
            ourClient.transaction().withBundle((Bundle) resource).execute();
        }
        else {
            ourClient.update().resource(resource).withId(id).execute();
        }
    }

    private CarePlan applyPlanDefinition(String planDefinitionId, String subjectId) {
        Parameters outParams = ourClient
                .operation()
                .onInstance(new IdDt("PlanDefinition", planDefinitionId ))
                .named("$apply")
                .withParameters( new Parameters()
                        .addParameter( new Parameters.ParametersParameterComponent()
                                .setName("patient")
                                .setValue(new StringType("Patient/"+subjectId))
                        )
                )
                .useHttpGet()
                .execute();

        List<Parameters.ParametersParameterComponent> response = outParams.getParameter();

        Assert.assertTrue(!response.isEmpty());

        Resource resource = response.get(0).getResource();

        Assert.assertTrue(resource instanceof CarePlan);

        CarePlan carePlan = (CarePlan) resource;

        assertNotNull( carePlan );
        assertTrue( carePlan.hasActivity() );

        return carePlan;
    }



    public Resource transform(String structuredMapId, QuestionnaireResponse questionnaireResponse) {
        String xml = ourCtx.newXmlParser().encodeResourceToString(questionnaireResponse);
        Parameters outParams = ourClient
                .operation()
                .onInstance(new IdDt("StructureMap", structuredMapId ))
                .named("$transform")
//                .withParameters( new Parameters()
//                        .addParameter( new Parameters.ParametersParameterComponent()
//                                .setName("content")
//                                .setValue( new StringType("dummy"))
////                                .setValue(new StringType(xml) )
//                        )
//                )
                .withNoParameters(Parameters.class)
                .execute();

            List<Parameters.ParametersParameterComponent> response = outParams.getParameter();

            Assert.assertTrue(!response.isEmpty());

            Resource resource = response.get(0).getResource();


            return resource;
        }

    public void putStoreResource(Resource resource) throws IOException {
        storeResource(resource);
        putResource(resource);
    }
}
