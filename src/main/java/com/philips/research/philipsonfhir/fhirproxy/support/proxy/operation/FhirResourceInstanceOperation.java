package com.philips.research.philipsonfhir.fhirproxy.support.proxy.operation;

import com.philips.research.philipsonfhir.fhirproxy.support.proxy.service.FhirServer;
import lombok.Getter;

import java.util.Map;

@Getter
public abstract class FhirResourceInstanceOperation {


    protected final String resourceType;
    protected final String operationName;

    public FhirResourceInstanceOperation(String resourceType, String operationName) {
        this.resourceType = resourceType;
        this.operationName = operationName;
    }

    public abstract FhirOperationCall createOperationCall(FhirServer fhirServer, String resourceId, Map<String, String> queryparams);
}
