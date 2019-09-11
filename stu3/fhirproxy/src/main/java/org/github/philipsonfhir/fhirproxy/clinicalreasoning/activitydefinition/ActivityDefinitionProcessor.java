package org.github.philipsonfhir.fhirproxy.clinicalreasoning.activitydefinition;

import ca.uhn.fhir.context.FhirContext;
import org.github.philipsonfhir.fhirproxy.clinicalreasoning.common.CqlExecutionProvider;
import org.github.philipsonfhir.fhirproxy.clinicalreasoning.common.FhirValueSetter;
import org.github.philipsonfhir.fhirproxy.clinicalreasoning.common.MyWorkerContext;
import org.github.philipsonfhir.fhirproxy.clinicalreasoning.structuremap.StructureMapTransformWorker;
import org.github.philipsonfhir.fhirproxy.common.FhirProxyException;
import org.github.philipsonfhir.fhirproxy.common.FhirProxyNotImplementedException;
import org.hl7.fhir.dstu3.hapi.validation.DefaultProfileValidationSupport;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.dstu3.utils.FHIRPathEngine;
import org.hl7.fhir.dstu3.utils.StructureMapUtilities;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.cql.data.fhir.BaseFhirDataProvider;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by Bryn on 1/16/2017.
 */
public class ActivityDefinitionProcessor {
    private static StructureMapUtilities structureMapUtilities;
    //    private static final Logger logger = Logger.getLogger( ActivityDefinitionProcessor.class.getName());
    private Logger logger = Logger.getLogger( this.getClass().getName());
    private final ActivityDefinition activityDefinition;
    private BaseFhirDataProvider fhirDataProvider;
    private static StructureMapTransformWorker transformServer = null;
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
        if ( structureMapUtilities==null ) {
            structureMapUtilities = new StructureMapUtilities( new MyWorkerContext( ourCtx, new DefaultProfileValidationSupport() ) );
        }
        if ( transformServer==null ) {
            transformServer = new StructureMapTransformWorker();
        }
    }
    ///////////////////////////////////////

    public ActivityDefinitionProcessor(BaseFhirDataProvider fhirDataProvider, ActivityDefinition activityDefinition, String patientId) throws FHIRException, FhirProxyNotImplementedException {
        initialize();

        this.activityDefinition = activityDefinition;
        this.fhirDataProvider =fhirDataProvider;
        process( patientId, null, null, null, null, null, null, null, null );
    }

    public ActivityDefinitionProcessor(BaseFhirDataProvider baseFhirDataProvider, ActivityDefinition activityDefinition, String patientId, String encounterId, String practitionerId, String organizatonId) throws FhirProxyNotImplementedException {
        this.activityDefinition = activityDefinition;
        this.fhirDataProvider =fhirDataProvider;
        process( patientId, encounterId, practitionerId, organizatonId, null, null, null, null, null );
    }


    public ActivityDefinitionProcessor(BaseFhirDataProvider fhirDataProvider, ActivityDefinition activityDefinition, String patientId, String encounterId, String practitionerId, String organizationId, String userType, String userLanguage, String userTaskContext, String setting, String settingContext) throws FHIRException, FhirProxyNotImplementedException {
        initialize();
        this.activityDefinition = activityDefinition;
        this.fhirDataProvider =fhirDataProvider;
        process( patientId, encounterId, practitionerId, organizationId, userType, userLanguage, userTaskContext, setting, settingContext);
    }


    public ActivityDefinitionProcessor(BaseFhirDataProvider fhirDataProvider
        , IdType activityDefinitionId
        , String patientId, String encounterId, String practitionerId, String organizationId, String userType, String userLanguage, String userTaskContext, String setting, String settingContext
    ) throws FHIRException, FhirProxyNotImplementedException {
        initialize();
        this.fhirDataProvider = fhirDataProvider;
        this.activityDefinition = fhirDataProvider.getFhirClient().read()
            .resource( ActivityDefinition.class ).withId( activityDefinitionId ).execute();

        if ( activityDefinition == null ) {
            throw new IllegalArgumentException( "Couldn't find ActivityDefinition "+activityDefinitionId  );
        }
        process( patientId, encounterId, practitionerId, organizationId, userType, userLanguage, userTaskContext, setting, settingContext );
    }

    private void process(String patientId, String encounterId, String practitionerId, String organizationId, String userType, String userLanguage, String userTaskContext, String setting, String settingContext
    ) throws FHIRException, FhirProxyNotImplementedException {
        resolveActivityDefinition(activityDefinition, patientId, practitionerId, organizationId, encounterId);
    }

    // For library use
    public Resource resolveActivityDefinition(ActivityDefinition activityDefinition, String patientId,
                                              String practitionerId, String organizationId, String encounterId)
        throws FHIRException, FhirProxyNotImplementedException {

        result = null;

        Parameters parameters = new Parameters()
            .addParameter( new Parameters.ParametersParameterComponent(  )
                .setName( "source" )
                .setResource( activityDefinition )
            );
        if ( patientId!=null ) {
            parameters.addParameter( new Parameters.ParametersParameterComponent()
                .setName( "subject" )
                .setValue( new Reference( patientId ) )
            );
        }
        if( encounterId!=null ) {
            parameters.addParameter( new Parameters.ParametersParameterComponent()
                .setName( "encounter" )
                .setValue( new Reference( encounterId ) )
            );
        }
        if( practitionerId!=null ) {
            parameters.addParameter( new Parameters.ParametersParameterComponent()
                .setName( "practitioner" )
                .setValue( new Reference( practitionerId ) )
            );
        }
        if( organizationId!=null ) {
            parameters.addParameter( new Parameters.ParametersParameterComponent()
                .setName( "organization" )
                .setValue( new Reference( organizationId ) )
            );
        }

        if ( !activityDefinition.hasKind() ){
            throw new FHIRException( "ActivityDefinition.kind is not set" );
        }

        InputStream is = getClass().getClassLoader().getResourceAsStream( "dstu3/"+activityDefinition.getKind().toCode()+".fhirmap" );
        if ( is != null ) {
            BufferedReader reader = new BufferedReader( new InputStreamReader( is ) );
            String mapStr = reader.lines().collect( Collectors.joining( System.lineSeparator() ) );
            StructureMap structureMap = (StructureMap) structureMapUtilities.parse( mapStr ).setId("AdToResource");
            IBaseResource transformResult = transformServer.doTransform( structureMap, parameters, null );
            if (!( transformResult instanceof Resource)){
                throw new FHIRException( "Error, AD did not result in a resource" );
            }
            result = (Resource) transformResult;
        }
        else{
            throw new FhirProxyNotImplementedException( "Mapping from Activitydefinition to "+activityDefinition.getKind().toCode()+" is undefined." );
        }

        if ( activityDefinition.hasTransform() ){
            Reference transformReference = activityDefinition.getTransform();
            final String rn = "StructuredMap/";
            String strMapId = transformReference.getReference().substring( rn.length() );

            StructureMap structureMap = null;
            if ( strMapId.startsWith( "#" )){
                Optional<StructureMap> opt = activityDefinition.getContained().stream()
                    .filter( resource -> resource.getId().equals( strMapId.substring( 1 ) ) )
                    .filter( resource -> resource instanceof StructureMap)
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
                        FhirValueSetter.setProperty( result, dynamicValue.getPath(), (Base) dynValResult );
                    }
                }
            }
        }
        return result;
    }

    public Resource getResult() {
        return result;
    }


}
