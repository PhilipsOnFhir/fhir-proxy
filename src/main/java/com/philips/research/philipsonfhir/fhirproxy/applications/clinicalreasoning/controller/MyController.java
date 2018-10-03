package com.philips.research.philipsonfhir.fhirproxy.applications.clinicalreasoning.controller;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import com.philips.research.philipsonfhir.fhirproxy.support.NotImplementedException;
import com.philips.research.philipsonfhir.fhirproxy.support.clinicalreasoning.ActivityDefinitionProcessor;
import com.philips.research.philipsonfhir.fhirproxy.support.clinicalreasoning.MeasureEvaluationProcessor;
import com.philips.research.philipsonfhir.fhirproxy.support.clinicalreasoning.PlanDefinitionProcessor;
import com.philips.research.philipsonfhir.fhirproxy.support.clinicalreasoning.StructureMapTransformServer;
import com.philips.research.philipsonfhir.fhirproxy.support.clinicalreasoning.helpers.DateHelper;
import com.philips.research.philipsonfhir.fhirproxy.support.proxy.controller.SampleFhirGateway;
import com.philips.research.philipsonfhir.fhirproxy.support.proxy.operation.FhirOperationCall;
import com.philips.research.philipsonfhir.fhirproxy.support.proxy.operation.FhirResourceInstanceOperation;
import com.philips.research.philipsonfhir.fhirproxy.support.proxy.service.FhirServer;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.cql.data.fhir.BaseFhirDataProvider;
import org.opencds.cqf.cql.data.fhir.FhirDataProviderStu3;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
public class MyController extends SampleFhirGateway {
    private static final String url = "http://localhost:9500/baseDstu3";

    public MyController() throws FHIRException, NotImplementedException {
        super( url );
        FhirContext ourCtx = this.fhirServer.getCtx();
        IGenericClient client = this.fhirServer.getCtx().newRestfulGenericClient( url );

        addMeasureEvaluate( ourCtx, client );
        addPlanDefinitionApply();
        addActivityDefinitionApply();
        addStructureMapTransform( ourCtx, client );
    }

    private void addMeasureEvaluate(FhirContext ourCtx, IGenericClient client) throws FHIRException {
        FhirResourceInstanceOperation measureEvaluation = new FhirResourceInstanceOperation( ResourceType.Measure.name(), "$evaluate-measure" ) {
            @Override
            public FhirOperationCall createGetOperationCall(FhirServer fhirServer, String resourceId, Map<String, String> queryParams) {
                return new FhirOperationCall() {
                    @Override
                    public IBaseResource getResult() throws FHIRException, NotImplementedException {
                        String periodStart = queryParams.get("periodStart");
                        String periodEnd   = queryParams.get( "periodEnd" );
                        String measureRef  = queryParams.get( "measure" );
                        String reportType  = queryParams.get( "reportType" );
                        String patient     = queryParams.get( "patient" );
                        String practitioner = queryParams.get("practitioner");
                        String lastReceivedOn = queryParams.get( "lastReceievedOn" );

                        String resId = ( measureRef!=null? measureRef: resourceId );
                        Measure measure = client.read().resource( Measure.class ).withId( resourceId ).execute();

                        // resolve the measurement period
//                        Interval measurementPeriod =
//                            new org.opencds.cqf.cql.runtime.Interval(
//                                DateHelper.resolveRequestDate(periodStart, true), true,
//                                DateHelper.resolveRequestDate(periodEnd, false), true
//                            );

//                        Period period = new Period();
//                        period.setStart( DateHelper.resolveRequestDate(periodStart, true) );
//                        period.setEnd( DateHelper.resolveRequestDate(periodEnd, false) );

                        MeasureEvaluationProcessor measureEvaluationProcesso =
                            new MeasureEvaluationProcessor( client, null,
                                DateHelper.resolveRequestDate(periodStart, true),
                                DateHelper.resolveRequestDate( periodEnd,false ));

                        return measureEvaluationProcesso.getResult();
                    }

                    @Override
                    public String getDescription() {
                        return null;
                    }

                    @Override
                    public Map<String, OperationOutcome> getErrors() {
                        return null;
                    }


                };

            }
        };
        this.fhirServer.getFhirOperationRepository().registerOperation(  measureEvaluation  );
    }

    private void addStructureMapTransform(FhirContext ourCtx, IGenericClient client) throws FHIRException {
        FhirResourceInstanceOperation structuredMapTransform = new FhirResourceInstanceOperation( ResourceType.StructureMap.name(), "$transform" ) {
            @Override
            public FhirOperationCall createPostOperationCall(FhirServer fhirServer, String resourceId, IBaseResource parameters, Map<String, String> queryParams) {
                return new FhirOperationCall() {
                    @Override
                    public IBaseResource getResult() throws FHIRException, NotImplementedException {
                        BaseFhirDataProvider baseFhirDataProvider = new FhirDataProviderStu3().setEndpoint( url );
                        IdType idType = new IdType( ).setValue( resourceType+"/"+resourceId );

                        String source = queryParams.get( "source" );

                        Optional<Resource> optResource = ((Parameters) parameters).getParameter().stream()
                            .filter( parameter -> parameter.getName().equals( "content" ) )
                            .map( parameter -> parameter.getResource())
                            .findFirst();

                        if ( !optResource.isPresent()){
                            throw new FHIRException( "missind content parameter" );
                        }
                        Resource contentRsource = optResource.get();

                        StructureMapTransformServer structureMapTransformServer = new StructureMapTransformServer( client );

                        IBaseResource result = structureMapTransformServer.doTransform( resourceId, contentRsource, null );
                        return result;
                    }

                    @Override
                    public String getDescription() {
                        return null;
                    }

                    @Override
                    public Map<String, OperationOutcome> getErrors() {
                        return null;
                    }
                };
            }
        };
        this.fhirServer.getFhirOperationRepository().registerOperation(  structuredMapTransform  );
    }

    private void addActivityDefinitionApply() throws FHIRException, NotImplementedException {
        FhirResourceInstanceOperation activityDefinitionApply = new FhirResourceInstanceOperation( "PlanDefinition", "$apply" ) {
            @Override
            public FhirOperationCall createGetOperationCall(FhirServer fhirServer, String resourceId, Map<String, String> queryParams) {
                return new FhirOperationCall() {
                    @Override
                    public IBaseResource getResult() throws FHIRException, NotImplementedException {
                        BaseFhirDataProvider baseFhirDataProvider = new FhirDataProviderStu3().setEndpoint( url );
                        IdType idType = new IdType( ).setValue( resourceType+"/"+resourceId );

                        String patientId = queryParams.get( "patient" );
                        String encounterId = queryParams.get( "encounter");
                        String practitionerId = queryParams.get( "practitioner");
                        String organizationId = queryParams.get( "organization");
                        String userType       = queryParams.get( "userType");
                        String userLanguage   = queryParams.get("userLanguage");
                        String userTaskContext = queryParams.get( "userTaskComtext");
                        String setting         = queryParams.get("setting");
                        String settingContext  = queryParams.get("settingContext");

                        PlanDefinitionProcessor activityDefinitionProcessor = new PlanDefinitionProcessor(
                            baseFhirDataProvider, idType
                            , patientId, encounterId, practitionerId, organizationId, userType, userLanguage, userTaskContext, setting, settingContext );
                        return activityDefinitionProcessor.getCarePlan();
                    }

                    @Override
                    public String getDescription() {
                        return null;
                    }

                    @Override
                    public Map<String, OperationOutcome> getErrors() {
                        return null;
                    }
                };
            }
        };
        this.fhirServer.getFhirOperationRepository().registerOperation(  activityDefinitionApply  );
    }

    private void addPlanDefinitionApply() throws FHIRException, NotImplementedException {
        FhirResourceInstanceOperation activityDefinitionApply = new FhirResourceInstanceOperation( "ActivityDefinition", "$apply" ) {
            @Override
            public FhirOperationCall createGetOperationCall(FhirServer fhirServer, String resourceId, Map<String, String> queryParams) {
                return new FhirOperationCall() {
                    @Override
                    public IBaseResource getResult() throws FHIRException, NotImplementedException {
                        BaseFhirDataProvider baseFhirDataProvider = new FhirDataProviderStu3().setEndpoint( url );
                        IdType idType = new IdType( ).setValue( resourceType+"/"+resourceId );

                        String patientId = queryParams.get( "patient" );
                        String encounterId = queryParams.get( "encounter");
                        String practitionerId = queryParams.get( "practitioner");
                        String organizationId = queryParams.get( "organization");
                        String userType       = queryParams.get( "userType");
                        String userLanguage   = queryParams.get("userLanguage");
                        String userTaskContext = queryParams.get( "userTaskComtext");
                        String setting         = queryParams.get("setting");
                        String settingContext  = queryParams.get("settingContext");

                        ActivityDefinitionProcessor activityDefinitionProcessor = new ActivityDefinitionProcessor(
                            baseFhirDataProvider, idType
                            , patientId, encounterId, practitionerId, organizationId, userType, userLanguage, userTaskContext, setting, settingContext );
                        return activityDefinitionProcessor.getResult();
                    }

                    @Override
                    public String getDescription() {
                        return null;
                    }

                    @Override
                    public Map<String, OperationOutcome> getErrors() {
                        return null;
                    }
                };
            }
        };
        this.fhirServer.getFhirOperationRepository().registerOperation(  activityDefinitionApply  );
    }

}

