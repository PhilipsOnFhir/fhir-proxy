package com.philips.research.philipsonfhir.fhirproxy.applications.cdshooks.controller;

import com.philips.research.philipsonfhir.fhirproxy.support.cdshooks.controller.CdsHookController;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MyCdsHooksController extends CdsHookController {
//    @Value("fhirserver.url")
    String fhirServerUrl = "http://localhost:9500/baseDstu3";

    MyCdsHooksController( ) {
        super();
        this.setFhirServerUrl(  fhirServerUrl  );
    }
}
