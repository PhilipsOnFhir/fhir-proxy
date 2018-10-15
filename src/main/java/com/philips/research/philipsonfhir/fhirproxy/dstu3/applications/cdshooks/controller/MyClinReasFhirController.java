package com.philips.research.philipsonfhir.fhirproxy.dstu3.applications.cdshooks.controller;

import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.NotImplementedException;
import org.hl7.fhir.exceptions.FHIRException;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MyClinReasFhirController extends com.philips.research.philipsonfhir.fhirproxy.dstu3.applications.clinicalreasoning.controller.MyController {
    public MyClinReasFhirController() throws FHIRException, NotImplementedException {
    }
}

