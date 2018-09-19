package com.philips.research.philipsonfhir.fhirproxy.support.proxy.operation;

import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;

import java.util.Map;

public interface FhirOperationCall {
    IBaseResource getResult() throws FHIRException;

    String getDescription();

    Map<String, OperationOutcome> getErrors();
}
