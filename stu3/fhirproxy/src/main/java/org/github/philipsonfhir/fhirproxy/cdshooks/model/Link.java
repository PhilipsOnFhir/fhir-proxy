package org.github.philipsonfhir.fhirproxy.cdshooks.model;

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
