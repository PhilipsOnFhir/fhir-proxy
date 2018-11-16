package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.clinicalreasoning;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.NotImplementedException;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.clinicalreasoning.processor.ParametersUtil;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.clinicalreasoning.processor.QuestionnairePopulateProcessor;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.operation.FhirOperationCall;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.operation.FhirResourceInstanceOperation;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.service.FhirServer;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.opencds.cqf.cql.data.fhir.BaseFhirDataProvider;
import org.opencds.cqf.cql.data.fhir.FhirDataProviderStu3;
import org.opencds.cqf.cql.terminology.fhir.FhirTerminologyProvider;

import java.util.Map;

public class QuestionnairePopulateOperation extends FhirResourceInstanceOperation {
    private final String fhirServerUrl;

    public QuestionnairePopulateOperation(String fhirServerUrl) {
        super(ResourceType.Questionnaire.name(), "$populate");
        this.fhirServerUrl = fhirServerUrl;
    }

    public FhirOperationCall createPostOperationCall(FhirServer fhirServer, String resourceId, IBaseResource parseResource, Map<String, String> queryParams) throws NotImplementedException {
        return new FhirOperationCall() {
            @Override
            public IBaseResource getResult() throws FHIRException, NotImplementedException {
                BaseFhirDataProvider baseFhirDataProvider = new FhirDataProviderStu3().setEndpoint(fhirServerUrl);
                Parameters parameters = new Parameters();
                populateParameter(parameters,"identifier", false, Enumerations.FHIRAllTypes.URI, queryParams);
                populateParameter(parameters,"questionnaire", false, Enumerations.FHIRAllTypes.URI, queryParams);
                populateParameter(parameters,"questionnaireRef", false, Enumerations.FHIRAllTypes.REFERENCE, queryParams);
                populateParameter(parameters,"subject", true, Enumerations.FHIRAllTypes.REFERENCE, queryParams);
                populateParameter(parameters,"content", false, Enumerations.FHIRAllTypes.REFERENCE, queryParams);
                populateParameter(parameters,"local", false, Enumerations.FHIRAllTypes.BOOLEAN, queryParams);

                IBaseResource iBaseResource = fhirServer.doGet(resourceType,resourceId,null);
                if ( iBaseResource == null || !(iBaseResource instanceof  Questionnaire )){
                    throw new FHIRException("Questionnaire "+ resourceId + " cannot be found");
                }
                QuestionnairePopulateProcessor questionnairePopulateProcessor = new QuestionnairePopulateProcessor(baseFhirDataProvider, (Questionnaire)iBaseResource, parameters);
                return questionnairePopulateProcessor.getQuestionnaireResponse();
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

    @Override
    public FhirOperationCall createGetOperationCall(FhirServer fhirServer, Map<String, String> queryparams) throws NotImplementedException {
        throw new NotImplementedException();
    }

    @Override
    public FhirOperationCall createGetOperationCall(FhirServer fhirServer, String resourceId, Map<String, String> queryParams) {
        return new FhirOperationCall() {
            @Override
            public IBaseResource getResult() throws FHIRException, NotImplementedException {
                BaseFhirDataProvider baseFhirDataProvider = new FhirDataProviderStu3().setEndpoint(fhirServerUrl);
                Parameters parameters = new Parameters();
                populateParameter(parameters,"identifier", false, Enumerations.FHIRAllTypes.URI, queryParams);
//                populateParameter(parameters,"questionnaire", false, Enumerations.FHIRAllTypes.URI, queryParams);
                populateParameter(parameters,"questionnaireRef", false, Enumerations.FHIRAllTypes.REFERENCE, queryParams);
                populateParameter(parameters,"subject", true, Enumerations.FHIRAllTypes.REFERENCE, queryParams);
                populateParameter(parameters,"content", false, Enumerations.FHIRAllTypes.REFERENCE, queryParams);
                populateParameter(parameters,"local", false, Enumerations.FHIRAllTypes.BOOLEAN, queryParams);

                IBaseResource iBaseResource = fhirServer.doGet(resourceType,resourceId,null);
                if ( iBaseResource == null || !(iBaseResource instanceof  Questionnaire )){
                    throw new FHIRException("Questionnaire "+ resourceId + " cannot be found");
                }
                QuestionnairePopulateProcessor questionnairePopulateProcessor = new QuestionnairePopulateProcessor(baseFhirDataProvider, (Questionnaire)iBaseResource, parameters);
                return questionnairePopulateProcessor.getQuestionnaireResponse();
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

    @Override
    public FhirOperationCall createPostOperationCall(FhirServer fhirServer, IBaseResource parseResource, Map<String, String> queryParams) throws NotImplementedException {
        return new FhirOperationCall() {
            @Override
            public IBaseResource getResult() throws FHIRException, NotImplementedException {
                BaseFhirDataProvider baseFhirDataProvider = new FhirDataProviderStu3().setEndpoint(fhirServerUrl);
                FhirContext fhirContext = baseFhirDataProvider.getFhirContext();
                fhirContext.getRestfulClientFactory().setServerValidationMode(ServerValidationModeEnum.NEVER);
                baseFhirDataProvider.setFhirContext(fhirContext);
                baseFhirDataProvider.getFhirClient().setEncoding(EncodingEnum.JSON);

                FhirTerminologyProvider terminologyProvider = new FhirTerminologyProvider();
                terminologyProvider.setEndpoint(fhirServerUrl, false );

                baseFhirDataProvider.setTerminologyProvider(terminologyProvider);

                Parameters parameters = (Parameters) parseResource;
//                ParametersUtil parameters = new ParametersUtil();
//                populateParameter(parameters,"identifier", false, Enumerations.FHIRAllTypes.URI, queryParams);
//                populateParameter(parameters,"questionnaire", false, Enumerations.FHIRAllTypes.URI, queryParams);
////                populateParameter(parameters,"questionnaireRef", false, Enumerations.FHIRAllTypes.REFERENCE, queryParams);
//                populateParameter(parameters,"subject", true, Enumerations.FHIRAllTypes.REFERENCE, queryParams);
//                populateParameter(parameters,"content", false, Enumerations.FHIRAllTypes.REFERENCE, queryParams);
//                populateParameter(parameters,"local", false, Enumerations.FHIRAllTypes.BOOLEAN, queryParams);

//                IBaseResource iBaseResource = fhirServer.doGet(resourceType,resourceId,null);
//                if ( iBaseResource == null || !(iBaseResource instanceof  Questionnaire )){
//                    throw new FHIRException("Questionnaire "+ resourceId + " cannot be found");
//                }
                Questionnaire questionnaire = (Questionnaire) ParametersUtil.getParameter(parameters,"questionnaire").getResource();
                QuestionnairePopulateProcessor questionnairePopulateProcessor = new QuestionnairePopulateProcessor(baseFhirDataProvider, questionnaire, parameters);
                return questionnairePopulateProcessor.getQuestionnaireResponse();
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

}