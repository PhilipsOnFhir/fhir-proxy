package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.service;

import ca.uhn.fhir.context.FhirContext;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.NotImplementedException;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.CapabilityStatement;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseOperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;

import java.util.Map;

public interface IFhirServer {
    CapabilityStatement getCapabilityStatement() throws NotImplementedException;

    IBaseResource searchResource(String resourceType, Map<String, String> queryParams) throws NotImplementedException;

    IBaseResource readResource(String resourceType, String id, Map<String, String> queryParams) throws FHIRException;

    IBaseResource getResourceOperation(String resourceType, String operationName, Map<String, String> queryParams) throws FHIRException, NotImplementedException;

    IBaseResource getResourceOperation(String resourceType, String id, String params, Map<String, String> queryParams) throws FHIRException, NotImplementedException;

    Bundle loadPage(Bundle resultBundle) throws FHIRException, NotImplementedException;

    IBaseOperationOutcome updateResource(IBaseResource iBaseResource) throws FHIRException, NotImplementedException;

    IBaseOperationOutcome postResourceOperation(IBaseResource iBaseResource) throws FHIRException, NotImplementedException;

    IBaseResource postResourceOperation(
            String resourceType,
            String id,
            IBaseResource parseResource,
            String params,
            Map<String, String> queryParams
    ) throws FHIRException, NotImplementedException;

    FhirContext getCtx() throws NotImplementedException;

    String getUrl() throws NotImplementedException;
}
