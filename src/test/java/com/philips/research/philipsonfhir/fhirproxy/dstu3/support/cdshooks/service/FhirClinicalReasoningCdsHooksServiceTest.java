package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.cdshooks.service;

import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.cdshooks.model.CdsService;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.cdshooks.model.CdsServices;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.service.FhirServer;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.service.IFhirServer;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.PlanDefinition;
import org.hl7.fhir.dstu3.model.TriggerDefinition;
import org.hl7.fhir.exceptions.FHIRException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FhirClinicalReasoningCdsHooksServiceTest {

    @Test
    public void retrieveServices() throws FHIRException {
        IFhirServer fhirServer = mock( FhirServer.class );
        String resourceType = "PlanDefinition";

        Bundle bundle = new Bundle(  );

        PlanDefinition pd1 = (PlanDefinition) new PlanDefinition(  ).setId("pd1");
        bundle.addEntry( new Bundle.BundleEntryComponent().setResource( pd1 ) );
        when ( fhirServer.searchResource( resourceType,null )).thenReturn( bundle );

        FhirClinicalReasoningCdsHooksService fhirClinicalReasoningCdsHooksService = new FhirClinicalReasoningCdsHooksService( fhirServer );
        CdsServices cdsServices = fhirClinicalReasoningCdsHooksService.getServices();
        assertNotNull( cdsServices );
        assertEquals( "Should ignore PD's with not triggers", 0, cdsServices.getServices().size() );

        PlanDefinition pd2 = (PlanDefinition) new PlanDefinition(  )
            .setTitle( "PD title" )
            .addAction( new PlanDefinition.PlanDefinitionActionComponent()
                .addTriggerDefinition( new TriggerDefinition(  )
                    .setType( TriggerDefinition.TriggerType.NAMEDEVENT )
                    .setEventName( "patient-view" )
                )
                .setTitle( "sometitle" )
                .setDescription( "somedescription" )
            )
            .setId("pd2");
        bundle.addEntry( new Bundle.BundleEntryComponent().setResource( pd2 ) );

        cdsServices = fhirClinicalReasoningCdsHooksService.getServices();
        assertNotNull( cdsServices );
        assertEquals("One patient-view", 1, cdsServices.getServices().size() );
        CdsService cdsService = cdsServices.getServices().get( 0 );
        assertEquals( "patient-view", cdsService.getHook() );
        assertEquals( pd2.getTitle(), cdsService.getTitle() );
        assertEquals( pd2.getDescription(), cdsService.getDescription() );
    }

//    @Test
//    public void callService() throws FHIRException, NotImplementedException {
//        IFhirServer fhirServer = mock( FhirServer.class );
//        String resourceType = "PlanDefinition";
//
//        Bundle bundle = new Bundle(  );
//
//        when ( fhirServer.searchResource( resourceType,null )).thenReturn( bundle );
//        when ( fhirServer.getUrl()).thenReturn( "http://dummyServer" );
//
//        FhirClinicalReasoningCdsHooksService fhirClinicalReasoningCdsHooksService = new FhirClinicalReasoningCdsHooksService( fhirServer );
//
//        PlanDefinition pd3 = (PlanDefinition) new PlanDefinition(  )
//            .setTitle( "PD title" )
//            .addAction( new PlanDefinition.PlanDefinitionActionComponent()
//                .addTriggerDefinition( new TriggerDefinition(  )
//                    .setType( TriggerDefinition.TriggerType.NAMEDEVENT )
//                    .setEventName( "patient-view" )
//                )
//                .setTitle( "sometitle" )
//                .setDescription( "somedescription" )
//            )
//            .setId("pd3");
//        bundle.addEntry( new Bundle.BundleEntryComponent().setResource( pd3 ) );
//
//        CdsServiceCallBody cdsServiceCallBody = new CdsServiceCallBody();
//        cdsServiceCallBody.setHook( "patient-view" );
//        cdsServiceCallBody.setUser( "Practioner/pracId" );
//        Context context = new Context();
//        context.setPatientId( "Patient/somePatientId" );
//        cdsServiceCallBody.setContext(  context );
//        fhirClinicalReasoningCdsHooksService.callCdsService( pd3.getId(), cdsServiceCallBody );
//    }
}