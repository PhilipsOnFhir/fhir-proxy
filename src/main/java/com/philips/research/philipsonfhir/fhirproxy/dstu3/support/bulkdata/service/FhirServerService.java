package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.bulkdata.service;

import ca.uhn.fhir.context.FhirContext;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.NotImplementedException;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.bulkdata.fhir.FhirServerBulkdata;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.service.FhirServer;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.service.IFhirServer;
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

    String url = "http://measure.eval.kanvix.com/cqf-ruler/baseDstu3";
//    String url = "http://localhost:9500/baseDstu3";

    public FhirServerService() {
        FhirServerBulkdata fhirServerBulkdata = new FhirServerBulkdata( new FhirServer( url ));

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
    public IBaseResource readResource(String resourceType, String id, Map<String, String> queryParams) throws FHIRException {
        return fhirServer.readResource( resourceType, id, queryParams );
    }

    @Override
    public IBaseResource getResourceOperation(String resourceType, String operationName, Map<String, String> queryParams) throws FHIRException {
        return fhirServer.getResourceOperation( resourceType, operationName, queryParams );
    }

    @Override
    public IBaseResource getResourceOperation(String resourceType, String id, String params, Map<String, String> queryParams) throws FHIRException, NotImplementedException {
        return fhirServer.getResourceOperation( resourceType, id, params, queryParams );
    }

    @Override
    public Bundle loadPage(Bundle resultBundle) throws FHIRException {
        return fhirServer.loadPage( resultBundle );
    }

    @Override
    public IBaseOperationOutcome updateResource(IBaseResource iBaseResource) throws FHIRException {
        return fhirServer.updateResource( iBaseResource );
    }

    @Override
    public IBaseOperationOutcome postResourceOperation(IBaseResource iBaseResource) throws FHIRException {
        return fhirServer.postResourceOperation( iBaseResource );
    }

    @Override
    public IBaseResource postResourceOperation(String resourceType, String id, IBaseResource parseResource, String params, Map<String, String> queryParams) throws FHIRException, NotImplementedException {
        return fhirServer.postResourceOperation( resourceType, id, parseResource, params, queryParams );
    }

    @Override
    public FhirContext getCtx() {
        return fhirServer.getCtx();
    }

    @Override
    public String getUrl() {
        return this.url;
    }
}
