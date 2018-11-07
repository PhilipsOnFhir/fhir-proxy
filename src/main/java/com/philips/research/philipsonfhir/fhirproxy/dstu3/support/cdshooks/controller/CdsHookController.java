package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.cdshooks.controller;

import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.NotImplementedException;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.cdshooks.model.CdsServiceCallBody;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.cdshooks.model.CdsServiceResponse;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.cdshooks.model.CdsServices;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.cdshooks.service.FhirClinicalReasoningCdsHooksService;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.service.FhirServer;
import org.hl7.fhir.exceptions.FHIRException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RestController
public class CdsHookController {

    static final String PREFIX = "cdshooks";
    private FhirClinicalReasoningCdsHooksService cdsHooksService;

    public void setFhirServerUrl( String url ){
        FhirServer fhirServer = new FhirServer(url);
        this.cdsHooksService = new FhirClinicalReasoningCdsHooksService( fhirServer);
    }

    @GetMapping(PREFIX + "/cds-services")
    public CdsServices getCdsServices() throws FHIRException, NotImplementedException {
        return cdsHooksService.getServices();
    }

    @PostMapping(PREFIX + "/cds-services/{serviceId}")
    public CdsServiceResponse callCdsService(@PathVariable String serviceId, @RequestBody CdsServiceCallBody body ) throws FHIRException, NotImplementedException {
        return cdsHooksService.callCdsService( serviceId, body );
    }
}
