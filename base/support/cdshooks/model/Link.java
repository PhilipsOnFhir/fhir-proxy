package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.cdshooks.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Link {
    String label;
    String url;
    String type;
    String appContext;
}
