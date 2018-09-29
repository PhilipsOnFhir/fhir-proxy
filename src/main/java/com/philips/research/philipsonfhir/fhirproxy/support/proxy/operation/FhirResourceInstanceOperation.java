package com.philips.research.philipsonfhir.fhirproxy.support.proxy.operation;

import com.philips.research.philipsonfhir.fhirproxy.support.NotImplementedException;
import com.philips.research.philipsonfhir.fhirproxy.support.proxy.service.FhirServer;
import lombok.Getter;
import org.hl7.fhir.instance.model.api.IBaseResource;

import java.util.Map;

@Getter
public abstract class FhirResourceInstanceOperation {


    protected final String resourceType;
    protected final String operationName;

    public FhirResourceInstanceOperation(String resourceType, String operationName) {
        this.resourceType = resourceType;
        this.operationName = operationName;
    }

    public FhirOperationCall createGetOperationCall(FhirServer fhirServer, String resourceId, Map<String, String> queryparams) throws NotImplementedException {
        throw new NotImplementedException();
    }

    public FhirOperationCall createPostOperationCall(FhirServer fhirServer, String resourceId, IBaseResource parseResource, Map<String, String> queryParams) throws NotImplementedException {
        throw new NotImplementedException();
    }
}
