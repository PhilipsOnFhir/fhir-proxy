package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.operation;

import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.NotImplementedException;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.service.FhirServer;
import org.hl7.fhir.instance.model.api.IBaseResource;

import java.util.Map;

public class GenericFhirResourceInstanceOperation extends FhirResourceInstanceOperation {
    public GenericFhirResourceInstanceOperation(String resourceType, String operationName) {
        super( resourceType, operationName );
    }

    @Override
    public FhirOperationCall createGetOperationCall(FhirServer fhirServer, String resourceId, Map<String, String> queryparams) {
        return new GenericFhirResourceInstanceOperationCall( fhirServer, resourceType, resourceId, operationName, queryparams );
    }

    @Override
    public FhirOperationCall createPostOperationCall(FhirServer fhirServer, IBaseResource parseResource, Map<String, String> queryParams) throws NotImplementedException {
        throw new NotImplementedException();
    }

}
