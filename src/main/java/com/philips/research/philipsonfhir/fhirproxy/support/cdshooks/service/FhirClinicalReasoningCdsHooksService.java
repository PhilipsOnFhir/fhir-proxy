package com.philips.research.philipsonfhir.fhirproxy.support.cdshooks.service;

import com.philips.research.philipsonfhir.fhirproxy.support.NotImplementedException;
import com.philips.research.philipsonfhir.fhirproxy.support.bulkdata.fhir.BundleRetriever;
import com.philips.research.philipsonfhir.fhirproxy.support.cdshooks.model.*;
import com.philips.research.philipsonfhir.fhirproxy.support.clinicalreasoning.PlanDefinitionProcessor;
import com.philips.research.philipsonfhir.fhirproxy.support.proxy.service.IFhirServer;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.exceptions.FHIRException;
import org.opencds.cqf.cql.data.fhir.BaseFhirDataProvider;
import org.opencds.cqf.cql.data.fhir.FhirDataProviderStu3;
import org.springframework.stereotype.Controller;

import java.util.*;

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

    public CdsServiceResponse callCdsService(String serviceId, CdsServiceCallBody requestBody) throws FHIRException, NotImplementedException {
        // requestBody.getHook() ==> event name
        // requestBody.getContext() ==> context for this hook
//        requestBody.getPrefetch() ==> ignore for now
//        requestBody.getFhirAuthorization() ==> ignore for now
        //requestBody.getUser() ==> Practitioner

//        FhirServer contentFhirServer = new FhirServer( requestBody.getFhirServer() );

        BaseFhirDataProvider baseFhirDataProvider = new FhirDataProviderStu3().setEndpoint( requestBody.getFhirServer() );
        IdType idType = new IdType( ).setValue( "PlanDefinition/"+serviceId );

        Map<String, String > contextParameters =  processContext( requestBody.getHook(), requestBody.getUser(), requestBody.getContext() );

        // requestBody.getFhirAuthorization() ==> ignore for now
        PlanDefinitionProcessor planDefinitionProcessor = new PlanDefinitionProcessor(
            baseFhirDataProvider, idType,
            contextParameters.get("patientId"),
            contextParameters.get("encounterId"),
            contextParameters.get("practitionerId" ),
            contextParameters.get("organizationId" ),
            contextParameters.get("userType"),
            contextParameters.get("userLanguage"),
            contextParameters.get("userTaskContext"),
            contextParameters.get("setting" ),
            contextParameters.get("settingContext")
        );
        CarePlan carePlan = planDefinitionProcessor.getCarePlan();
        CdsServiceResponse cdsServiceResponse = new CdsServiceResponse();
        cdsServiceResponse.setCards( carePlanToCards( carePlan ) );
        return new CdsServiceResponse();
    }

    private Map<String, String> processContext(String hook, String user, Context context) throws NotImplementedException {
        Map<String,String> result = new TreeMap<>(  );
        result.put( "practitioner", user );
        switch( hook ){
            case "patient-view":
                result.put( "patient", context.getPatientId() );
                result.put( "encounterId", context.getEncounterId() );
                break;
            default:
                throw new NotImplementedException("hook "+hook+" is not (yet) supported");
        }
        return result;
    }

    private List<Card> carePlanToCards(CarePlan carePlan) {
        // what process to take?
        /*
         ? each trigger results in a card?
         ? each base tree maps to a card
         Base tree is easier as it does not require linkin PD.action to Action

         tree processsing
            all actions beneath - format text into card -- markdown based -- use heading and text
            grouping behavior --
                ignored
            selection behavior other than "select one"??tue?? --> error
            pre-check -- not allowed
            cardianality -- not allowed
            requiredBehavor - could or abseent
            other fields also not allowed

            type/resource --> suggestion, if on top -- one general "accept"
            selection behavior -- all in base trees below as one suggestion option possibly multiple resources

            other fields -- description
            condition -- evaluate now -- if true include (pointless)

            related action -- include text -- treat as subtrees?
         */
        return CarePlanToCard.convert(  carePlan );
    }

}
