package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.context.model;

import lombok.Getter;
import lombok.Setter;
import org.hl7.fhir.dstu3.model.Resource;

@Getter
@Setter
public class FhirCastContext {
    String key;
    Resource resource;
}
