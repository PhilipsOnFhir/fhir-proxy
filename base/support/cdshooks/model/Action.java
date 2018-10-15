package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.cdshooks.model;

import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.dstu3.model.Resource;

@Getter
@Setter
public class Action {
    public enum ActionType {create, update, delete}

    ActionType type;
    String description;
    Resource resource;
}
