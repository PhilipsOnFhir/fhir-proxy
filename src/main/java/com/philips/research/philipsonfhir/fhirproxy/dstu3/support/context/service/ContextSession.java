package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.context.service;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.NotImplementedException;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.context.model.*;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.service.FhirServer;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.service.IFhirServer;
import org.hl7.fhir.dstu3.model.Bundle;
import org.hl7.fhir.dstu3.model.CapabilityStatement;
import org.hl7.fhir.dstu3.model.Resource;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseOperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.scheduling.annotation.Async;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class ContextSession implements IFhirServer {

    private final String sessionId;
    private final FhirServer fhirServer;
    private Map<String,Map<String,FhirAction>> resourceMapMap = new TreeMap<>();
    private Map<String, FhirCastSessionSubscribe>  fhirCastSubscriptions = new TreeMap<>();

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
    public IBaseResource doSearch(String resourceType, @NonNull Map<String, String> queryParams) throws NotImplementedException {
        if ( !queryParams.isEmpty() ){
            throw new NotImplementedException("Search parameters not supported");
        }
        Map<String, FhirAction> resourceMap = getResourceMap(resourceType);
        Bundle bundle = (Bundle) fhirServer.doSearch(resourceType, queryParams );
        Iterator<Bundle.BundleEntryComponent> iter = bundle.getEntry().iterator();
        while ( iter.hasNext() ){
            Bundle.BundleEntryComponent entryComponent = iter.next();
            FhirAction fhirAction = resourceMap.get(entryComponent.getResource().getIdElement().getIdPart() );
            if ( fhirAction != null ){
                switch (fhirAction.getAction() ){
                    case DELETE:
                        iter.remove();
                        break;
                    case CREATE:
                    case UPDATE:
                        entryComponent.setResource(fhirAction.getResource());
                        break;
                    default:
                        throw new NotImplementedException("TODO handle fhirActions");
                }
            }
        }
        resourceMap.values().stream()
                .filter( fhirAction -> fhirAction.getAction().equals(FhirAction.FhirActionAction.CREATE))
                .forEach(fhirAction -> bundle.addEntry(new Bundle.BundleEntryComponent().setResource(fhirAction.getResource())) );
        bundle.setTotal( bundle.getEntry().size() );
        return bundle;
    }

    @Override
    public IBaseResource doGet(String resourceType, String id, Map<String, String> queryParams) throws FHIRException, NotImplementedException {
        IBaseResource iBaseResource = fhirServer.doGet(resourceType,id,queryParams);

        Map<String,FhirAction> resourceMap = this.resourceMapMap.get(resourceType);
        FhirAction fhirAction = resourceMap.get(id);

        return fhirAction.process( iBaseResource );
    }


    @Override
    public IBaseResource doGet(String resourceType, String id, String params, Map<String, String> queryParams) throws FHIRException, NotImplementedException {
        throw new NotImplementedException();
    }

    @Override
    public Bundle loadPage(Bundle resultBundle) throws FHIRException, NotImplementedException {
        throw new NotImplementedException();
    }

    @Override
    public IBaseOperationOutcome updateResource(IBaseResource iBaseResource) throws FHIRException, NotImplementedException {
        throw new NotImplementedException();
    }

    public MethodOutcome updateResource2(Resource resourceIn) throws FHIRException, NotImplementedException {
        MethodOutcome methodOutcome = new MethodOutcome();
        boolean existsInFhirServer = true;
        try {
            Resource resourceFs = (Resource) fhirServer.doGet(resourceIn.getResourceType().name(), resourceIn.getIdElement().getIdPart(), null);
        } catch ( ResourceNotFoundException e ){
            existsInFhirServer = false;
        }

        Map<String,FhirAction> resourceMap = getResourceMap(resourceIn.fhirType());
        boolean existsInContext = resourceMap.containsKey(resourceIn.getIdElement().getIdPart());
        methodOutcome.setCreated( !existsInFhirServer && !existsInContext );

        FhirAction fhirAction = new FhirAction(
                (existsInFhirServer?FhirAction.FhirActionAction.UPDATE: FhirAction.FhirActionAction.CREATE),
                resourceIn
        );

        resourceMap.put(resourceIn.getIdElement().getIdPart(), fhirAction);

        return methodOutcome;
    }

    @Override
    public IBaseResource doPost(String resourceType, String id, IBaseResource iBaseResource, Map<String, String> queryParams) throws FHIRException, NotImplementedException {
            throw  new NotImplementedException();
    }

    public Resource postResourceOperation2(IBaseResource iBaseResource) throws FHIRException, NotImplementedException {
            Resource resource = (Resource)iBaseResource;
        resource.setId( UUID.randomUUID().toString() );
        Map<String,FhirAction> resourceMap = getResourceMap(resource.fhirType());
        FhirAction fhirAction = new FhirAction( FhirAction.FhirActionAction.CREATE, resource );
        resourceMap.put(resource.getIdElement().getIdPart(), fhirAction);

        return resource;
    }

    @Override
    public IBaseResource doPost(String resourceType, String id, IBaseResource parseResource, String params, Map<String, String> queryParams) throws FHIRException, NotImplementedException {
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

    public FhirServer getFhirServer() {
        return fhirServer;
    }

    public void addFhirCastSubscribe(FhirCastSessionSubscribe fhirCastSessionSubscribe) throws FHIRException {
        if ( !fhirCastSessionSubscribe.getHub_topic().equals(this.sessionId)){
            throw new FHIRException("SessionId does not correspond");
        }
        if ( fhirCastSessionSubscribe.getHub_mode().equals("subscribe")){
            this.fhirCastSubscriptions.put( fhirCastSessionSubscribe.getHub_callback(), fhirCastSessionSubscribe );
        } else if ( fhirCastSessionSubscribe.getHub_mode().equals("unsubscribe")) {
            this.fhirCastSubscriptions.remove( fhirCastSessionSubscribe.getHub_callback() );
        } else {
            throw new FHIRException("Unknown value for hub.mode "+fhirCastSessionSubscribe.getHub_mode());
        }
    }

    public void sendFhirContextChangedEvent(){
        for ( FhirCastSessionSubscribe fhirCastSessionSubscribe: this.fhirCastSubscriptions.values() ){
            if( fhirCastSessionSubscribe.getHub_events().contains("fhir-context-update")){
                FhirCastWorkflowEvent fhirCastWorkflowEvent = new FhirCastWorkflowEvent();

                TimeZone tz = TimeZone.getTimeZone("UTC");
                DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
                df.setTimeZone(tz);

                fhirCastWorkflowEvent.setTimestamp( df.format(new Date()));

                fhirCastWorkflowEvent.setId( this.sessionId + java.lang.System.currentTimeMillis());
                FhirCastWorkflowEventEvent fhirCastWorkflowEventEvent = new FhirCastWorkflowEventEvent();
                fhirCastWorkflowEventEvent.setHub_topic( fhirCastSessionSubscribe.getHub_topic() );
                fhirCastWorkflowEventEvent.setHub_event( "fhir-context-update" );

                fhirCastWorkflowEvent.setEvent(fhirCastWorkflowEventEvent);

            }
        }
    }


    TestRestTemplate restTemplate = new TestRestTemplate();

    @Async
    public void sendEvent( FhirCastSessionSubscribe fhirCastSessionSubscribe, FhirCastWorkflowEvent fhirCastWorkflowEvent ){
        HttpHeaders httpHeaders = new HttpHeaders();
        // TODO add HMAC
        HttpEntity<FhirCastWorkflowEvent> entity = new HttpEntity<>(fhirCastWorkflowEvent, httpHeaders);
        ResponseEntity<String> response = restTemplate.exchange(
                fhirCastSessionSubscribe.getHub_callback(),
                HttpMethod.GET, entity, String.class
        );
    }
}
