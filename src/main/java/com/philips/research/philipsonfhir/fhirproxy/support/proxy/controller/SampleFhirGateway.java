package com.philips.research.philipsonfhir.fhirproxy.support.proxy.controller;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import com.philips.research.philipsonfhir.fhirproxy.support.bulkdata.service.FhirServerService;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseOperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

@Controller
@RestController
@CrossOrigin(origins = "*")
public class SampleFhirGateway {
    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();
    private Logger logger = Logger.getLogger(SampleFhirGateway.class.getName());
    private FhirContext myContext = FhirContext.forDstu3();

    @Autowired
    FhirServerService fhirServer;


    @RequestMapping (
            method = RequestMethod.GET,
            value = "/fhir/{resourceType}",
            produces =  "application/fhir+json"
    )
    public String searchResourcesJson(
            @RequestHeader(value = "Accept", defaultValue = "application/fhir+json") String accept,
            @PathVariable String resourceType,
            @RequestParam Map<String, String> queryParams
    ) {
        if ( resourceType.equals("metadata")){
            logger.log(Level.INFO,"JSON GET CapabilityStatement" );
            return parser(accept).encodeResourceToString( fhirServer.getCapabilityStatement() );
        }
        else {
            logger.log(Level.INFO,"JSON GET "+resourceType );
            return parser(accept).encodeResourceToString(fhirServer.searchResource(resourceType, queryParams ));
        }
    }

    @RequestMapping (
            method = RequestMethod.GET,
            value = "/fhir/{resourceType}",
            produces =  "application/fhir+xml"
    )
    public String searchResourcesXml(
            @RequestHeader("Accept") String accept,
            @PathVariable String resourceType,
            @RequestParam Map<String, String> queryParams
    ) {

        if ( resourceType.equals("metadata")){
            logger.log(Level.INFO,"JSON GET CapabilityStatement" );
            return parser(accept).encodeResourceToString( fhirServer.getCapabilityStatement() );
        }
        else {
            logger.log(Level.INFO,"JSON GET "+resourceType );
            return parser(accept).encodeResourceToString( fhirServer.searchResource(resourceType, queryParams ));
        }
    }

    @RequestMapping (
            method = RequestMethod.GET,
            value = "/fhir/{resourceType}/{id}",
            produces =  "application/fhir+json"
    )
    public String getJsonResource(
            @RequestHeader("Accept") String accept,
            @PathVariable String resourceType,
            @PathVariable String id,
            @RequestParam Map<String, String> queryParams
    ) throws Exception {
        return getResource( accept, resourceType, id, queryParams );
    }


    @RequestMapping (
            method = RequestMethod.GET,
            value = "/fhir/{resourceType}/{id}",
            produces =  "application/fhir+xml"
    )
    public String getXmlResource(
            @RequestHeader("Accept") String accept,
            @PathVariable String resourceType,
            @PathVariable String id,
            @RequestParam Map<String, String> queryParams
    ) throws Exception {
        return getResource( accept, resourceType, id, queryParams );
    }

    private String getResource(
            String contentType,
            String resourceType,
            String id,
            Map<String, String> queryParams
    ) throws Exception {
        logger.log(Level.INFO,"GET "+resourceType+" "+id );
        return parser(contentType).encodeResourceToString( fhirServer.getResource( resourceType, id, queryParams ));
    }
    /////////////////////////////////////////////////////////////////////////////

    @RequestMapping (
            method = RequestMethod.GET,
            value = "/fhir/{resourceType}/{id}/{params}",
            produces =  "application/fhir+xml"
    )
    public String getXmlResourceWithParams(
            @RequestHeader(value = "Accept", defaultValue = "application/fhir+json") String accept,
//            @RequestHeader(value = "Content-Type", defaultValue = "application/fhir+json") String contentType,
            @PathVariable String resourceType,
            @PathVariable String id,
            @PathVariable String params,
            @RequestParam Map<String, String> queryParams
    ) throws Exception {
        return parser(accept).encodeResourceToString(fhirServer.getResource( resourceType, id, params, queryParams ));
    }

    @RequestMapping (
            method = RequestMethod.GET,
            value = "/fhir/{resourceType}/{id}/{params}",
            produces =  "application/fhir+json"
    )
    public String getJsonResourceWithParams(
            @RequestHeader("Accept") String accept,
//            @RequestHeader("Content-Type") String contentType,
            @PathVariable String resourceType,
            @PathVariable String id,
            @PathVariable String params,
            @RequestParam Map<String, String> queryParams
    ) throws Exception {
        return parser(accept).encodeResourceToString(fhirServer.getResource( resourceType, id, params, queryParams ));
    }

    private String getResourceWithParams(
            String contentType,
            String resourceType,
            String id,
            Map<String, String> queryParams
    ) throws Exception {
        logger.log(Level.INFO,"GET "+resourceType+" "+id );
        return parser(contentType).encodeResourceToString( fhirServer.getResource( resourceType, id, queryParams ));
    }
    /////////////////////////////////////////////////////////////////////////////

    @RequestMapping (
            method = RequestMethod.PUT,
            value = "/fhir/{resourceType}/{id}",
            produces =  "application/fhir+json"
    )
    public String putResourceJson(
            @RequestHeader("Content-Type") String contentType,
            @RequestBody  String requestBody,
            @PathVariable String resourceType,
            @PathVariable String id,
            @RequestParam Map<String, String> queryParams
    ) throws Exception {
        return putResource( contentType, requestBody, resourceType, id, queryParams);
    }

    @RequestMapping (
            method = RequestMethod.PUT,
            value = "/fhir/{resourceType}/{id}",
            produces =  "application/fhir+xml"
    )
    public String putResourceXml(
            @RequestHeader("Content-Type") String contentType,
            @RequestBody  String requestBody,
            @PathVariable String resourceType,
            @PathVariable String id,
            @RequestParam Map<String, String> queryParams
    ) throws Exception {
        return putResource( contentType, requestBody, resourceType, id, queryParams);
    }

    private String putResource(
            String contentType,
            String requestBody,
            String resourceType,
            String id,
            Map<String, String> queryParams
    ) throws Exception {
        logger.log(Level.INFO,"PUT "+resourceType+" "+id );
        IBaseResource iBaseResource = parser( contentType ).parseResource(requestBody);
        IBaseOperationOutcome operationalOutcome = fhirServer.putResource(iBaseResource);
        return parser( contentType).encodeResourceToString( operationalOutcome );
    }

    private IParser parser(String contentType) {
        if ( contentType.contains("application/fhir+xml")){
            return myContext.newXmlParser();
        } else if ( contentType.contains("application/fhir+json")){
            return myContext.newJsonParser();
        } else {
            return myContext.newJsonParser();
        }

    }

    /////////////////////////////////////////////////////////////////////////////

    @RequestMapping (
            method = RequestMethod.POST,
            value = "/fhir/{resourceType}/{id}",
            produces =  "application/fhir+xml"
    )
    public String postResourceXml(
            @RequestHeader("Accept") String accept,
            @RequestHeader("Content-Type") String contentType,
            @RequestBody  String requestBody,
            @PathVariable String resourceType,
            @PathVariable String id,
            @RequestParam Map<String, String> queryParams
    ) throws FHIRException {
        logger.log(Level.INFO,"POST "+resourceType+" "+id );
        return parser( accept ).encodeResourceToString(fhirServer.postResource( parser(contentType).parseResource(requestBody) ));
    }

    @RequestMapping (
            method = RequestMethod.POST,
            value = "/fhir/{resourceType}/{id}",
            produces =  "application/fhir+json"
    )
    public String postResourceJson(
            @RequestHeader("Accept") String accept,
            @RequestHeader("Content-Type") String contentType,
            @RequestBody  String requestBody,
            @PathVariable String resourceType,
            @PathVariable String id,
            @RequestParam Map<String, String> queryParams
    ) throws FHIRException {
        logger.log(Level.INFO,"POST "+resourceType+" "+id );
        return parser( accept ).encodeResourceToString(fhirServer.postResource( parser(contentType).parseResource(requestBody) ));
    }

    /////////////////////////////////////////////////////////////////////////////


    @RequestMapping (
            method = RequestMethod.POST,
            value = "/fhir/{resourceType}/{id}/{params}",
            produces =  "application/fhir+json"
    )
    public String postJsonResourceWithParams(
            @RequestHeader(value = "Accept", defaultValue = "application/fhir+json") String accept,
            @RequestHeader(value = "Content-Type", defaultValue = "application/fhir+json") String contentType,
            @RequestBody  String requestBody,
            @PathVariable String resourceType,
            @PathVariable String id,
            @PathVariable String params,
            @RequestParam Map<String, String> queryParams
    ) throws Exception {
        logger.log(Level.INFO,"POST "+resourceType+" "+id );
        return parser( accept )
                .encodeResourceToString(
                        fhirServer.postResource( resourceType, id, parser(contentType).parseResource(requestBody),params, queryParams )
                );
    }

    @RequestMapping (
            method = RequestMethod.POST,
            value = "/fhir/{resourceType}/{id}/{params}",
            produces =  "application/fhir+xml"
    )
    public String postXmlResourceWithParams(
            @RequestHeader("Accept") String accept,
            @RequestHeader("Content-Type") String contentType,
            @RequestBody  String requestBody,
            @PathVariable String resourceType,
            @PathVariable String id,
            @PathVariable String params,
            @RequestParam Map<String, String> queryParams
    ) throws Exception {
        logger.log(Level.INFO,"POST "+resourceType+" "+id );
        return parser( accept )
                .encodeResourceToString(
                        fhirServer.postResource( resourceType, id, parser(contentType).parseResource(requestBody),params, queryParams )
                );
    }
}
