package com.philips.research.philipsonfhir.fhirproxy.support.proxy.operation;

import com.philips.research.philipsonfhir.fhirproxy.support.proxy.service.FhirServer;

import java.util.Map;

public class GenericFhirResourceInstanceOperation extends FhirResourceInstanceOperation {
    public GenericFhirResourceInstanceOperation(String resourceType, String operationName) {
        super( resourceType, operationName );
    }

    @Override
    public FhirOperationCall createOperationCall(FhirServer fhirServer, String resourceId, Map<String, String> queryparams) {
        return new GenericFhirResourceInstanceOperationCall( fhirServer, resourceType, resourceId, operationName, queryparams );
    }
}
