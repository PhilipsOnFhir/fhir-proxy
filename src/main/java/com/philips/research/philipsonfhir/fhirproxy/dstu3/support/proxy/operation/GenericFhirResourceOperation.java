package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.operation;

import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.service.FhirServer;

import java.util.Map;

public class GenericFhirResourceOperation extends FhirResourceOperation {

    public GenericFhirResourceOperation(String baseResource, String operationName) {
        super( baseResource, operationName );
    }

    @Override
    public FhirOperationCall createOperationCall(FhirServer fhirServer, Map<String, String> queryparams) {
        return new GenericFhirResourceOperationCall( resourceType, operationName, fhirServer, queryparams );
    }
}
