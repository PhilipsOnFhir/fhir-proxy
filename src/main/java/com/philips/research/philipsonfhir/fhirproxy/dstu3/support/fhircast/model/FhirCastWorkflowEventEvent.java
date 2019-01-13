package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.fhircast.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class FhirCastWorkflowEventEvent {
    String hub_topic;
    String hub_event;
    List<FhirCastContext> context = new ArrayList<>();

}
