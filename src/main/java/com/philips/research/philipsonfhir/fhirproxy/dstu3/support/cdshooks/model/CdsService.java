package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.cdshooks.model;

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
