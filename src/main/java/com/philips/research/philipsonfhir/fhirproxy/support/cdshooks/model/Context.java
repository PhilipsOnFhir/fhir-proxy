package com.philips.research.philipsonfhir.fhirproxy.support.cdshooks.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Context {
    // patient-view
    String patientId;
    String encounterId;
}
