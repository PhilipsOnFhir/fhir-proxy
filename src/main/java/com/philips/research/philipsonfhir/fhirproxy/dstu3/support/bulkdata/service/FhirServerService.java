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
    public CapabilityStatement getCapabilityStatement() throws NotImplementedException {
        return fhirServer.getCapabilityStatement();
    }

    @Override
    public IBaseResource doSearch(String resourceType, Map<String, String> queryParams) throws NotImplementedException {
        return fhirServer.doSearch( resourceType, queryParams );
    }

    @Override
    public IBaseResource doGet(String resourceType, String id, Map<String, String> queryParams) throws FHIRException, NotImplementedException {
        return fhirServer.doGet( resourceType, id, queryParams );
    }

    @Override
    public IBaseResource doGet(String resourceType, String id, String params, Map<String, String> queryParams) throws FHIRException, NotImplementedException {
        return fhirServer.doGet( resourceType, id, params, queryParams );
    }

    @Override
    public Bundle loadPage(Bundle resultBundle) throws FHIRException, NotImplementedException {
        return fhirServer.loadPage( resultBundle );
    }

    @Override
    public IBaseOperationOutcome updateResource(IBaseResource iBaseResource) throws FHIRException, NotImplementedException {
        return fhirServer.updateResource( iBaseResource );
    }

    @Override
    public IBaseResource doPost(String resourceType, String id, IBaseResource iBaseResource, Map<String, String> queryParams) throws FHIRException, NotImplementedException {
        return fhirServer.doPost(resourceType, id, iBaseResource, queryParams);
    }

    @Override
    public IBaseResource doPost(String resourceType, String id, IBaseResource parseResource, String params, Map<String, String> queryParams) throws FHIRException, NotImplementedException {
        return fhirServer.doPost( resourceType, id, parseResource, params, queryParams );
    }

    @Override
    public FhirContext getCtx() throws NotImplementedException {
        return fhirServer.getCtx();
    }

    @Override
    public String getUrl() {
        return this.url;
    }
}
