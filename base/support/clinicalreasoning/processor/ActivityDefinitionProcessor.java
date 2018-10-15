package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.clinicalreasoning.processor;

import ca.uhn.fhir.context.FhirContext;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.FhirValueSetter;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.NotImplementedException;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.clinicalreasoning.cql.CqlExecutionProvider;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.clinicalreasoning.cql.MyWorkerContext;
import org.hl7.fhir.dstu3.hapi.validation.DefaultProfileValidationSupport;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.dstu3.utils.FHIRPathEngine;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.cql.data.fhir.BaseFhirDataProvider;

import java.util.*;
import java.util.logging.Logger;

/**
 * Created by Bryn on 1/16/2017.
 */
public class ActivityDefinitionProcessor  {
//    private static final Logger logger = Logger.getLogger( ActivityDefinitionProcessor.class.getName());
    private Logger logger = Logger.getLogger( this.getClass().getName());
    private final ActivityDefinition activityDefinition;
    private BaseFhirDataProvider fhirDataProvider;
    private StructureMapTransformServer transformServer = null;
    private CqlExecutionProvider executionProvider;

    ///////////////////////////////////////
    private static MyWorkerContext hapiWorkerContext;
    private static final FhirContext ourCtx = FhirContext.forDstu3();
    private static FHIRPathEngine fhirPathEngine ;
    private Resource result= null;


    public static void initialize(){
        if ( hapiWorkerContext==null ) {
            hapiWorkerContext = new MyWorkerContext( ourCtx, new DefaultProfileValidationSupport() );
        }
        if ( fhirPathEngine==null ) {
            fhirPathEngine = new FHIRPathEngine( hapiWorkerContext );
        }
    }
    ///////////////////////////////////////

    public ActivityDefinitionProcessor(BaseFhirDataProvider fhirDataProvider, ActivityDefinition activityDefinition, String patientId) throws FHIRException, NotImplementedException {
        initialize();

        this.activityDefinition = activityDefinition;
        this.fhirDataProvider =fhirDataProvider;
        process( patientId, null, null, null, null, null, null, null, null );
    }

    public ActivityDefinitionProcessor(BaseFhirDataProvider fhirDataProvider, ActivityDefinition activityDefinition, String patientId, String encounterId, String practitionerId, String organizationId, String userType, String userLanguage, String userTaskContext, String setting, String settingContext) throws FHIRException, NotImplementedException {
        initialize();
        this.activityDefinition = activityDefinition;
        this.fhirDataProvider =fhirDataProvider;
        process( patientId, encounterId, practitionerId, organizationId, userType, userLanguage, userTaskContext, setting, settingContext);
    }


    public ActivityDefinitionProcessor(BaseFhirDataProvider fhirDataProvider
        , IdType activityDefinitionId
        , String patientId, String encounterId, String practitionerId, String organizationId, String userType, String userLanguage, String userTaskContext, String setting, String settingContext
    ) throws FHIRException, NotImplementedException {
        initialize();
        this.fhirDataProvider = fhirDataProvider;
        this.activityDefinition = fhirDataProvider.getFhirClient().read()
            .resource( ActivityDefinition.class ).withId( activityDefinitionId ).execute();
//        JpaResourceProviderDstu3<? extends PlanDefinition> planDefinitionResourceProvider =
//            (JpaResourceProviderDstu3<? extends PlanDefinition>) clinReasoningProvider.resolveResourceProvider( ResourceType.PlanDefinition.name() );

//        this.planDefinition = planDefinitionResourceProvider.getDao().read( planDefinitionId );
        if ( activityDefinition == null ) {
            throw new IllegalArgumentException( "Couldn't find ActivityDefinition "+activityDefinitionId  );
        }
        process( patientId, encounterId, practitionerId, organizationId, userType, userLanguage, userTaskContext, setting, settingContext );
    }

    private void process(String patientId, String encounterId, String practitionerId, String organizationId, String userType, String userLanguage, String userTaskContext, String setting, String settingContext
    ) throws FHIRException, NotImplementedException {
        resolveActivityDefinition(activityDefinition, patientId, practitionerId, organizationId);
    }

    // For library use
    public Resource resolveActivityDefinition(ActivityDefinition activityDefinition, String patientId,
                                              String practitionerId, String organizationId)
        throws FHIRException, NotImplementedException {
        transformServer = new StructureMapTransformServer( fhirDataProvider.getFhirClient() );
        // create result resource
        result = null;
        try {
            // This is a little hacky...
            result = (Resource) Class.forName("org.hl7.fhir.dstu3.model." + activityDefinition.getKind().toCode()).newInstance();
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new FHIRException("Could not find org.hl7.fhir.dstu3.model." + activityDefinition.getKind().toCode());
        }

        switch (result.fhirType()) {
            case "ProcedureRequest":
                result = resolveProcedureRequest(activityDefinition, patientId, practitionerId, organizationId);
                break;

            case "MedicationRequest":
                result = resolveMedicationRequest(activityDefinition, patientId);
                break;

            case "SupplyRequest":
                result = resolveSupplyRequest(activityDefinition, practitionerId, organizationId);
                break;

            case "Procedure":
                result = resolveProcedure(activityDefinition, patientId);
                break;

            case "DiagnosticReport":
                result = resolveDiagnosticReport(activityDefinition, patientId);
                break;

            case "Communication":
                result = resolveCommunication(activityDefinition, patientId);
                break;

            case "CommunicationRequest":
                result = resolveCommunicationRequest(activityDefinition, patientId);
                break;

            case "ReferralRequest":
                result = resolveReferralRequest(activityDefinition, patientId, practitionerId, organizationId);
                break;

            case "Observation":
                result = resolveObservation(activityDefinition, patientId, practitionerId, organizationId);
                break;
        }

        if ( activityDefinition.hasTransform() ){
            Reference transformReference = activityDefinition.getTransform();
            final String rn = "StructuredMap/";
            String strMapId = transformReference.getReference().substring( rn.length() );

            StructureMap structureMap = null;
            if ( strMapId.startsWith( "#" )){
                Optional<StructureMap> opt = activityDefinition.getContained().stream()
                    .filter( resource -> resource.getId().equals( strMapId.substring( 1 ) ) )
                    .filter( resource -> resource instanceof StructureMap )
                    .map( resource -> (StructureMap)resource )
                    .findFirst();
                if ( opt.isPresent() ) { structureMap=opt.get(); }
            } else {
                structureMap =
                    this.fhirDataProvider.getFhirClient().read().resource( StructureMap.class ).withId( strMapId ).execute();
            }
            if ( structureMap==null ){
                throw new FHIRException("StructureMap "+strMapId+" cannot be found");
            }

            IBaseResource resource  = this.transformServer.doTransform( structureMap, activityDefinition, (Resource) result);
//            Resource resouce = fhirServer.postResource("StructuredMap", transformReference.getId(), null. null );
//            FHIRStructureMapResourceProvider fhirStructureMapResourceProvider = (FHIRStructureMapResourceProvider) provider.resolveResourceProvider("StructureMap");
//            Resource resource = fhirStructureMapResourceProvider.internalTransform( transformReference, activityDefinition, result );
            result= (Resource) resource;
        }

        if( activityDefinition.hasDynamicValue() ) {
            CqlExecutionProvider cqlExecutionProvider = new CqlExecutionProvider( this.fhirDataProvider, activityDefinition, patientId );

            for ( ActivityDefinition.ActivityDefinitionDynamicValueComponent dynamicValue : activityDefinition.getDynamicValue() ) {
                if ( dynamicValue.hasPath() && dynamicValue.hasExpression() ) {
                    Object dynValResult = null;
                    switch ( dynamicValue.getLanguage() ) {
                        case "text/fhirpath":
                            dynValResult = fhirPathEngine.evaluate( this.activityDefinition, dynamicValue.getExpression() );
                            if ( !((List) dynValResult).isEmpty() ) {
                                dynValResult = ((List) dynValResult).get( 0 );
                            }
                            break;
                        case "text/cql":
                        default:
                            dynValResult = cqlExecutionProvider.evaluateInContext( dynamicValue.getExpression() );
                    }

                    if ( dynamicValue.getPath().equals( "$this" ) ) {
                        this.result = (Resource) dynValResult;
                    } else {
                        //                    this.clinReasoningProvider.setValue( carePlan, dynamicValue.getPath(), result );
                        FhirValueSetter.setProperty( result, dynamicValue.getPath(), (Base) dynValResult );
                    }
                }
            }
        }
        return result;
    }

    private Resource resolveObservation(ActivityDefinition activityDefinition, String patientId, String practitionerId, String organizationId) {
        Observation observation = new Observation(  );
        observation.setCode( activityDefinition.getCode() );
        observation.setIssued( new Date(  ) );
        observation.setStatus( Observation.ObservationStatus.PRELIMINARY );
        observation.addBasedOn( new Reference(  ).setReference( activityDefinition.getResourceType()+"/"+activityDefinition.getId() ) );
        observation.setSubject(new Reference(patientId));

        return observation;
    }

    private ProcedureRequest resolveProcedureRequest(ActivityDefinition activityDefinition, String patientId,
                                                     String practitionerId, String organizationId)
        throws ActivityDefinitionApplyException
    {
        // status, intent, code, and subject are required
        ProcedureRequest procedureRequest = new ProcedureRequest();
        procedureRequest.setStatus(ProcedureRequest.ProcedureRequestStatus.DRAFT);
        procedureRequest.setIntent(ProcedureRequest.ProcedureRequestIntent.ORDER);
        procedureRequest.setSubject(new Reference(patientId));
        procedureRequest.addBasedOn( new Reference().setReference( activityDefinition.getResourceType()+"/"+activityDefinition.getId() ));

        if (practitionerId != null) {
            procedureRequest.setRequester(
                new ProcedureRequest.ProcedureRequestRequesterComponent()
                    .setAgent(new Reference(practitionerId))
            );
        }

        else if (organizationId != null) {
            procedureRequest.setRequester(
                new ProcedureRequest.ProcedureRequestRequesterComponent()
                    .setAgent(new Reference(organizationId))
            );
        }

        if (activityDefinition.hasExtension()) {
            procedureRequest.setExtension(activityDefinition.getExtension());
        }

        if (activityDefinition.hasCode()) {
            procedureRequest.setCode(activityDefinition.getCode());
        }

        // code can be set as a dynamicValue
        else if (!activityDefinition.hasCode() && !activityDefinition.hasDynamicValue()) {
            throw new ActivityDefinitionApplyException("Missing required code property");
        }

        if (activityDefinition.hasBodySite()) {
            procedureRequest.setBodySite( activityDefinition.getBodySite());
        }

        if (activityDefinition.hasProduct()) {
            throw new ActivityDefinitionApplyException("Product does not map to "+activityDefinition.getKind());
        }

        if (activityDefinition.hasDosage()) {
            throw new ActivityDefinitionApplyException("Dosage does not map to "+activityDefinition.getKind());
        }

        return procedureRequest;
    }

    private MedicationRequest resolveMedicationRequest(ActivityDefinition activityDefinition, String patientId)
        throws ActivityDefinitionApplyException
    {
        // intent, medication, and subject are required
        MedicationRequest medicationRequest = new MedicationRequest();
        medicationRequest.setIntent(MedicationRequest.MedicationRequestIntent.ORDER);
        medicationRequest.setSubject(new Reference(patientId));

        if (activityDefinition.hasProduct()) {
            medicationRequest.setMedication( activityDefinition.getProduct());
        }

        else {
            throw new ActivityDefinitionApplyException("Missing required product property");
        }

        if (activityDefinition.hasDosage()) {
            medicationRequest.setDosageInstruction( activityDefinition.getDosage());
        }

        if (activityDefinition.hasBodySite()) {
            throw new ActivityDefinitionApplyException("Bodysite does not map to " + activityDefinition.getKind());
        }

        if (activityDefinition.hasCode()) {
            throw new ActivityDefinitionApplyException("Code does not map to " + activityDefinition.getKind());
        }

        if (activityDefinition.hasQuantity()) {
            throw new ActivityDefinitionApplyException("Quantity does not map to " + activityDefinition.getKind());
        }

        return medicationRequest;
    }

    private SupplyRequest resolveSupplyRequest(ActivityDefinition activityDefinition, String practionerId,
                                               String organizationId) throws ActivityDefinitionApplyException
    {
        SupplyRequest supplyRequest = new SupplyRequest();

        if (practionerId != null) {
            supplyRequest.setRequester(
                new SupplyRequest.SupplyRequestRequesterComponent()
                    .setAgent(new Reference(practionerId))
            );
        }

        if (organizationId != null) {
            supplyRequest.setRequester(
                new SupplyRequest.SupplyRequestRequesterComponent()
                    .setAgent(new Reference(organizationId))
            );
        }

        if (activityDefinition.hasQuantity()){
            supplyRequest.setOrderedItem(
                new SupplyRequest.SupplyRequestOrderedItemComponent()
                    .setQuantity( activityDefinition.getQuantity())
            );
        }

        else {
            throw new ActivityDefinitionApplyException("Missing required orderedItem.quantity property");
        }

        if (activityDefinition.hasCode()) {
            supplyRequest.getOrderedItem().setItem(activityDefinition.getCode());
        }

        if (activityDefinition.hasProduct()) {
            throw new ActivityDefinitionApplyException("Product does not map to "+activityDefinition.getKind());
        }

        if (activityDefinition.hasDosage()) {
            throw new ActivityDefinitionApplyException("Dosage does not map to "+activityDefinition.getKind());
        }

        if (activityDefinition.hasBodySite()) {
            throw new ActivityDefinitionApplyException("Bodysite does not map to "+activityDefinition.getKind());
        }

        return supplyRequest;
    }

    private Procedure resolveProcedure(ActivityDefinition activityDefinition, String patientId) {
        Procedure procedure = new Procedure();

        // TODO - set the appropriate status
        procedure.setStatus(Procedure.ProcedureStatus.UNKNOWN);
        procedure.setSubject(new Reference(patientId));

        if (activityDefinition.hasCode()) {
            procedure.setCode(activityDefinition.getCode());
        }

        if (activityDefinition.hasBodySite()) {
            procedure.setBodySite(activityDefinition.getBodySite());
        }

        return procedure;
    }

    private DiagnosticReport resolveDiagnosticReport(ActivityDefinition activityDefinition, String patientId) throws ActivityDefinitionApplyException {
        DiagnosticReport diagnosticReport = new DiagnosticReport();

        diagnosticReport.setStatus(DiagnosticReport.DiagnosticReportStatus.UNKNOWN);
        diagnosticReport.setSubject(new Reference(patientId));

        if (activityDefinition.hasCode()) {
            diagnosticReport.setCode(activityDefinition.getCode());
        }

        else {
            throw new ActivityDefinitionApplyException("Missing required ActivityDefinition.code property for DiagnosticReport");
        }

        if (activityDefinition.hasRelatedArtifact()) {
            List<Attachment> presentedFormAttachments = new ArrayList<>();
            for (RelatedArtifact artifact : activityDefinition.getRelatedArtifact()) {
                Attachment attachment = new Attachment();

                if (artifact.hasUrl()) {
                    attachment.setUrl(artifact.getUrl());
                }

                if (artifact.hasDisplay()) {
                    attachment.setTitle(artifact.getDisplay());
                }
                presentedFormAttachments.add(attachment);
            }
            diagnosticReport.setPresentedForm(presentedFormAttachments);
        }

        return diagnosticReport;
    }

    private Communication resolveCommunication(ActivityDefinition activityDefinition, String patientId) {
        Communication communication = new Communication();

        communication.setStatus(Communication.CommunicationStatus.UNKNOWN);
        communication.setSubject(new Reference(patientId));

        if (activityDefinition.hasCode()) {
            communication.setReasonCode(Collections.singletonList(activityDefinition.getCode()));
        }

        if (activityDefinition.hasRelatedArtifact()) {
            for (RelatedArtifact artifact : activityDefinition.getRelatedArtifact()) {
                if (artifact.hasUrl()) {
                    Attachment attachment = new Attachment().setUrl(artifact.getUrl());
                    if (artifact.hasDisplay()) {
                        attachment.setTitle(artifact.getDisplay());
                    }

                    Communication.CommunicationPayloadComponent payload = new Communication.CommunicationPayloadComponent();
                    payload.setContent(artifact.hasDisplay() ? attachment.setTitle(artifact.getDisplay()) : attachment);
                    communication.setPayload(Collections.singletonList(payload));
                }

                // TODO - other relatedArtifact types
            }
        }

        return communication;
    }

    // TODO - extend this to be more complete
    private CommunicationRequest resolveCommunicationRequest(ActivityDefinition activityDefinition, String patientId) {
        CommunicationRequest communicationRequest = new CommunicationRequest();

        communicationRequest.setStatus(CommunicationRequest.CommunicationRequestStatus.UNKNOWN);
        communicationRequest.setSubject(new Reference(patientId));

        // Unsure if this is correct - this is the way Motive is doing it...
        if (activityDefinition.hasCode()) {
            if (activityDefinition.getCode().hasText()) {
                communicationRequest.addPayload().setContent(new StringType(activityDefinition.getCode().getText()));
            }
        }

        return communicationRequest;
    }

    private Resource resolveReferralRequest(ActivityDefinition activityDefinition, String patientId,
                                            String practitionerId, String organizationId) {
        ReferralRequest referralRequest = new ReferralRequest();
        referralRequest.setStatus(ReferralRequest.ReferralRequestStatus.DRAFT);
        referralRequest.addDefinition(new Reference().setReference( activityDefinition.getResourceType()+"/"+activityDefinition.getId() ));
        referralRequest.setSubject(new Reference(patientId));
        referralRequest.setIntent( ReferralRequest.ReferralCategory.ORDER );
        referralRequest.setSpecialty( activityDefinition.getCode() );

        if (practitionerId != null) {
            referralRequest.setRequester(
                new ReferralRequest.ReferralRequestRequesterComponent()
                    .setAgent(new Reference(practitionerId))
            );
        }

        else if (organizationId != null) {
            referralRequest.setRequester(
                new ReferralRequest.ReferralRequestRequesterComponent()
                    .setAgent(new Reference(organizationId))
            );
        }

        return referralRequest;
    }

    public IBaseResource getResult() {
        return result;
    }

    private class ActivityDefinitionApplyException extends FHIRException {
        public ActivityDefinitionApplyException(String s) {
            super(s);
        }
    }
}
