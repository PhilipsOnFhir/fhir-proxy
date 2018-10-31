//package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.context.controller;
//
//import ca.uhn.fhir.context.FhirContext;
//import ca.uhn.fhir.parser.IParser;
//import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
//import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.NotImplementedException;
//import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.cdshooks.model.CdsServiceCallBody;
//import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.cdshooks.model.CdsServiceResponse;
//import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.cdshooks.model.CdsServices;
//import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.cdshooks.service.FhirClinicalReasoningCdsHooksService;
//import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.context.service.ContextService;
//import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.context.service.ContextSession;
//import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.service.FhirServer;
//import org.hl7.fhir.dstu3.model.OperationOutcome;
//import org.hl7.fhir.exceptions.FHIRException;
//import org.hl7.fhir.instance.model.api.IBaseResource;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.Map;
//import java.util.Set;
//import java.util.logging.Level;
//
//@Controller
//@RestController
//public class ContextController {
//
//    static final String PREFIX = "context";
//    private ContextService contextService;
//    private FhirServer fhirServer=null;
//    private FhirContext myContext = FhirContext.forDstu3();
//
//
//    public void setFhirServerUrl( String url ){
//        fhirServer = new FhirServer(url);
//        this.contextService = new ContextService( fhirServer );
//    }
//
//    @PostMapping(PREFIX )
//    public long callCdsService( @RequestBody CdsServiceCallBody body ) throws FHIRException, NotImplementedException {
//        return contextService.createContext();
//    }
//
//    @GetMapping(PREFIX )
//    public Set<Long> getCdsServices() throws FHIRException, NotImplementedException {
//        return contextService.getActiveContext();
//    }
//
//    /////////////////////////////////////////////////////////////////////////////
//
//    @RequestMapping (
//            method = RequestMethod.GET,
//            value = "/context/{contextId}/{resourceType}",
//            produces =  "application/fhir+json"
//    )
//    public String searchResourcesJson(
//            @RequestHeader(value = "Accept", defaultValue = "application/fhir+json") String accept,
//            @PathVariable String contextId,
//            @PathVariable String resourceType,
//            @RequestParam Map<String, String> queryParams
//    ) throws FHIRException {
//        ContextSession contextSession = contextService.getContextSession(contextId);
//        if ( contextSession==null) { throw new FHIRException("unknown session");}
//
//        /* Context stuff */
//
//
//        if ( resourceType.equals("metadata")){
//            return parser(accept).encodeResourceToString( fhirServer.getCapabilityStatement() );
//        }
//        else {
//            return parser(accept).encodeResourceToString(fhirServer.searchResource(resourceType, queryParams ));
//        }
//    }
//
//    @RequestMapping (
//            method = RequestMethod.GET,
//            value = "/context/{contextId}/{resourceType}",
//            produces =  "application/fhir+xml"
//    )
//    public String searchResourcesXml(
//            @RequestHeader("Accept") String accept,
//            @PathVariable String contextId,
//            @PathVariable String resourceType,
//            @RequestParam Map<String, String> queryParams
//    ) {
//        ContextSession contextSession = contextService.getContextSession(contextId);
//        if ( contextSession==null) { throw new FHIRException("unknown session");}
//
//        /* Context stuff */
//
//        if ( resourceType.equals("metadata")){
//            return parser(accept).encodeResourceToString( fhirServer.getCapabilityStatement() );
//        }
//        else {
//            return parser(accept).encodeResourceToString( fhirServer.searchResource(resourceType, queryParams ));
//        }
//    }
//
//    @RequestMapping (
//            method = RequestMethod.GET,
//            value = "/fhir/{resourceType}/{id}",
//            produces =  "application/fhir+json"
//    )
//    public String getJsonResource(
//            @RequestHeader("Accept") String accept,
//            @PathVariable String resourceType,
//            @PathVariable String id,
//            @RequestParam Map<String, String> queryParams
//    ) throws Exception {
//        return getResource( accept, resourceType, id, queryParams );
//    }
//
//
//    @RequestMapping (
//            method = RequestMethod.GET,
//            value = "/fhir/{resourceType}/{id}",
//            produces =  "application/fhir+xml"
//    )
//    public String getXmlResource(
//            @RequestHeader("Accept") String accept,
//            @PathVariable String resourceType,
//            @PathVariable String id,
//            @RequestParam Map<String, String> queryParams
//    ) throws Exception {
//        return getResource( accept, resourceType, id, queryParams );
//    }
//
//    private String getResource(
//            String contentType,
//            String resourceType,
//            String id,
//            Map<String, String> queryParams
//    ) throws Exception {
//        logger.log(Level.INFO,"GET "+resourceType+" "+id );
//        return parser(contentType).encodeResourceToString( fhirServer.readResource( resourceType, id, queryParams ));
//    }
//
//    @RequestMapping (
//            method = RequestMethod.GET,
//            value = "/fhir/{resourceType}/{id}/{params}",
//            produces =  "application/fhir+xml"
//    )
//    public String getXmlResourceWithParams(
//            @RequestHeader(value = "Accept", defaultValue = "application/fhir+json") String accept,
////            @RequestHeader(value = "Content-Type", defaultValue = "application/fhir+json") String contentType,
//            @PathVariable String resourceType,
//            @PathVariable String id,
//            @PathVariable String params,
//            @RequestParam Map<String, String> queryParams
//    ) throws Exception {
//        return parser(accept).encodeResourceToString(fhirServer.getResourceOperation( resourceType, id, params, queryParams ));
//    }
//
//    @RequestMapping (
//            method = RequestMethod.GET,
//            value = "/fhir/{resourceType}/{id}/{params}",
//            produces =  "application/fhir+json"
//    )
//    public ResponseEntity<String> getJsonResourceWithParams(
//            @RequestHeader("Accept") String accept,
////            @RequestHeader("Content-Type") String contentType,
//            @PathVariable String resourceType,
//            @PathVariable String id,
//            @PathVariable String params,
//            @RequestParam Map<String, String> queryParams
//    )  {
//        IBaseResource iBaseResource;
//        HttpStatus httpStatus;
//        try{
//            iBaseResource = fhirServer.getResourceOperation( resourceType, id, params, queryParams );
//            httpStatus= HttpStatus.OK;
//        } catch ( FHIRException| NotImplementedException e1 ){
//            iBaseResource = new OperationOutcome().addIssue( new OperationOutcome.OperationOutcomeIssueComponent()
//                    .setSeverity( OperationOutcome.IssueSeverity.FATAL )
//                    .setDiagnostics( e1.getMessage() )
//            );
//            httpStatus= HttpStatus.INTERNAL_SERVER_ERROR;
//        }
//        catch ( BaseServerResponseException se ){
//            iBaseResource = se.getOperationOutcome();
//            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
//        }
//        ResponseEntity<String> responseEntity = new ResponseEntity<>( parser( accept ).encodeResourceToString(iBaseResource), httpStatus );
//        return responseEntity;
//    }
//
//    private String getResourceWithParams(
//            String contentType,
//            String resourceType,
//            String id,
//            Map<String, String> queryParams
//    ) throws Exception {
//        logger.log(Level.INFO,"GET "+resourceType+" "+id );
//        return parser(contentType).encodeResourceToString( fhirServer.readResource( resourceType, id, queryParams ));
//    }
//    /////////////////////////////////////////////////////////////////////////////
//
//    @RequestMapping (
//            method = RequestMethod.PUT,
//            value = "/fhir/{resourceType}/{id}",
//            produces =  "application/fhir+json"
//    )
//    public String putResourceJson(
//            @RequestHeader("Content-Type") String contentType,
//            @RequestBody  String requestBody,
//            @PathVariable String resourceType,
//            @PathVariable String id,
//            @RequestParam Map<String, String> queryParams
//    ) throws Exception {
//        return putResource( contentType, requestBody, resourceType, id, queryParams);
//    }
//
//    @RequestMapping (
//            method = RequestMethod.PUT,
//            value = "/fhir/{resourceType}/{id}",
//            produces =  "application/fhir+xml"
//    )
//    public String putResourceXml(
//            @RequestHeader("Content-Type") String contentType,
//            @RequestBody  String requestBody,
//            @PathVariable String resourceType,
//            @PathVariable String id,
//            @RequestParam Map<String, String> queryParams
//    ) throws Exception {
//        return putResource( contentType, requestBody, resourceType, id, queryParams);
//    }
//
//    private IParser parser(String contentType) {
//        if ( contentType.contains("application/fhir+xml")){
//            return myContext.newXmlParser();
//        } else if ( contentType.contains("application/fhir+json")){
//            return myContext.newJsonParser();
//        } else {
//            return myContext.newJsonParser();
//        }
//
//    }
//}
