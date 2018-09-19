package com.philips.research.philipsonfhir.fhirproxy.support.proxy.service;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.CapabilityStatement;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseOperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;

import java.util.Map;

public interface IFhirServer {
    CapabilityStatement getCapabilityStatement();

    IBaseResource searchResource(String resourceType, Map<String, String> queryParams);

    IBaseResource getResource(String resourceType, String id, Map<String, String> queryParams) throws FHIRException;

    IBaseResource getResourceOperation(String resourceType, String operationName, Map<String, String> queryParams) throws FHIRException;

    IBaseResource getResource(String resourceType, String id, String params, Map<String, String> queryParams) throws FHIRException;

    Bundle loadPage(Bundle resultBundle) throws FHIRException;

        IBaseOperationOutcome putResource(IBaseResource iBaseResource) throws FHIRException;

    IBaseOperationOutcome postResource(IBaseResource iBaseResource) throws FHIRException;

    IBaseResource postResource(
            String resourceType,
            String id,
            IBaseResource parseResource,
            String params,
            Map<String, String> queryParams
    ) throws FHIRException;

    FhirContext getCtx();

}
