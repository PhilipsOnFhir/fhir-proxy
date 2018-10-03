package com.philips.research.philipsonfhir.fhirproxy.support.cdshooks.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CdsServiceCallBody {
    String hook;
    String hookInstance;
    String fhirServer;
    String fhirAuthorization;
    String user;
    Context context;
    String prefetch;
}