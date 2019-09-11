package org.github.philipsonfhir.fhirproxy.cdshooks.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CdsService {
    String hook;
    String title;
    String description;
    String id;
    String prefetch;
}
