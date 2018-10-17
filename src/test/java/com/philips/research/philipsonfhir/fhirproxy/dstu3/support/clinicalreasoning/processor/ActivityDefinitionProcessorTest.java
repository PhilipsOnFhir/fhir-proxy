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
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.*;
import java.util.Date;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ActivityDefinitionProcessorTest {
    private static final DefaultProfileValidationSupport defaultProfileValidationSupport = new DefaultProfileValidationSupport();
    private static FhirContext ourCtx;
    private static StructureMapTransformServer structureMapTransformServer;
    private static StructureMapUtilities structureMapUtilities;

    @BeforeClass
    public static void init(){
        ourCtx = FhirContext.forDstu3();

        IGenericClient fhirClient = mock( IGenericClient.class );
        when( fhirClient.getFhirContext() ).thenReturn( ourCtx );
        structureMapTransformServer = new StructureMapTransformServer( fhirClient );

        structureMapUtilities = new StructureMapUtilities( new MyWorkerContext( ourCtx, defaultProfileValidationSupport ));
    }

    @Test
    public void testProcedureRequestFull() throws FHIRException, IOException {
        String mapStr = "";
        String mapJsonStr = "";
        StructureMap structureMap = getStructureMap( mapStr );

        // test 1 - apply basic parameters
        ActivityDefinition activityDefinition = (ActivityDefinition) new ActivityDefinition(  )
            .setTiming(  new Timing().addEvent( new Date( ) ) )
            .setCode( new CodeableConcept().addCoding( new Coding( ).setCode( "2233" ).setSystem( "a" ) ) )
            .addBodySite( new CodeableConcept().addCoding( new Coding( ).setSystem( "http://example.com").setCode( "someCode" ) ) )
            .setId( "AdGenProcedureRequest" );

        Parameters parameters = new Parameters()
            .addParameter( new Parameters.ParametersParameterComponent(  )
                .setName( "source" )
                .setResource( activityDefinition )
            )
            .addParameter( new Parameters.ParametersParameterComponent(  )
                .setName( "subject" )
                .setValue( new Reference( "Patient/patientid" ) )
            )
            ;

        IBaseResource result = structureMapTransformServer.doTransform( structureMap, parameters, null );
        assertNotNull( result );
        assertTrue( result instanceof ProcedureRequest );
        ProcedureRequest procedureRequest = (ProcedureRequest)result;
        assertEquals( "ActivityDefinition/"+activityDefinition.getId(), procedureRequest.getBasedOnFirstRep().getReference() );
        assertEquals( "Patient/patientid", procedureRequest.getSubject().getReference() );
        assertTrue( !procedureRequest.hasContext() );
        assertTrue( !procedureRequest.hasRequester() );
        // test PR contents
        assertEquals( activityDefinition.getTiming(), procedureRequest.getOccurrence() );
        assertEquals( ProcedureRequest.ProcedureRequestStatus.DRAFT, procedureRequest.getStatus() );
        assertEquals( ProcedureRequest.ProcedureRequestIntent.ORDER, procedureRequest.getIntent() );
        assertEquals( activityDefinition.getCode(), procedureRequest.getCode() );
        assertEquals( activityDefinition.getBodySite(), procedureRequest.getBodySite() );

        // add advanced parameters
        parameters.addParameter( new Parameters.ParametersParameterComponent(  )
            .setName( "encounter" )
            .setValue( new Reference( "Encounter/encounterid" ) )
        )
            .addParameter( new Parameters.ParametersParameterComponent(  )
                .setName( "practitioner" )
                .setValue( new Reference( "Practioner/practionerId" ) )
            )
        ;

        result = structureMapTransformServer.doTransform( structureMap, parameters, null );
        assertNotNull( result );
        assertTrue( result instanceof ProcedureRequest );
        procedureRequest = (ProcedureRequest)result;
        assertEquals( "ActivityDefinition/"+activityDefinition.getId(), procedureRequest.getBasedOnFirstRep().getReference() );
        assertEquals( "Encounter/encounterid", procedureRequest.getContext().getReference() );
        assertEquals( "Practioner/practionerId", procedureRequest.getRequester().getAgent().getReference() );
        assertTrue( !procedureRequest.getRequester().hasOnBehalfOf() );

        parameters.addParameter( new Parameters.ParametersParameterComponent(  )
            .setName( "organization" )
            .setValue( new Reference( "Organization/organizationId" ) )
        );

        result = structureMapTransformServer.doTransform( structureMap, parameters, null );
        assertNotNull( result );
        assertTrue( result instanceof ProcedureRequest );
        procedureRequest = (ProcedureRequest)result;
        assertEquals( "ActivityDefinition/"+activityDefinition.getId(), procedureRequest.getBasedOnFirstRep().getReference() );
        assertEquals( "Encounter/encounterid", procedureRequest.getContext().getReference() );
        assertEquals( "Practioner/practionerId", procedureRequest.getRequester().getAgent().getReference() );
        assertEquals( "Organization/organizationId", procedureRequest.getRequester().getOnBehalfOf().getReference() );

        // check errors
        try {
            parameters = new Parameters()
                .addParameter( new Parameters.ParametersParameterComponent()
                    .setName( "source" )
                    .setResource( new ActivityDefinition().addDosage( new Dosage().setText( "aaa" ) ).setId( "AdGenProcedureRequest" ) )
                )
            ;

            structureMapTransformServer.doTransform( structureMap, parameters, null );
            fail("should have thrown exception");
        }
        catch ( FHIRException e ){}

        try {
            parameters = new Parameters()
                .addParameter( new Parameters.ParametersParameterComponent()
                    .setName( "source" )
                    .setResource( new ActivityDefinition().setProduct( new Reference(  ).setReference( "Substance/sdakdjasl" ) ).setId( "AdGenProcedureRequest" ) )
                )
            ;

            structureMapTransformServer.doTransform( structureMap, parameters, null );
            fail("should have thrown exception");
        }
        catch ( FHIRException e ){}
    }

    @Test
    public void testProcedureRequest() throws FHIRException, IOException {


        StructureMap structureMap;
        String mapStr = "";
        String mapJsonStr = "";
        structureMap = getStructureMap( mapStr );

        ActivityDefinition activityDefinition = (ActivityDefinition) new ActivityDefinition(  )
            .setTiming(  new Timing().addEvent( new Date( ) ) )
            .setCode( new CodeableConcept().addCoding( new Coding( ).setCode( "2233" ).setSystem( "a" ) ) )
            .addDosage( new Dosage().setText( "aaa" ) )
            .setProduct( new Reference(  ).setReference( "Substance/sdakdjasl" ) )
            .setId( "AdGenProcedureRequest" );

        System.out.println(ourCtx.newJsonParser().setPrettyPrint( true ).encodeResourceToString( structureMap ));
        TestUtil.storeResource(  structureMap );
        TestUtil.storeResource(  activityDefinition );

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
                .setName( "practitioner" )
                .setValue( new Reference( "Practioner/practionerId" ) )
            )
            .addParameter( new Parameters.ParametersParameterComponent(  )
                .setName( "organization" )
                .setValue( new Reference( "Organization/organizationId" ) )
            )
        ;

        System.out.println(ourCtx.newJsonParser().setPrettyPrint( true ).encodeResourceToString( parameters ));

        IBaseResource result = structureMapTransformServer.doTransform( structureMap, parameters, null );
        assertNotNull( result );
        System.out.println(ourCtx.newJsonParser().setPrettyPrint( true ).encodeResourceToString( result ));
    }

    private StructureMap getStructureMap(String mapStr) throws FHIRException {
        StructureMap structureMap;
        InputStream is = getClass().getClassLoader().getResourceAsStream( "dstu3/ProcedureRequest.fhirmap" );
        if ( is != null ) {
            BufferedReader reader = new BufferedReader( new InputStreamReader( is ) );
            mapStr = reader.lines().collect( Collectors.joining( System.lineSeparator() ) );
        }
        structureMap = (StructureMap) structureMapUtilities.parse( mapStr ).setId( "Ad2ProcedureRequest" );
        return structureMap;
    }

}