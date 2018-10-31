package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.context.service;

import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseResource;

public class FhirAction {
    private final IBaseResource resource;

    public enum FhirActionAction{ CREATE, UPDATE, DELETE };

    private FhirActionAction fhirActionAction;

    FhirAction( FhirActionAction fhirActionAction, IBaseResource resource ){
        this.fhirActionAction = fhirActionAction;
        this.resource = resource;
    }

    public IBaseResource process(IBaseResource iBaseResource) throws FHIRException {
        switch ( fhirActionAction ){
            case CREATE:
                throw new FHIRException("resource already exists");
            case DELETE:
                return null;
            case UPDATE:
                return resource;
            default:
                throw new FHIRException("unkwown action");
        }
    }
}
