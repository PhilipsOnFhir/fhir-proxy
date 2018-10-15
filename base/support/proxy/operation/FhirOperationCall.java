package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.operation;

import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.NotImplementedException;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;

import java.util.Map;

public interface FhirOperationCall {
    IBaseResource getResult() throws FHIRException, NotImplementedException;

    String getDescription();

    Map<String, OperationOutcome> getErrors();
}
