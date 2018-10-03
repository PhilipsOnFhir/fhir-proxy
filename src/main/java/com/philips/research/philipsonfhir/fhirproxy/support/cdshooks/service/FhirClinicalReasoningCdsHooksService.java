package com.philips.research.philipsonfhir.fhirproxy.support.cdshooks.service;

import com.philips.research.philipsonfhir.fhirproxy.support.bulkdata.fhir.BundleRetriever;
import com.philips.research.philipsonfhir.fhirproxy.support.cdshooks.model.CdsService;
import com.philips.research.philipsonfhir.fhirproxy.support.cdshooks.model.CdsServiceResponse;
import com.philips.research.philipsonfhir.fhirproxy.support.cdshooks.model.CdsServices;
import com.philips.research.philipsonfhir.fhirproxy.support.proxy.service.IFhirServer;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.PlanDefinition;
import org.hl7.fhir.dstu3.model.TriggerDefinition;
import org.hl7.fhir.exceptions.FHIRException;
import org.springframework.stereotype.Controller;

import java.util.Set;
import java.util.TreeSet;

@Controller
public class FhirClinicalReasoningCdsHooksService {
    IFhirServer fhirServer;

    FhirClinicalReasoningCdsHooksService( IFhirServer fhirServer ){
        this.fhirServer = fhirServer;
    }

    public CdsServices getServices() throws FHIRException {
        CdsServices cdsServices = new CdsServices();

        Bundle bundle = (Bundle) fhirServer.searchResource( "PlanDefinition", null );
        BundleRetriever bundleRetriever = new BundleRetriever( fhirServer, bundle );

        bundleRetriever.retrieveAllResources().stream()
            .filter( resource -> resource instanceof PlanDefinition )
            .map( resource -> (PlanDefinition) resource )
            .forEach( planDefinition -> {
                Set<String> hooks = getHooks( planDefinition );

                hooks.stream().forEach( hook -> {
                    CdsService cdsService = new CdsService();
                    cdsService.setHook( hook );
                    cdsService.setId( planDefinition.getIdElement().getIdPart() );
                    if ( planDefinition.hasTitle() ) {
                        cdsService.setTitle( planDefinition.getTitle() );
                    } else {
                        if ( planDefinition.hasName() ) {
                            cdsService.setTitle( planDefinition.getName() );
                        }
                    }

                    if ( planDefinition.hasDescription() ) {
                        cdsService.setDescription( planDefinition.getDescription() );
                    }

                    cdsServices.getServices().add( cdsService );
                } );
            } );

        return cdsServices;
    }

    private Set<String> getHooks(PlanDefinition planDefinition) {
        Set<String> result = new TreeSet<>();
        planDefinition.getAction().stream()
            .forEach( planDefinitionActionComponent -> result.addAll( getHooks( planDefinitionActionComponent ) ) );
        return result;
    }

    private Set<String> getHooks(PlanDefinition.PlanDefinitionActionComponent action) {
        Set<String> hooks = new TreeSet<>();

        action.getTriggerDefinition().stream()
            .filter( triggerDefinition -> triggerDefinition.getType().equals( TriggerDefinition.TriggerType.NAMEDEVENT ) )
            .map( triggerDefinition -> triggerDefinition.getEventName() )
            .forEach( eventName -> hooks.add( eventName ) );

        action.getAction().stream()
            .forEach( planDefinitionActionComponent -> hooks.addAll( getHooks( planDefinitionActionComponent ) ) );

        return hooks;
    }

    public CdsServiceResponse callCdsService(String serviceId, String requestBody) {
        return new CdsServiceResponse();
    }
}
