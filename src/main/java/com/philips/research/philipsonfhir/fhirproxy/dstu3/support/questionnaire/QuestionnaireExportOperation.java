package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.questionnaire;

import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.NotImplementedException;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.operation.FhirOperationCall;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.operation.FhirResourceInstanceOperation;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.service.FhirServer;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.dstu3.model.Parameters;
import org.hl7.fhir.dstu3.model.QuestionnaireResponse;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;

import java.util.Map;
import java.util.Optional;

public class QuestionnaireExportOperation extends FhirResourceInstanceOperation {

    private final String url;

    public QuestionnaireExportOperation(String url ) {
        super( "Questionnaire", "$export" );
        this.url = url;
    }

    @Override
    public FhirOperationCall createGetOperationCall(FhirServer fhirServer, Map<String, String> queryparams) throws NotImplementedException {
        throw new NotImplementedException();
    }

    @Override
    public FhirOperationCall createGetOperationCall(FhirServer fhirServer, String resourceId, Map<String, String> queryparams) throws NotImplementedException {
        throw new NotImplementedException();
    }

    @Override
    public FhirOperationCall createPostOperationCall(FhirServer fhirServer, String resourceId, IBaseResource parameters, Map<String, String> queryParams) {
        return new FhirOperationCall() {
            @Override
            public IBaseResource getResult() throws FHIRException, NotImplementedException {
//                fhirServer.getResource( ResourceType.Questionnaire.name(), resourceId, new TreeMap() );
                // extract transforms
                // call each transform
                // http://build.fhir.org/ig/HL7/sdc/Questionnaire-extract.html

                Optional<Resource> optResource = ((Parameters) parameters).getParameter().stream()
                    .filter( parameter -> parameter.getName().equals( "questionnaire-response" ) )
                    .map( parameter -> parameter.getResource() )
                    .findFirst();

                if ( !optResource.isPresent() ) {
                    throw new FHIRException( "missing content parameter" );
                }
                Resource resource = optResource.get();
                if ( !(resource instanceof QuestionnaireResponse) ){
                    throw new FHIRException( "questionnaire-response does not contain a QuestionaireReponse" );
                }

                throw new NotImplementedException("work in progress");
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
        return null;
    }
}
