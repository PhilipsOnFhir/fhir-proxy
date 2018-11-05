package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.context.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FhirCastWorkflowEvent {
    String timestamp;
    String id;
    FhirCastWorkflowEventEvent event;
}
