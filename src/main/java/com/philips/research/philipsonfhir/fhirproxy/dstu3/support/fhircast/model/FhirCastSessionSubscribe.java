package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.fhircast.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FhirCastSessionSubscribe {
    String hub_callback;
    String hub_mode;
    String hub_topic;
    String hub_secret;
    String hub_events;
    String hub_lease_seconds;
}
