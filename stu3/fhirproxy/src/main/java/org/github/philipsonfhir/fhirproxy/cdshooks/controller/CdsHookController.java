package org.github.philipsonfhir.fhirproxy.cdshooks.controller;

import org.github.philipsonfhir.fhirproxy.async.service.AsyncService;
import org.github.philipsonfhir.fhirproxy.cdshooks.model.CdsServiceCallBody;
import org.github.philipsonfhir.fhirproxy.cdshooks.model.CdsServiceResponse;
import org.github.philipsonfhir.fhirproxy.cdshooks.model.CdsServices;
import org.github.philipsonfhir.fhirproxy.cdshooks.service.FhirClinicalReasoningCdsHooksService;
import org.github.philipsonfhir.fhirproxy.common.FhirProxyNotImplementedException;
import org.github.philipsonfhir.fhirproxy.controller.service.FhirServer;
import org.hl7.fhir.exceptions.FHIRException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RestController
public class CdsHookController {

    static final String PREFIX = "cdshooks";
    private FhirClinicalReasoningCdsHooksService cdsHooksService;

    @Autowired
    CdsHookController(@Value("${proxy.fhirserver.url}") String fhirServerUrl ) {
        FhirServer fhirServer = new FhirServer(fhirServerUrl);
        this.cdsHooksService = new FhirClinicalReasoningCdsHooksService( fhirServer);
    }

    public void setFhirServerUrl( String url ){
        FhirServer fhirServer = new FhirServer(url);
        this.cdsHooksService = new FhirClinicalReasoningCdsHooksService( fhirServer);
    }

    @GetMapping(PREFIX + "/cds-services")
    public CdsServices getCdsServices() throws FHIRException {
        CdsServices cdsServices = new CdsServices();

        return cdsHooksService.getServices();
    }

    @PostMapping(PREFIX + "/cds-services/{serviceId}")
    public CdsServiceResponse callCdsService(@PathVariable String serviceId, @RequestBody CdsServiceCallBody body ) throws FHIRException, FhirProxyNotImplementedException {
        return cdsHooksService.callCdsService( serviceId, body );
    }
}
