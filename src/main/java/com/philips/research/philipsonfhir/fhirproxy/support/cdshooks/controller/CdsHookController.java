package com.philips.research.philipsonfhir.fhirproxy.support.cdshooks.controller;

import com.philips.research.philipsonfhir.fhirproxy.support.cdshooks.model.CdsServiceResponse;
import com.philips.research.philipsonfhir.fhirproxy.support.cdshooks.model.CdsServices;
import com.philips.research.philipsonfhir.fhirproxy.support.cdshooks.service.FhirClinicalReasoningCdsHooksService;
import org.hl7.fhir.exceptions.FHIRException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RestController
public class CdsHookController {

    static final String PREFIX = "cdshooks";

    @Autowired
    FhirClinicalReasoningCdsHooksService cdsHooksService;

    @GetMapping(PREFIX + "/cds-services")
    public CdsServices getCdsServices() throws FHIRException {
        return cdsHooksService.getServices();
    }

    @PostMapping(PREFIX + "/cds-services{serviceId}")
    public CdsServiceResponse callCdsService(@PathVariable String serviceId, @RequestBody String requestBody) {
        return cdsHooksService.callCdsService( serviceId, requestBody );
    }
}
