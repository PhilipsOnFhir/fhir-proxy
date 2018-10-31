package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.context.service;

import ca.uhn.fhir.context.FhirContext;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.NotImplementedException;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.service.FhirServer;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.service.IFhirServer;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.CapabilityStatement;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseOperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public class ContextSession implements IFhirServer {

    private final String sessionId;
    private final FhirServer fhirServer;
    private Map<String,Map<String,FhirAction>> resourceMapMap = new TreeMap<>();

    public ContextSession(String id, FhirServer fhirServer ) {
        this.fhirServer = fhirServer;
        this.sessionId = id;
    }

    private Map<String, FhirAction> getResourceMap( String resourceType ){
        Map<String,FhirAction> resourceMap = this.resourceMapMap.get(resourceType);
        if ( resourceMap==null ){
            resourceMap = new TreeMap<>();
            this.resourceMapMap.put(resourceType, resourceMap);
        }
        return resourceMap;
    }


    @Override
    public CapabilityStatement getCapabilityStatement() throws NotImplementedException {
        throw new NotImplementedException();
    }

    @Override
    public IBaseResource searchResource(String resourceType, Map<String, String> queryParams) throws NotImplementedException {

        throw new NotImplementedException();
    }

    @Override
    public IBaseResource readResource(String resourceType, String id, Map<String, String> queryParams) throws FHIRException, NotImplementedException {
        IBaseResource iBaseResource = fhirServer.readResource(resourceType,id,queryParams);

        Map<String,FhirAction> resourceMap = this.resourceMapMap.get(resourceType);
        FhirAction fhirAction = resourceMap.get(id);

        return fhirAction.process( iBaseResource );
    }

    @Override
    public IBaseResource getResourceOperation(String resourceType, String operationName, Map<String, String> queryParams) throws FHIRException, NotImplementedException {
        throw new NotImplementedException();
    }

    @Override
    public IBaseResource getResourceOperation(String resourceType, String id, String params, Map<String, String> queryParams) throws FHIRException, NotImplementedException {
        throw new NotImplementedException();
    }

    @Override
    public Bundle loadPage(Bundle resultBundle) throws FHIRException, NotImplementedException {
        throw new NotImplementedException();
    }

    @Override
    public IBaseOperationOutcome updateResource(IBaseResource iBaseResource) throws FHIRException, NotImplementedException {
        Resource resource = (Resource)iBaseResource;
        Map<String,FhirAction> resourceMap = this.resourceMapMap.get(resource.fhirType());
        FhirAction fhirAction = new FhirAction( FhirAction.FhirActionAction.UPDATE, resource );
        resourceMap.put(resource.getId(), fhirAction);
        return null;
    }

    @Override
    public IBaseOperationOutcome postResourceOperation(IBaseResource iBaseResource) throws FHIRException, NotImplementedException {
        Resource resource = (Resource)iBaseResource;
        resource.setId( UUID.randomUUID().toString() );
        Map<String,FhirAction> resourceMap = this.resourceMapMap.get(resource.fhirType());
        FhirAction fhirAction = new FhirAction( FhirAction.FhirActionAction.CREATE, resource );
        resourceMap.put(resource.getId(), fhirAction);

        return (IBaseOperationOutcome) resource;
    }

    @Override
    public IBaseResource postResourceOperation(String resourceType, String id, IBaseResource parseResource, String params, Map<String, String> queryParams) throws FHIRException, NotImplementedException {
        throw new NotImplementedException();
    }

    @Override
    public FhirContext getCtx() throws NotImplementedException {
        throw new NotImplementedException();
    }

    @Override
    public String getUrl() throws NotImplementedException {
        throw new NotImplementedException();
    }

    public String getSessionId() {
        return sessionId;
    }
}
