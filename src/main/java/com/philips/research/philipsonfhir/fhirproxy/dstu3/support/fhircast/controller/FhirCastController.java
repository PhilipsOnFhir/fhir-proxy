package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.fhircast.controller;

import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.fhircast.model.FhirCastBody;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RestController
public class FhirCastController {
    static final String PREFIX = "fhircast";

//    @Autowired
    private MyFhirCastService fhirCastService = new MyFhirCastService();

    @PostMapping(PREFIX )
    public ResponseEntity<String> createFhirCastSession(
        HttpServletRequest request,
        @RequestBody String requestBody) {
        System.out.println( request.getLocalAddr() );
        System.out.println( request.getLocalName() );

        ResponseEntity<String> responseEntity = new ResponseEntity( fhirCastService.createFhirCastSession().getSessionId(), HttpStatus.CREATED );

        return responseEntity;
    }

    @GetMapping(PREFIX )
    public List<String> getFhirCastServices()  {
        return  fhirCastService.getActiveFhirCastSessions().stream()
            .map( session -> session.getSessionId())
            .collect( Collectors.toList());
    }

    @PutMapping(PREFIX+"/{sessionId}" )
    public void updateFhirCastService( @PathVariable String sessionId) throws FhirCastException {
        fhirCastService.updateFhirCastSession(sessionId);
    }

    @DeleteMapping(PREFIX+"/{sessionId}" )
    public void removeFhirCastService( @PathVariable String sessionId) throws FhirCastException {
        fhirCastService.deleteFhirCastSession(sessionId);
    }

    ///////////////////FHIR CAST ////////////////////////////////////////
    @RequestMapping (
        method = RequestMethod.POST,
        value = PREFIX+"/{sessionId}"
    )
    public ResponseEntity subscribeToFhirCast(
        @PathVariable String sessionId,
//        @RequestBody FhirCastSessionSubscribe fhirCastSessionSubscribe,
        @RequestBody FhirCastBody fhirCastBody,
        @RequestParam Map<String, String> queryParams
    ) {
        ResponseEntity<String> responseEntity = new ResponseEntity( HttpStatus.ACCEPTED);

        try {
            if ( fhirCastBody.isSubscribe() ){
                fhirCastService.subscribe(sessionId, fhirCastBody.getFhirCastSessionSubscribe() );
            }
            if ( fhirCastBody.isEvent() ){
                fhirCastService.sendEvent( sessionId, fhirCastBody.getFhirCastWorkflowEvent() );
            }

        } catch (FhirCastException e) {
            responseEntity = new ResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR );
        }
        return responseEntity;
    }

//    @RequestMapping (
//        method = RequestMethod.POST,
//        value = PREFIX+"/{sessionId}"
//    )
//    public ResponseEntity sendFhirCastEvent(
//        @PathVariable String sessionId,
//        @RequestBody FhirCastWorkflowEvent fhirCastWorkflowEvent,
//        @RequestParam Map<String, String> queryParams
//    ) {
//        ResponseEntity<String> responseEntity = new ResponseEntity( HttpStatus.ACCEPTED);
//
//        try {
//            fhirCastService.sendEvent(sessionId, fhirCastWorkflowEvent );
//        } catch (FhirCastException e) {
//            responseEntity = new ResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR );
//        }
//        return responseEntity;
//    }

    @RequestMapping (
        method = RequestMethod.GET,
        value = PREFIX+"/{sessionId}"
    )
    public ResponseEntity getFhirCastContext(
        @PathVariable String sessionId,
        @RequestParam Map<String, String> queryParams
    ) {
        ResponseEntity<String> responseEntity = new ResponseEntity( HttpStatus.ACCEPTED);

        try {
            fhirCastService.getContext( sessionId );
        } catch (FhirCastException e) {
            responseEntity = new ResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR );
        }
        return responseEntity;
    }
}
