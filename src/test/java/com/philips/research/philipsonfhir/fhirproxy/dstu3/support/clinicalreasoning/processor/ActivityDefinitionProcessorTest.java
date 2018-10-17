package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.clinicalreasoning.processor;

import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.NotImplementedException;
import org.hl7.fhir.dstu3.hapi.validation.DefaultProfileValidationSupport;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opencds.cqf.cql.data.fhir.BaseFhirDataProvider;
import org.opencds.cqf.cql.data.fhir.FhirDataProviderStu3;

import java.io.IOException;
import java.util.Date;

import static org.junit.Assert.*;

public class ActivityDefinitionProcessorTest {
    private static final DefaultProfileValidationSupport defaultProfileValidationSupport = new DefaultProfileValidationSupport();
    private static BaseFhirDataProvider baseFhirDataProvider;

    @BeforeClass
    public static void setup() throws FHIRException, NotImplementedException {
        baseFhirDataProvider = new FhirDataProviderStu3().setEndpoint( "http://somesever" );
        ActivityDefinitionProcessor dummy =  new ActivityDefinitionProcessor( baseFhirDataProvider, new ActivityDefinition(  ).setKind( ActivityDefinition.ActivityDefinitionKind.PROCEDUREREQUEST ), "Patient/patientId" );

    }

    @Test
    public void testProcedureRequest() throws FHIRException, IOException, NotImplementedException {
        ActivityDefinitionProcessor activityDefinitionProcessor;

        ActivityDefinition activityDefinition = (ActivityDefinition) new ActivityDefinition(  )
            .setTiming(  new Timing().addEvent( new Date( ) ) )
            .setCode( new CodeableConcept().addCoding( new Coding( ).setCode( "2233" ).setSystem( "a" ) ) )
            .addBodySite( new CodeableConcept().addCoding( new Coding( ).setSystem( "http://example.com").setCode( "someCode" ) ) )
            .setKind( ActivityDefinition.ActivityDefinitionKind.PROCEDUREREQUEST )
            .setId( "AdGenProcedureRequest" );


//        System.out.println( ourCtx.newJsonParser().setPrettyPrint( true ).encodeResourceToString( parameters ) );

        activityDefinitionProcessor = new ActivityDefinitionProcessor( baseFhirDataProvider, activityDefinition, "Patient/patientId" );
        IBaseResource resource = activityDefinitionProcessor.getResult();
        assertNotNull( resource );
        assertTrue( resource instanceof ProcedureRequest );

        ProcedureRequest procedureRequest = (ProcedureRequest)resource;
        assertEquals( "ActivityDefinition/"+activityDefinition.getId(), procedureRequest.getBasedOnFirstRep().getReference() );
        assertEquals( "Patient/patientId", procedureRequest.getSubject().getReference() );
        assertTrue( !procedureRequest.hasContext() );
        assertTrue( !procedureRequest.hasRequester() );
        // test PR contents
        assertEquals( activityDefinition.getTiming(), procedureRequest.getOccurrence() );
        assertEquals( ProcedureRequest.ProcedureRequestStatus.DRAFT, procedureRequest.getStatus() );
        assertEquals( ProcedureRequest.ProcedureRequestIntent.ORDER, procedureRequest.getIntent() );
        assertEquals( activityDefinition.getCode(), procedureRequest.getCode() );
        assertEquals( activityDefinition.getBodySite(), procedureRequest.getBodySite() );

        ////////////////////////////////////////////////////
        activityDefinition = (ActivityDefinition) new ActivityDefinition(  )
            .setTiming(  new Timing().addEvent( new Date( ) ) )
            .setCode( new CodeableConcept().addCoding( new Coding( ).setCode( "2233" ).setSystem( "a" ) ) )
            .addBodySite( new CodeableConcept().addCoding( new Coding( ).setSystem( "http://example.com").setCode( "someCode" ) ) )
            .setKind( ActivityDefinition.ActivityDefinitionKind.PROCEDUREREQUEST )
            .setId( "AdGenProcedureRequest" );

        // add advanced parameters
        activityDefinitionProcessor = new ActivityDefinitionProcessor( baseFhirDataProvider, activityDefinition, "Patient/patientId","Encounter/encounterId", "Practitioner/practitionerId", null, null, null, null, null, null );
        resource = activityDefinitionProcessor.getResult();
        procedureRequest = (ProcedureRequest)resource;

        assertEquals( "ActivityDefinition/"+activityDefinition.getId(), procedureRequest.getBasedOnFirstRep().getReference() );
        assertEquals( "Encounter/encounterId", procedureRequest.getContext().getReference() );
        assertEquals( "Practitioner/practitionerId", procedureRequest.getRequester().getAgent().getReference() );
        assertTrue( !procedureRequest.getRequester().hasOnBehalfOf() );

        //////////////////////////
        activityDefinitionProcessor = new ActivityDefinitionProcessor( baseFhirDataProvider, activityDefinition, "Patient/patientId","Encounter/encounterId", "Practitioner/practitionerId", "Organization/organizationId", null, null, null, null, null );
        resource = activityDefinitionProcessor.getResult();
        procedureRequest = (ProcedureRequest)resource;
        assertEquals( "ActivityDefinition/"+activityDefinition.getId(), procedureRequest.getBasedOnFirstRep().getReference() );
        assertEquals( "Encounter/encounterId", procedureRequest.getContext().getReference() );
        assertEquals( "Practitioner/practitionerId", procedureRequest.getRequester().getAgent().getReference() );
        assertEquals( "Organization/organizationId", procedureRequest.getRequester().getOnBehalfOf().getReference() );

        // check errors
        try {
            activityDefinitionProcessor = new ActivityDefinitionProcessor( baseFhirDataProvider, (ActivityDefinition) new ActivityDefinition().addDosage( new Dosage().setText( "aaa" ) ).setId( "AdGenProcedureRequest" ), "Patient/patientId" );
            resource = activityDefinitionProcessor.getResult();
            fail("should have thrown exception");
        }
        catch ( FHIRException e ){}

        try {
            activityDefinitionProcessor = new ActivityDefinitionProcessor( baseFhirDataProvider, (ActivityDefinition) new ActivityDefinition().setProduct( new Reference(  ).setReference( "Substance/sdakdjasl" ) ).setId( "AdGenProcedureRequest" ), "Patient/patientId","Encounter/encounterId", null, "Organization/orgId", null, null, null, null, null );
            resource = activityDefinitionProcessor.getResult();

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

}