package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.operation;

import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.service.FhirServer;

import java.util.Map;

public abstract class FhirResourceOperation {
    protected final String resourceType;
    protected final String operationName;

    public FhirResourceOperation(String resourceType, String operationName) {
        this.resourceType = resourceType;
        this.operationName = operationName;
    }

    public abstract FhirOperationCall createOperationCall(FhirServer fhirServer, Map<String, String> queryparams);
}

