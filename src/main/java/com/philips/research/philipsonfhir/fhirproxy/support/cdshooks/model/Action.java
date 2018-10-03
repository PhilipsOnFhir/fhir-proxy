package com.philips.research.philipsonfhir.fhirproxy.support.cdshooks.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Action {
    String type;
    String description;
    String resource;
}
