package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.bulkdata.operations;

import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.service.IFhirServer;

import java.util.Map;
import java.util.logging.Logger;

public class ExportAllFhirOperation {
    private final String operationName;
    private Logger logger = Logger.getLogger( this.getClass().getName() );

    public ExportAllFhirOperation(String operationName) {
        this.operationName = operationName;
    }

    public ExportAllFhirOperationCall createOperationCall(IFhirServer fhirServer, Map<String, String> queryParams) {
        return new ExportAllFhirOperationCall( fhirServer, queryParams );
    }
}

