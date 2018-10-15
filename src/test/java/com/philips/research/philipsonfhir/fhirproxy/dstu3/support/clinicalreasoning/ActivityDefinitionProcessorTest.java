package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.clinicalreasoning;

import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.NotImplementedException;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.clinicalreasoning.processor.ActivityDefinitionProcessor;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.junit.Test;
import org.opencds.cqf.cql.data.fhir.BaseFhirDataProvider;
import org.opencds.cqf.cql.data.fhir.FhirDataProviderStu3;

import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class ActivityDefinitionProcessorTest {

    @Test
    public void testProcedureRequest() throws FHIRException, NotImplementedException {
        BaseFhirDataProvider baseFhirDataProvider = mock( FhirDataProviderStu3.class );
        ActivityDefinition activityDefinition = (ActivityDefinition) new ActivityDefinition()
            .setKind( ActivityDefinition.ActivityDefinitionKind.PROCEDUREREQUEST )
            .setId( "adProcedureRequestTest" );

        Coding coding = new Coding().setSystem( "http:example.com" ).setCode( "code" ).setDisplay( "display" );
        activityDefinition.setCode( new CodeableConcept().addCoding( coding ) );

        Date now = new Date();

        String patientID = "Patient/test1";
        {
            ActivityDefinitionProcessor activityDefinitionProcessor =
                new ActivityDefinitionProcessor( baseFhirDataProvider, activityDefinition, patientID );
            IBaseResource iBaseResource = activityDefinitionProcessor.getResult();
            assertTrue( iBaseResource instanceof ProcedureRequest );
            ProcedureRequest procedureRequest = (ProcedureRequest) iBaseResource;

            assertEquals( procedureRequest.getSubject().getReference(), patientID );
            assertEquals( ProcedureRequest.ProcedureRequestIntent.ORDER, procedureRequest.getIntent() );
            assertEquals( ProcedureRequest.ProcedureRequestStatus.DRAFT, procedureRequest.getStatus() );
            assertEquals( procedureRequest.getBasedOnFirstRep().getReference(), "ActivityDefinition/" + activityDefinition.getId() );
            assertEquals( coding, procedureRequest.getCode().getCodingFirstRep() );
        }
    }

    @Test
    public void testReferralRequest() throws FHIRException, NotImplementedException {
        BaseFhirDataProvider baseFhirDataProvider = mock( FhirDataProviderStu3.class );
        ActivityDefinition activityDefinition = (ActivityDefinition) new ActivityDefinition()
            .setKind( ActivityDefinition.ActivityDefinitionKind.REFERRALREQUEST )
            .setId( "adReferralRequestTest" );

        Coding coding = new Coding().setSystem( "http:example.com" ).setCode( "code" ).setDisplay( "display" );
        activityDefinition.setCode( new CodeableConcept().addCoding( coding ) );

        Date now = new Date();

        String patientID = "Patient/test1";
        ActivityDefinitionProcessor activityDefinitionProcessor =
            new ActivityDefinitionProcessor( baseFhirDataProvider, activityDefinition, patientID );
        IBaseResource iBaseResource = activityDefinitionProcessor.getResult();
        assertTrue( iBaseResource instanceof ReferralRequest );
        ReferralRequest referralRequest = (ReferralRequest) iBaseResource;

        assertEquals( patientID, referralRequest.getSubject().getReference() );
        assertEquals( ReferralRequest.ReferralRequestStatus.DRAFT, referralRequest.getStatus() );
        assertEquals( "ActivityDefinition/" + activityDefinition.getId(), referralRequest.getDefinition().get( 0 ).getReference() );
        assertEquals( coding.getSystem(), referralRequest.getSpecialty().getCodingFirstRep() .getSystem() );
        assertEquals( coding.getCode(), referralRequest.getSpecialty().getCodingFirstRep() .getCode() );
    }

    @Test
    public void testObservation() throws FHIRException, NotImplementedException {
        Date now = new Date();
        BaseFhirDataProvider baseFhirDataProvider = mock( FhirDataProviderStu3.class );

        ActivityDefinition activityDefinition = (ActivityDefinition) new ActivityDefinition( )
            .setKind( ActivityDefinition.ActivityDefinitionKind.OBSERVATION )
            .setId( "adObs" );

        String patientID = "Patient/test1";
        {
            ActivityDefinitionProcessor activityDefinitionProcessor =
                new ActivityDefinitionProcessor( baseFhirDataProvider, activityDefinition, patientID );
            IBaseResource iBaseResource = activityDefinitionProcessor.getResult();
            assertTrue( iBaseResource instanceof Observation );
            Observation observation = (Observation) iBaseResource;

            assertEquals( observation.getSubject().getReference(), patientID );
            assertEquals( observation.getBasedOnFirstRep().getReference(), "ActivityDefinition/" + activityDefinition.getId() );
            assertEquals( Observation.ObservationStatus.PRELIMINARY, observation.getStatus() );
            assertTrue( observation.getIssued().after( now ) );
        }
        {
            Coding coding = new Coding().setSystem( "http:example.com" ).setCode( "code" ).setDisplay( "display" );
            activityDefinition.setCode( new CodeableConcept().addCoding( coding ) );

            ActivityDefinitionProcessor activityDefinitionProcessor =
                new ActivityDefinitionProcessor( baseFhirDataProvider, activityDefinition, patientID );
            IBaseResource iBaseResource = activityDefinitionProcessor.getResult();
            assertTrue( iBaseResource instanceof Observation );
            Observation observation = (Observation) iBaseResource;

            assertEquals( coding, observation.getCode().getCodingFirstRep() );
        }

    }
}