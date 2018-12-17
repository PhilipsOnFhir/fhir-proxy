package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.controller;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.parser.IParser;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.proxy.service.FhirServer;
import org.hl7.fhir.dstu3.model.OperationOutcome;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.instance.model.api.IBaseOperationOutcome;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

@Controller
@RestController
@CrossOrigin(origins = "*")
public class SampleFhirGateway {
    private static final String template = "Hello, %s!";
    protected FhirServer fhirServer = null;
    private Logger logger = Logger.getLogger(SampleFhirGateway.class.getName());
    private FhirContext myContext = FhirContext.forDstu3();

    public SampleFhirGateway(){
//        this( "http:localhost:9500/baseStu3" );
    }
    public SampleFhirGateway(String fhirUrl) {
        this.fhirServer = new FhirServer( fhirUrl );
    }

    @RequestMapping (
            method = RequestMethod.GET,
            value = "/fhir/{resourceType}"
//            ,
//            produces =  "application/fhir+json"
    )
    public String getResourceType(
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
            return parser(accept).encodeResourceToString(fhirServer.doSearch(resourceType, queryParams ));
        }
    }

//    @RequestMapping (
//            method = RequestMethod.GET,
//            value = "/fhir/{resourceType}",
//            produces =  "application/fhir+xml"
//    )
//    public String searchResourcesXml(
//            @RequestHeader("Accept") String accept,
//            @PathVariable String resourceType,
//            @RequestParam Map<String, String> queryParams
//    ) {
//
//        if ( resourceType.equals("metadata")){
//            logger.log(Level.INFO,"JSON GET CapabilityStatement" );
//            return parser(accept).encodeResourceToString( fhirServer.getCapabilityStatement() );
//        }
//        else {
//            logger.log(Level.INFO,"JSON GET "+resourceType );
//            return parser(accept).encodeResourceToString( fhirServer.doSearch(resourceType, queryParams ));
//        }
//    }

    @RequestMapping (
            method = RequestMethod.GET,
            value = "/fhir/{resourceType}/{id}"
            //            ,produces =  "application/fhir+json"
    )
    public String getResourceTypeId(
            @RequestHeader(value = "Accept", defaultValue = "application/fhir+json") String accept,
            @PathVariable String resourceType,
            @PathVariable String id,
            @RequestParam Map<String, String> queryParams
    ) throws Exception {
        return parser(accept).encodeResourceToString( fhirServer.doGet( resourceType, id, queryParams ));
    }


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
//        return doGet( accept, resourceType, id, queryParams );
//    }
//
//    private String doGet(
//            String contentType,
//            String resourceType,
//            String id,
//            Map<String, String> queryParams
//    ) throws Exception {
//        logger.log(Level.INFO,"GET "+resourceType+" "+id );
//        return parser(contentType).encodeResourceToString( fhirServer.doGet( resourceType, id, queryParams ));
//    }
    /////////////////////////////////////////////////////////////////////////////

    @RequestMapping (
            method = RequestMethod.GET,
            value = "/fhir/{resourceType}/{id}/{params}"
//            ,            produces =  "application/fhir+xml"
    )
    public ResponseEntity<String> getResourceTypeIdParams(
            @RequestHeader(value = "Accept", defaultValue = "application/fhir+json") String accept,
//            @RequestHeader(value = "Content-Type", defaultValue = "application/fhir+json") String contentType,
            @PathVariable String resourceType,
            @PathVariable String id,
            @PathVariable String params,
            @RequestParam Map<String, String> queryParams
    ) {
        IBaseResource iBaseResource;
        HttpStatus httpStatus;
        try{
            iBaseResource = fhirServer.doGet( resourceType, id, params, queryParams );
            httpStatus= HttpStatus.OK;
        } catch ( FHIRException e1 ){
            iBaseResource = new OperationOutcome().addIssue( new OperationOutcome.OperationOutcomeIssueComponent()
                .setSeverity( OperationOutcome.IssueSeverity.FATAL )
                .setDiagnostics( e1.getMessage() )
            );
            httpStatus= HttpStatus.INTERNAL_SERVER_ERROR;
        }
        catch ( BaseServerResponseException se ){
            iBaseResource = se.getOperationOutcome();
            httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        ResponseEntity<String> responseEntity = new ResponseEntity<>( parser( accept ).encodeResourceToString(iBaseResource), httpStatus );
        return responseEntity;
    }

//    private String getResourceWithParams(
//            String contentType,
//            String resourceType,
//            String id,
//            Map<String, String> queryParams
//    ) throws Exception {
//        logger.log(Level.INFO,"GET "+resourceType+" "+id );
//        return parser(contentType).encodeResourceToString( fhirServer.doGet( resourceType, id, queryParams ));
//    }
//    /////////////////////////////////////////////////////////////////////////////

    @RequestMapping (
            method = RequestMethod.PUT,
            value = "/fhir/{resourceType}/{id}"
//            ,
//            produces =  "application/fhir+json"
    )
    public ResponseEntity<OperationOutcome> putResource(
            @RequestHeader(value = "Accept", defaultValue = "application/fhir+json") String accept,
            @RequestHeader(value = "Content-Type", defaultValue = "application/fhir+json") String contentType,
            @RequestBody  String requestBody,
            @PathVariable String resourceType,
            @PathVariable String id,
            @RequestParam Map<String, String> queryParams
    ) throws Exception {
        try {
            logger.log(Level.INFO,"PUT "+resourceType+" "+id );
            IBaseResource iBaseResource = parser( contentType ).parseResource(requestBody);
            IBaseOperationOutcome operationalOutcome = fhirServer.updateResource(iBaseResource);
            ResponseEntity responseEntity = ResponseEntity.ok().build();
            return responseEntity;
        }
        catch ( InvalidRequestException e ){
            ResponseEntity responseEntity =
                    ResponseEntity.badRequest().body(e.getOperationOutcome());
            return responseEntity;
        }
    }

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
//    private String putResource(
//            String contentType,
//            String requestBody,
//            String resourceType,
//            String id,
//            Map<String, String> queryParams
//    ) throws Exception {
//
//    }

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
            value = "/fhir"
    )
    public ResponseEntity<String> postResourceType(
            @RequestHeader(value = "Accept", defaultValue = "application/fhir+json") String accept,
            @RequestHeader(value = "Content-Type", defaultValue = "application/fhir+json") String contentType,
            @RequestBody  String requestBody

    ) throws FHIRException, URISyntaxException {
        logger.log(Level.INFO,"POST ");
        try {
            String methodOutcome = fhirServer.doPost(requestBody);
            return ResponseEntity.ok(methodOutcome);
        } catch( ca.uhn.fhir.rest.server.exceptions.InvalidRequestException e){
            logger.warning(e.toString());
            return new ResponseEntity( e.toString(), HttpStatus.valueOf(e.getStatusCode() ) );
        } catch ( Exception e ){
            logger.warning(e.toString());
            return  ResponseEntity.badRequest().body(e.toString());
        }
    }

    @RequestMapping (
            method = RequestMethod.POST,
            value = "/fhir/{resourceType}"
    )
    public ResponseEntity<OperationOutcome> postResourceType(
            @RequestHeader(value = "Accept", defaultValue = "application/fhir+json") String accept,
            @RequestHeader(value = "Content-Type", defaultValue = "application/fhir+json") String contentType,
            @RequestBody  String requestBody,
            @PathVariable String resourceType,
            @RequestParam Map<String, String> queryParams
    ) throws FHIRException, URISyntaxException {
        logger.log(Level.INFO,"POST "+resourceType );
        ResponseEntity<OperationOutcome> responseEntity = null;
                MethodOutcome methodOutcome = fhirServer.doPost(resourceType, parser(contentType).parseResource(requestBody), queryParams);
        if ( methodOutcome.getCreated() ){
            URI location = new URI( methodOutcome.getId().getValue());
            responseEntity = ResponseEntity.created(location).build();
        } else {
            OperationOutcome operationOutcome = new OperationOutcome()
                    .addIssue( new OperationOutcome.OperationOutcomeIssueComponent()
                        .setSeverity(OperationOutcome.IssueSeverity.FATAL)
                        .setDiagnostics(methodOutcome.getOperationOutcome().toString())
                    );
            responseEntity = ResponseEntity.badRequest().body( operationOutcome );
        }
        return responseEntity;
    }


    @RequestMapping (
            method = RequestMethod.POST,
            value = "/fhir/{resourceType}/{id}"
    )
    public String postResourceTypeId(
            @RequestHeader(value = "Accept", defaultValue = "application/fhir+json") String accept,
            @RequestHeader(value = "Content-Type", defaultValue = "application/fhir+json") String contentType,
            @RequestBody  String requestBody,
            @PathVariable String resourceType,
            @PathVariable String id,
            @RequestParam Map<String, String> queryParams
    ) throws FHIRException {
        logger.log(Level.INFO,"POST "+resourceType+" "+id );
        return parser( accept )
                .encodeResourceToString(
                        fhirServer.doPost( resourceType, id, parser(contentType).parseResource(requestBody), queryParams )
                );
    }

    /////////////////////////////////////////////////////////////////////////////


    @RequestMapping (
            method = RequestMethod.POST,
            value = "/fhir/{resourceType}/{id}/{params}"
//            ,
//            produces =  "application/fhir+json"
    )
    public String postResourceTypeIdParams(
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
                        fhirServer.doPost( resourceType, id, parser(contentType).parseResource(requestBody),params, queryParams )
                );
    }
//
//    @RequestMapping (
//            method = RequestMethod.POST,
//            value = "/fhir/{resourceType}/{id}/{params}",
//            produces =  "application/fhir+xml"
//    )
//    public String postXmlResourceWithParams(
//            @RequestHeader("Accept") String accept,
//            @RequestHeader("Content-Type") String contentType,
//            @RequestBody  String requestBody,
//            @PathVariable String resourceType,
//            @PathVariable String id,
//            @PathVariable String params,
//            @RequestParam Map<String, String> queryParams
//    ) throws Exception {
//        logger.log(Level.INFO,"POST "+resourceType+" "+id );
//        return parser( accept )
//                .encodeResourceToString(
//                        fhirServer.doPost( resourceType, id, parser(contentType).parseResource(requestBody),params, queryParams )
//                );
//    }
}
