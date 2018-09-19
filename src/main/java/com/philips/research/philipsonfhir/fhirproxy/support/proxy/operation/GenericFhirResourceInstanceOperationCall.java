package com.philips.research.philipsonfhir.fhirproxy.support.proxy.operation;

import com.philips.research.philipsonfhir.fhirproxy.support.proxy.service.IFhirServer;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;

import java.util.Map;

public class GenericFhirResourceInstanceOperationCall implements FhirOperationCall {
    private final Map<String, String> queryParams;
    private final String resourceType;
    private final IFhirServer fhirServer;
    private final String resourceId;
    private final String operationName;
    private IBaseResource result = null;

    public GenericFhirResourceInstanceOperationCall(IFhirServer fhirServer, String resourceType, String resourceId, String operationName, Map<String, String> queryParams) {
        this.fhirServer = fhirServer;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.queryParams = queryParams;
        this.operationName = operationName;
    }

    @Override
    public IBaseResource getResult() throws FHIRException {
        if ( this.result == null ) {
            this.result = fhirServer.getResource( resourceType, resourceId, operationName, queryParams );
        }
        return this.result;
    }

    @Override
    public String getDescription() {
        return "processing";
    }

    @Override
    public Map<String, OperationOutcome> getErrors() {
        return null;
    }
}
