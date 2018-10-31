package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.operation;

import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.NotImplementedException;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.service.IFhirServer;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;

import java.util.HashMap;
import java.util.Map;

public class GenericFhirResourceOperationCall implements FhirOperationCall {
    private final IFhirServer fhirServer;
    private final Map<String, String> queryParams;
    private final String baseResource;
    private final String operationName;
    private IBaseResource result = null;

    public GenericFhirResourceOperationCall(String baseResource, String operationName, IFhirServer fhirServer, Map<String, String> queryParams) {
        this.fhirServer = fhirServer;
        this.queryParams = queryParams;
        this.baseResource = baseResource;
        this.operationName = operationName;
    }


    @Override
    public IBaseResource getResult() throws FHIRException, NotImplementedException {
        if ( result == null ) {
            this.result = this.fhirServer.getResourceOperation( baseResource, operationName, queryParams );
        }
        return this.result;
    }

    @Override
    public String getDescription() {
        return "default";
    }

    @Override
    public Map<String, OperationOutcome> getErrors() {
        return new HashMap<>();
    }
}
