package com.philips.research.philipsonfhir.fhirproxy.support.cdshooks.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class CdsServices {
    List<CdsService> services = new ArrayList<>();
}
