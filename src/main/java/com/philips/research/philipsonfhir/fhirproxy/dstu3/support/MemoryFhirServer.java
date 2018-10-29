package com.philips.research.philipsonfhir.fhirproxy.dstu3.support;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.jpa.rp.dstu3.PatientResourceProvider;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.cdshooks.model.Action;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.service.IFhirServer;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.dstu3.model.codesystems.ActionType;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseOperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;

import java.util.*;

public class MemoryFhirServer implements IFhirServer {
    Map<String, Map<String,Resource>> mapMap = new TreeMap<>();
    FhirContext ourCtx = FhirContext.forDstu3();

    @Override
    public CapabilityStatement getCapabilityStatement() throws NotImplementedException {
        CapabilityStatement capabilityStatement = new CapabilityStatement()
                .setStatus( Enumerations.PublicationStatus.ACTIVE)
                .setDate( new Date())
                .setKind(CapabilityStatement.CapabilityStatementKind.INSTANCE)
                .setFhirVersion("3.0.0")
                .addFormat( "application/fhir+json" )
                ;
        Arrays.stream(ResourceType.values()).forEach( resourceType -> {
            capabilityStatement
                .addRest( new CapabilityStatement.CapabilityStatementRestComponent()
                    .setMode(CapabilityStatement.RestfulCapabilityMode.SERVER)
                    .addResource( new CapabilityStatement.CapabilityStatementRestResourceComponent()
                            .setType(resourceType.name())
                    )
            );
        });
        return capabilityStatement;
    }

    @Override
    public IBaseResource searchResource(String resourceType, Map<String, String> queryParams) throws NotImplementedException {
        Map<String,Resource> resourceMap = getResourceMap(resourceType);
        Bundle bundle = new Bundle();
        resourceMap.values().stream()
                .forEach( resource -> bundle.addEntry(new Bundle.BundleEntryComponent()
                        .setResource(resource)
                        )
                );
        return bundle;
    }

    @Override
    public IBaseResource readResource(String resourceType, String id, Map<String, String> queryParams) throws FHIRException {
        Map<String,Resource> resourceMap = mapMap.get(resourceType);
        if ( resourceMap==null ){
            resourceMap = new TreeMap<>();
        }
        Resource resource = resourceMap.get(id);
        if ( resource!=null ){
            return resource;
        } else{
            return new OperationOutcome().addIssue(new OperationOutcome.OperationOutcomeIssueComponent()
                    .setSeverity(OperationOutcome.IssueSeverity.ERROR)
            );
        }
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
        Map<String,Resource> resourceMap = getResourceMap( resource.getResourceType() );
        resourceMap.put(resource.getId(), resource );
        return new OperationOutcome();
    }

    private Map<String, Resource> getResourceMap(ResourceType resourceType) {
        return getResourceMap(resourceType.name());
    }
    private Map<String, Resource> getResourceMap(String resourceType) {
            Map<String,Resource> resourceMap = mapMap.get(resourceType);
        if ( resourceMap==null ){
            resourceMap = new TreeMap<>();
        }
        return resourceMap;
    }

    @Override
    public IBaseOperationOutcome postResourceOperation(IBaseResource iBaseResource) throws FHIRException, NotImplementedException {
        throw new NotImplementedException();
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
}
