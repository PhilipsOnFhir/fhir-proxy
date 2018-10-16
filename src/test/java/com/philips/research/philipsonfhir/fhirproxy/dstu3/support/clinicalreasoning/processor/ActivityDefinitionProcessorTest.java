package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.clinicalreasoning.processor;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.philips.research.philipsonfhir.TestUtil;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.clinicalreasoning.cql.MyWorkerContext;
import org.hl7.fhir.dstu3.hapi.validation.DefaultProfileValidationSupport;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.dstu3.utils.StructureMapUtilities;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.junit.Test;

import java.io.*;
import java.util.Date;
import java.util.stream.Collectors;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ActivityDefinitionProcessorTest {
    private static final DefaultProfileValidationSupport defaultProfileValidationSupport = new DefaultProfileValidationSupport();

    @Test
    public void testProcedureRequest() throws FHIRException, IOException {
        FhirContext ourCtx = FhirContext.forDstu3();

        IGenericClient fhirClient = mock( IGenericClient.class );
        when( fhirClient.getFhirContext() ).thenReturn( ourCtx );

        fhirClient = ourCtx.newRestfulGenericClient( "http://localhost:9500/baseDstu3" );

        StructureMapTransformServer structureMapTransformServer = new StructureMapTransformServer( fhirClient );


        MyWorkerContext hapiWorkerContext = new MyWorkerContext( ourCtx, defaultProfileValidationSupport );

        StructureMapUtilities structureMapUtilities = new StructureMapUtilities(hapiWorkerContext );

        StructureMap structureMap;
        String mapStr = "";
        String mapJsonStr = "";
        {
            InputStream is = getClass().getClassLoader().getResourceAsStream( "dstu3/ProcedureRequest.fhirmap" );
            if ( is != null ) {
                BufferedReader reader = new BufferedReader( new InputStreamReader( is ) );
                mapStr = reader.lines().collect( Collectors.joining( System.lineSeparator() ) );
            }
            structureMap = (StructureMap) structureMapUtilities.parse( mapStr ).setId("Ad2ProcedureRequest");
        }

//        {
//            InputStream is = new FileInputStream( new File( "example/StructureMap-Ad2ProcedureRequest.json" ) );
//            if ( is != null ) {
//                BufferedReader reader = new BufferedReader( new InputStreamReader( is ) );
//                mapJsonStr = reader.lines().collect( Collectors.joining( System.lineSeparator() ) );
//            }
//            structureMap = (StructureMap) ourCtx.newJsonParser().parseResource( mapJsonStr ).setId("Ad2ProcedureRequest");
//        }

//        InputStream is = getClass().getClassLoader().getResourceAsStream( "dstu3/ProcedureRequest.fhirmap" );
//        if (is != null) {
//            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
//            mapStr = reader.lines().collect( Collectors.joining(System.lineSeparator()));
//        }


        ActivityDefinition activityDefinition = (ActivityDefinition) new ActivityDefinition(  )
            .setTiming(  new Timing().addEvent( new Date( ) ) )
            .setCode( new CodeableConcept().addCoding( new Coding( ).setCode( "2233" ).setSystem( "a" ) ) )
            .setId( "2123" );

        System.out.println(ourCtx.newJsonParser().setPrettyPrint( true ).encodeResourceToString( structureMap ));
        TestUtil.storeResource(  structureMap );

        File rscFile = new File("example/"+structureMap.getResourceType()+"-"+structureMap.getId()+".fhirmap");
        FileWriter writer = new FileWriter( rscFile );
        writer.write( StructureMapUtilities.render( structureMap ));
        writer.close();

        Parameters parameters = new Parameters()
            .addParameter( new Parameters.ParametersParameterComponent(  )
                .setName( "source" )
                .setResource( activityDefinition )
            )
            .addParameter( new Parameters.ParametersParameterComponent(  )
                .setName( "subject" )
                .setValue( new Reference( "Patient/patientid" ) )
            )
            .addParameter( new Parameters.ParametersParameterComponent(  )
                .setName( "encounter" )
                .setValue( new Reference( "Encounter/encounterid" ) )
            )
            .addParameter( new Parameters.ParametersParameterComponent(  )
                .setName( "practioner" )
                .setValue( new Reference( "Practioner/practionerId" ) )
            )
//            .addParameter( new Parameters.ParametersParameterComponent(  )
//                .setName( "organization" )
//                .setValue( new Reference( "Encounter/encounterid" ) )
//            )
        ;

        System.out.println(ourCtx.newJsonParser().setPrettyPrint( true ).encodeResourceToString( parameters ));

        IBaseResource result = structureMapTransformServer.doTransform( structureMap, parameters, null );
        assertNotNull( result );
        System.out.println(ourCtx.newJsonParser().setPrettyPrint( true ).encodeResourceToString( result ));
    }

}