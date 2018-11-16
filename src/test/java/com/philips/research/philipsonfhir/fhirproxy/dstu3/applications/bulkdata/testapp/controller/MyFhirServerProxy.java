package com.philips.research.philipsonfhir.fhirproxy.dstu3.applications.bulkdata.testapp.controller;

import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.bulkdata.controller.BulkDataFhirController;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.service.FhirServer;
import org.springframework.boot.web.context.WebServerInitializedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MyFhirServerProxy extends BulkDataFhirController {
    //    @Autowired WebServerConfiguration webServerConfiguration;
//
//    @LocalServerPort int a;

    MyFhirServerProxy(){
        super();
//        webServerConfiguration.getPort();
    }

    @EventListener
    public void onWebServerInitialized(WebServerInitializedEvent event) {
        System.out.println(event);
        int port = event.getWebServer().getPort();
        super.setFhirServerUrl( "http://localhost:"+port+"/hapi" );
    }


}
