package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.clinicalreasoning.processor;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.NotImplementedException;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.clinicalreasoning.cql.MyWorkerContext;
import org.hl7.fhir.dstu3.hapi.validation.DefaultProfileValidationSupport;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.dstu3.utils.StructureMapUtilities;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opencds.cqf.cql.data.fhir.BaseFhirDataProvider;
import org.opencds.cqf.cql.data.fhir.FhirDataProviderStu3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
    public void testProcedureRequest() throws FHIRException, IOException {
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
        System.out.println( ourCtx.newJsonParser().setPrettyPrint( true ).encodeResourceToString( parameters ) );

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
    public void testCommunication() throws FHIRException, IOException, NotImplementedException {
        ActivityDefinition activityDefinition = (ActivityDefinition) new ActivityDefinition(  )
            .setKind( ActivityDefinition.ActivityDefinitionKind.COMMUNICATION )
//            .setTiming(  new Timing().addEvent( new Date( ) ) )
            .setCode( new CodeableConcept().addCoding( new Coding( ).setCode( "2233" ).setSystem( "a" ) ) )
//            .addBodySite( new CodeableConcept().addCoding( new Coding( ).setSystem( "http://example.com").setCode( "someCode" ) ) )
            .setId( "Ad2Communication" );

        BaseFhirDataProvider baseFhirDataProvider = new FhirDataProviderStu3().setEndpoint( "http://somesever" );
        ActivityDefinitionProcessor activityDefinitionProcessor;

        activityDefinitionProcessor = new ActivityDefinitionProcessor( baseFhirDataProvider, activityDefinition, "Patient/patientId" );
        IBaseResource resource = activityDefinitionProcessor.getResult();
        assertNotNull( resource );
        assertTrue( resource instanceof Communication );
        Communication communication = (Communication) resource;

        assertEquals( "Patient/patientId", communication.getSubject().getReference() );
        assertEquals("ActivityDefinition/"+activityDefinition.getId(),communication.getBasedOnFirstRep().getReference()  );
        assertEquals( Communication.CommunicationStatus.PREPARATION, communication.getStatus() );
        assertEquals( activityDefinition.getCode(), communication.getReasonCodeFirstRep() );

        activityDefinitionProcessor = new ActivityDefinitionProcessor( baseFhirDataProvider, activityDefinition, "Patient/patientId","Encounter/encounterId", "Practitioner/pracId", "Organization/orgId", null, null, null, null, null );
        communication = (Communication) activityDefinitionProcessor.getResult();
        assertEquals( "Encounter/encounterId", communication.getContext().getReference() );
        assertEquals( "Practitioner/pracId", communication.getSender().getReference() );

        activityDefinitionProcessor = new ActivityDefinitionProcessor( baseFhirDataProvider, activityDefinition, "Patient/patientId","Encounter/encounterId", null, "Organization/orgId", null, null, null, null, null );
        communication = (Communication) activityDefinitionProcessor.getResult();
        assertEquals( "Organization/orgId", communication.getSender().getReference() );
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