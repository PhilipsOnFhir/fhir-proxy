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

    IBaseResource doSearch(String resourceType, Map<String, String> queryParams) throws NotImplementedException;

    IBaseResource doGet(String resourceType, String id, Map<String, String> queryParams) throws FHIRException;

    IBaseResource doGet(String resourceType, String id, String params, Map<String, String> queryParams) throws FHIRException;

    Bundle loadPage(Bundle resultBundle) throws FHIRException;

    IBaseOperationOutcome updateResource(IBaseResource iBaseResource) throws FHIRException;

    IBaseResource doPost(String resourceType, String id, IBaseResource iBaseResource, Map<String, String> queryParams) throws FHIRException;

    IBaseResource doPost(
            String resourceType,
            String id,
            IBaseResource parseResource,
            String params,
            Map<String, String> queryParams
    ) throws FHIRException, NotImplementedException;

    FhirContext getCtx() throws NotImplementedException;

    String getUrl() throws NotImplementedException;
}
