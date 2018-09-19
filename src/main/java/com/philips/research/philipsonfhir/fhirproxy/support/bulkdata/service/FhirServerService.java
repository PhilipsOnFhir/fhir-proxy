package com.philips.research.philipsonfhir.fhirproxy.support.bulkdata.service;

import ca.uhn.fhir.context.FhirContext;
import com.philips.research.philipsonfhir.fhirproxy.support.bulkdata.fhir.FhirServerBulkdata;
import com.philips.research.philipsonfhir.fhirproxy.support.proxy.service.FhirServer;
import com.philips.research.philipsonfhir.fhirproxy.support.proxy.service.IFhirServer;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.CapabilityStatement;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseOperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
public class FhirServerService implements IFhirServer {
    IFhirServer fhirServer;

    public FhirServerService() {
//        super("http://hapi.fhir.org/baseDstu3");
        FhirServerBulkdata fhirServerBulkdata = new FhirServerBulkdata( new FhirServer("http://localhost:9500/baseDstu3"));
        fhirServer = fhirServerBulkdata;
    }

    @Override
    public CapabilityStatement getCapabilityStatement() {
        return fhirServer.getCapabilityStatement();
    }

    @Override
    public IBaseResource searchResource(String resourceType, Map<String, String> queryParams) {
        return fhirServer.searchResource( resourceType, queryParams );
    }

    @Override
    public IBaseResource getResource(String resourceType, String id, Map<String, String> queryParams) throws FHIRException {
        return fhirServer.getResource( resourceType, id, queryParams );
    }

    @Override
    public IBaseResource getResourceOperation(String resourceType, String operationName, Map<String, String> queryParams) throws FHIRException {
        return fhirServer.getResourceOperation( resourceType, operationName, queryParams );
    }

    @Override
    public IBaseResource getResource(String resourceType, String id, String params, Map<String, String> queryParams) throws FHIRException {
        return fhirServer.getResource( resourceType, id, params, queryParams );
    }

    @Override
    public Bundle loadPage(Bundle resultBundle) throws FHIRException {
        return fhirServer.loadPage( resultBundle );
    }

    @Override
    public IBaseOperationOutcome putResource(IBaseResource iBaseResource) throws FHIRException {
        return fhirServer.putResource( iBaseResource );
    }

    @Override
    public IBaseOperationOutcome postResource(IBaseResource iBaseResource) throws FHIRException {
        return fhirServer.postResource( iBaseResource );
    }

    @Override
    public IBaseResource postResource(String resourceType, String id, IBaseResource parseResource, String params, Map<String, String> queryParams) throws FHIRException {
        return fhirServer.postResource( resourceType, id, parseResource, params, queryParams );
    }

    @Override
    public FhirContext getCtx() {
        return fhirServer.getCtx();
    }
}
