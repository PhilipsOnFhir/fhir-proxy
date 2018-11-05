package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.clinicalreasoning.processor;

import ca.uhn.fhir.context.FhirContext;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.FhirValueSetter;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.NotImplementedException;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.clinicalreasoning.cql.CqlExecutionProvider;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.clinicalreasoning.cql.MyWorkerContext;
import org.cqframework.cql.elm.execution.VersionedIdentifier;
import org.hl7.fhir.dstu3.hapi.validation.DefaultProfileValidationSupport;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.dstu3.utils.FHIRPathEngine;
import org.hl7.fhir.exceptions.FHIRException;
import org.opencds.cqf.cql.data.fhir.BaseFhirDataProvider;

import java.util.*;
import java.util.logging.Logger;

public class PlanDefinitionProcessor {

    private Logger logger = Logger.getLogger( this.getClass().getName());
//    private static final Logger logger = Logger.getLogger(PlanDefinitionProcessor.class.getName());
    private List<Object> contextParameters = null;
    private PlanDefinition planDefinition;
    private CqlExecutionProvider cqlExecutionProvider;
    private String patientId;
    private String encounterId;
    private String practitionerId;
    private String organizationId;
    private String userLanguage;
    private String userType;
    private String userTaskContext;
    private String setting;
    private String settingContext;
    private CarePlan carePlan;
    private BaseFhirDataProvider fhirDataProvider = null;
    private final Map<String, Library> libraryCache = new TreeMap<>();

    ///////////////////////////////////////
    private static MyWorkerContext hapiWorkerContext;
    private static final FhirContext ourCtx = FhirContext.forDstu3();
    private static FHIRPathEngine fhirPathEngine ;
    private static StructureMapTransformServer structureMapTransformServer = null;


    public static void initialize(){
        if ( hapiWorkerContext==null ) {
            hapiWorkerContext = new MyWorkerContext( ourCtx, new DefaultProfileValidationSupport() );
        }
        if ( fhirPathEngine==null ) {
            fhirPathEngine = new FHIRPathEngine( hapiWorkerContext );
        }
        if( structureMapTransformServer==null ){
            structureMapTransformServer = new StructureMapTransformServer( ourCtx );
        }
    }
    ///////////////////////////////////////

//    public PlanDefinitionProcessor(BaseFhirDataProvider dataSource, PlanDefinition planDefinition, String patientId) throws FHIRException, NotImplementedException {
//        initialize();
//
//        this.planDefinition = planDefinition;
//        process( fhirDataProvider, patientId, encounterId, practitionerId, organizationId, userType, userLanguage, userTaskContext, setting, settingContext );
//    }
//
//    public PlanDefinitionProcessor(BaseFhirDataProvider dataProvider, List<Object> contextParameters, PlanDefinition planDefinition, String patientId) throws FHIRException, NotImplementedException {
//        initialize();
//        this.contextParameters = contextParameters;
//        this.planDefinition = planDefinition;
//        process( fhirDataProvider, patientId, encounterId, practitionerId, organizationId, userType, userLanguage, userTaskContext, setting, settingContext );
//    }

    public PlanDefinitionProcessor(BaseFhirDataProvider fhirDataProvider
        , IdType planDefinitionId
        , String patientId, String encounterId, String practitionerId, String organizationId, String userType, String userLanguage, String userTaskContext, String setting, String settingContext
    ) throws FHIRException, NotImplementedException {
        initialize();
        this.planDefinition = fhirDataProvider.getFhirClient().read()
            .resource( PlanDefinition.class ).withId( planDefinitionId ).execute();
//        JpaResourceProviderDstu3<? extends PlanDefinition> planDefinitionResourceProvider =
//            (JpaResourceProviderDstu3<? extends PlanDefinition>) clinReasoningProvider.resolveResourceProvider( ResourceType.PlanDefinition.name() );

//        this.planDefinition = planDefinitionResourceProvider.getDao().read( planDefinitionId );
        if ( planDefinition == null ) {
            throw new IllegalArgumentException( "Couldn't find PlanDefinition "+planDefinitionId  );
        }
        process( fhirDataProvider, patientId, encounterId, practitionerId, organizationId, userType, userLanguage, userTaskContext, setting, settingContext );
    }

    private void process(BaseFhirDataProvider fhirDataProvider
        , String patientId, String encounterId, String practitionerId, String organizationId, String userType, String userLanguage, String userTaskContext, String setting, String settingContext
    ) throws FHIRException, NotImplementedException {
        logger.info( "Performing $apply operation on PlanDefinition/" + planDefinition.getId() );
        this.fhirDataProvider = fhirDataProvider;
        this.patientId      = patientId;
        this.encounterId    = encounterId;
        this.practitionerId = practitionerId;
        this.organizationId = organizationId;
        this.userType       = userType;
        this.userLanguage   = userLanguage;
        this.userTaskContext = userTaskContext;
        this.setting         = setting;
        this.settingContext  = settingContext;

        logger.info( "Performing $apply operation on PlanDefinition/" + planDefinition.getId() );

        /******************************/
        if ( planDefinition.getLibrary().size()==1 ){
            VersionedIdentifier vid = new VersionedIdentifier().withId(planDefinition.getLibrary().get( 0).getReference());
            cqlExecutionProvider = new CqlExecutionProvider( fhirDataProvider, planDefinition,  patientId, vid,  contextParameters );
        } else {
            cqlExecutionProvider = new CqlExecutionProvider( fhirDataProvider, planDefinition,  patientId, contextParameters );
        }

        /******************************/


        cqlExecutionProvider = new CqlExecutionProvider( fhirDataProvider, planDefinition,  patientId, contextParameters );

        cqlExecutionProvider = new CqlExecutionProvider( fhirDataProvider, planDefinition, patientId, contextParameters );
        if (contextParameters!=null && !contextParameters.isEmpty()) {
            cqlExecutionProvider.setContextParameters( contextParameters );
        }
        carePlan = new CarePlan(  );
        carePlan.addDefinition(  new Reference( ResourceType.PlanDefinition.name()+ "/" + planDefinition.getIdElement().getIdPart() ) );
        carePlan.setSubject(  new Reference( patientId ) );
        carePlan.setStatus( CarePlan.CarePlanStatus.ACTIVE );
        carePlan.setTitle( "Application of " + (planDefinition.hasTitle() ? planDefinition.getTitle() : planDefinition.getId()) );
        carePlan.setDescription( planDefinition.getDescription() );

        if ( encounterId != null )   { carePlan.setContext( new Reference( encounterId ) );}
        if ( practitionerId != null ){ carePlan.addAuthor( new Reference( practitionerId ) ); }
        if ( organizationId != null ){ carePlan.addAuthor( new Reference( organizationId ) ); }
        if ( userLanguage != null )  { carePlan.setLanguage( userLanguage );}

        RequestGroup requestGroup = new RequestGroup();
        requestGroup.setIntent( RequestGroup.RequestIntent.PROPOSAL );
        requestGroup.setStatus( RequestGroup.RequestStatus.DRAFT );
        requestGroup.setId( "#requestGroup" );

        carePlan.addContained( requestGroup );
        carePlan.addActivity( new CarePlan.CarePlanActivityComponent()
            .setReferenceTarget( requestGroup )
            .setReference( new Reference( requestGroup.getId() ) )
        );

        logger.info( "Processing actions of PlanDefinition/" + planDefinition.getId() );
        for ( PlanDefinition.PlanDefinitionActionComponent action: planDefinition.getAction()) {
            RequestGroup.RequestGroupActionComponent requestGroupActionComponent = resolveAction( action );
            if ( requestGroupActionComponent!=null ) {
                requestGroup.addAction( requestGroupActionComponent );
            }
        }
        logger.info( "Processing action of PlanDefinition - done" );
    }

    private RequestGroup.RequestGroupActionComponent resolveAction(PlanDefinition.PlanDefinitionActionComponent carePlanAction ) throws FHIRException {
        logger.info( "Processing action of PlanDefinition" );
        if ( conditionsMet( carePlanAction ) ) {
            logger.info( "Processing action of PlanDefinition - condition met" );
            return processAction( carePlanAction );
        }
        return null;
    }


    private boolean conditionsMet(PlanDefinition.PlanDefinitionActionComponent carePlanAction) throws FHIRException {
        boolean conditionsMet = true;
        for ( PlanDefinition.PlanDefinitionActionConditionComponent condition: carePlanAction.getCondition()) {
            if ( condition.getKind() == PlanDefinition.ActionConditionKind.APPLICABILITY ) {
                if ( !condition.hasExpression() ) {
                    continue;
                }

                Object result = null;
                switch( condition.getLanguage() ) {
                    case "text/fhirpath":
                        logger.info( "Processing action of PlanDefinition - condition  "+condition.getExpression() );
                        result = fhirPathEngine.evaluate( this.planDefinition, condition.getExpression() );
                        if ( !((List) result).isEmpty() ) {
                            result = ((List) result).get( 0 );
                        }
                        break;
                    case "text/cql":
                    default:
                        logger.info( "Processing action of PlanDefinition - condition  "+condition.getExpression() );
                        result = cqlExecutionProvider.evaluateInContext(  condition.getExpression() );
                }
                if ( !(result instanceof Boolean) ) {
                    continue;
                }

                if ( !(Boolean) result ) {
                    conditionsMet = false;
                }
            }
        }
        return conditionsMet;
    }

    private RequestGroup.RequestGroupActionComponent processAction(PlanDefinition.PlanDefinitionActionComponent planDefinitionAction
    ) throws FHIRException {
        RequestGroup.RequestGroupActionComponent requestGroupAction = new RequestGroup.RequestGroupActionComponent();

        if ( planDefinitionAction.hasTitle() ) {
            requestGroupAction.setTitle( planDefinitionAction.getTitle() );
        }
        if ( planDefinitionAction.hasDescription() ) {
            requestGroupAction.setDescription( planDefinitionAction.getDescription() );
        }
        // source
        if ( planDefinitionAction.hasDocumentation() ) {
            RelatedArtifact artifact = planDefinitionAction.getDocumentationFirstRep().copy();

//            RelatedArtifactBuilder artifactBuilder = new RelatedArtifactBuilder();
//            if ( artifact.hasDisplay() ) {
//                artifactBuilder.buildDisplay( artifact.getDisplay() );
//            }
//            if ( artifact.hasUrl() ) {
//                artifactBuilder.buildUrl( artifact.getUrl() );
//            }
//            if ( artifact.hasDocument() && artifact.getDocument().hasUrl() ) {
//                AttachmentBuilder attachmentBuilder = new AttachmentBuilder();
//                attachmentBuilder.buildUrl( artifact.getDocument().getUrl() );
//                artifactBuilder.buildDocument( attachmentBuilder.build() );
//            }
            requestGroupAction.setDocumentation( Collections.singletonList( artifact ) );
        }

        // suggestions
        // TODO - uuid
        if ( planDefinitionAction.hasLabel() ) {
            requestGroupAction.setLabel( planDefinitionAction.getLabel() );
        }
        if ( planDefinitionAction.hasType() ) {
            requestGroupAction.setType( planDefinitionAction.getType() );
        }
        if (planDefinitionAction.hasCardinalityBehavior()){
            requestGroupAction.setCardinalityBehavior( RequestGroup.ActionCardinalityBehavior.fromCode(  planDefinitionAction.getCardinalityBehavior().toCode() ) );
        }
        if (planDefinitionAction.hasGroupingBehavior()){
            requestGroupAction.setGroupingBehavior( RequestGroup.ActionGroupingBehavior.fromCode( planDefinitionAction.getGroupingBehavior().toCode() ));
        }
        if( planDefinitionAction.hasPrecheckBehavior() ){
            requestGroupAction.setPrecheckBehavior( RequestGroup.ActionPrecheckBehavior.fromCode( planDefinitionAction.getPrecheckBehavior().toCode() ));
        }
        if( planDefinitionAction.hasSelectionBehavior() ){
            requestGroupAction.setSelectionBehavior( RequestGroup.ActionSelectionBehavior.fromCode(  planDefinitionAction.getSelectionBehavior().toCode() ));
        }

        if ( planDefinitionAction.hasDefinition() ) {
            logger.info( "Processing action of PlanDefinition - process definition "+planDefinitionAction.getDefinition().getReference() );
            processDefinition( planDefinitionAction, requestGroupAction );
        }
        if (planDefinitionAction.hasDynamicValue()) {
            logger.info( "Processing action of PlanDefinition - process dynamic values" );
            processDynamicValues( planDefinitionAction, requestGroupAction );
        }
        for ( PlanDefinition.PlanDefinitionActionComponent subAction: planDefinitionAction.getAction() ){
            logger.info( "Processing action of PlanDefinition - process sub actions" );
            RequestGroup.RequestGroupActionComponent subRequestGroupAction = resolveAction( subAction );
            if ( subRequestGroupAction!=null ) {
                requestGroupAction.addAction( subRequestGroupAction );
            }
        }
        return requestGroupAction;
    }

    private void processDynamicValues(PlanDefinition.PlanDefinitionActionComponent planDefinitionAction, RequestGroup.RequestGroupActionComponent requestGroupAction) throws FHIRException {
        for ( PlanDefinition.PlanDefinitionActionDynamicValueComponent dynamicValue : planDefinitionAction.getDynamicValue()) {
            if (dynamicValue.hasPath() && dynamicValue.hasExpression()) {
                Object result = null;
                switch( dynamicValue.getLanguage() ) {
                    case "text/fhirpath":
                        result = fhirPathEngine.evaluate( this.planDefinition, dynamicValue.getExpression() );
                        if ( !((List) result).isEmpty() ) {
                            result = ((List) result).get( 0 );
                        }
                        break;
                    case "text/cql":
                    default:
                        result = cqlExecutionProvider.evaluateInContext(  dynamicValue.getExpression() );
                }

                if ( dynamicValue.getPath().startsWith( "%action" ) ) {
                    FhirValueSetter.setProperty( requestGroupAction, dynamicValue.getPath().replace("%action",""), (Base) result );
                } else if ( dynamicValue.getPath().equals( "%context" ) ) {
                    this.carePlan = (CarePlan)carePlan;
                } else if ( dynamicValue.getPath().startsWith( "%context" ) ) {
                    FhirValueSetter.setProperty( requestGroupAction, dynamicValue.getPath().replace("%context",""), (Base) result );
                } else {
//                    this.clinReasoningProvider.setValue( carePlan, dynamicValue.getPath(), result );
                    FhirValueSetter.setProperty( carePlan, dynamicValue.getPath(), (Base) result );
                }
            }
        }
    }

    private void processDefinition(PlanDefinition.PlanDefinitionActionComponent planDefinitionAction, RequestGroup.RequestGroupActionComponent requestGroupAction) throws FHIRException {
        String definitionType = planDefinitionAction.getDefinition().getReferenceElement().getResourceType();
        Resource resource = null;
        if ( definitionType.equals( ResourceType.ActivityDefinition.name() ) ) {

            ActivityDefinition activityDefinition = null;
            if (planDefinitionAction.getDefinition().getReferenceElement().getIdPart().startsWith("#")) {
                activityDefinition = (ActivityDefinition) resolveContained(planDefinition, planDefinitionAction.getDefinition().getReferenceElement().getIdPart() );
            }
            if ( planDefinitionAction.getDefinition().getResource() != null ) {
                activityDefinition = (ActivityDefinition) planDefinitionAction.getDefinition().getResource();

            } else {
                activityDefinition =
                    this.fhirDataProvider.getFhirClient().read().resource( ActivityDefinition.class ).withId( planDefinitionAction.getDefinition().getReferenceElement() ).execute();
            }
            if ( activityDefinition == null ) {
                throw new FHIRException( "ActivityDefinition " + planDefinitionAction.getDefinition().getReferenceElement() + " cannot be found" );
            }

            if ( !requestGroupAction.hasTitle() && activityDefinition.hasTitle() ) {
                requestGroupAction.setTitle( activityDefinition.getTitle() );
            }
            if ( !requestGroupAction.hasDescription() && activityDefinition.hasDescription() ) {
                    requestGroupAction.setDescription( activityDefinition.getDescription() );
            }

            try {
                ActivityDefinitionProcessor activityDefinitionProcessor = new ActivityDefinitionProcessor(
                    this.fhirDataProvider, activityDefinition, patientId, encounterId, practitionerId, organizationId, null, null, null, null, null
                );
                resource = (Resource) activityDefinitionProcessor.getResult();
            } catch ( FHIRException e ) {
                throw new RuntimeException( "Error applying ActivityDefinition " + e.getMessage() );
            }
        } else if (definitionType.equals( ResourceType.StructureMap.name() )){
            try {
                Resource result=null;
                String structureMapId = planDefinitionAction.getDefinition().getReferenceElement().getIdPart();
                StructureMap structuredMap =
                    fhirDataProvider.getFhirClient().read().resource( StructureMap.class ).withId( structureMapId ).execute();
                if ( structuredMap==null ){
                    throw new FHIRException( "StructureMap "+structureMapId+" can not be found" );
                }
                resource = (Resource) structureMapTransformServer.doTransform( structuredMap, planDefinition, result  );
            }catch ( FHIRException e ) {
                throw new RuntimeException( "Error applying StructureMap " + e.getMessage() );
            }
        } else if( definitionType.equals( ResourceType.PlanDefinition )) {
            try {
                PlanDefinitionProcessor planDefinitionProcessor =
                    new PlanDefinitionProcessor( fhirDataProvider, (IdType) planDefinitionAction.getDefinition().getReferenceElement(),
                        patientId, encounterId, practitionerId, organizationId, userType, userLanguage, userTaskContext, setting, settingContext  );
                resource = planDefinitionProcessor.getCarePlan();
            }catch ( FHIRException e ) {
                throw new RuntimeException( "Error applying StructureMap " + e.getMessage() );
            }
        } else {
            throw new RuntimeException( "Error processing PlanDefinition, unknown definition " + planDefinitionAction.getDefinition().getReference() );
        }
        if (resource==null){
            throw new FHIRException( "unable to create resource using "+planDefinitionAction.getDefinition().toString() );
        }
        if ( ! resource.hasId() ){ resource.setId( UUID.randomUUID().toString() );}
        resource.setId( (resource.getId().startsWith( "#" )?resource.getId():"#"+resource.getId()));
        requestGroupAction.setResourceTarget( resource );
        requestGroupAction.setResource( new Reference().setReference( resource.getId() ) );
        carePlan.addContained( resource );
    }

    private Resource resolveContained(DomainResource resource, String id) {
        for ( Resource res : resource.getContained()) {
            if (res.hasIdElement()) {
                if (res.getIdElement().getIdPart().equals(id)) {
                    return res;
                }
            }
        }
        throw new RuntimeException( String.format("Resource %s does not contain resource with id %s", resource.fhirType(), id));
    }

    public CarePlan getCarePlan() {
        return carePlan;
    }

}

