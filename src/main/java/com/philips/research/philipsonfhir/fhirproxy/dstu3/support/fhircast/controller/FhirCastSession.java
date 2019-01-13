package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.fhircast.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.fhircast.model.FhirCastSessionSubscribe;
import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.fhircast.model.FhirCastWorkflowEvent;
import lombok.ToString;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

@ToString
public class FhirCastSession {
    private Map<String, FhirCastSessionSubscribe> fhirCastSubscriptions = new TreeMap<>();
    private String sessionId;
    Logger logger = Logger.getLogger( this.getClass().getName() );
    Map<String, String> context = new TreeMap<>(  );
    private boolean verified;

    public FhirCastSession( String sessionId ){
        this.sessionId = sessionId;
    }

    public void subscribe(FhirCastSessionSubscribe fhirCastSessionSubscribe) throws FhirCastException {
        if ( !fhirCastSessionSubscribe.getHub_topic().equals(this.sessionId)){
            throw new FhirCastException( "SessionId "+sessionId+"does not correspond");
        }
        if ( fhirCastSessionSubscribe.getHub_mode().equals("subscribe")){
            this.fhirCastSubscriptions.put( fhirCastSessionSubscribe.getHub_callback(), fhirCastSessionSubscribe );
            VerificationProcess verificationProcess = VerificationProcess.verify( this, fhirCastSessionSubscribe );
        } else if ( fhirCastSessionSubscribe.getHub_mode().equals("unsubscribe")) {
            this.fhirCastSubscriptions.remove( fhirCastSessionSubscribe.getHub_callback() );
        } else {
            throw new FhirCastException("Unknown value for hub.mode "+fhirCastSessionSubscribe.getHub_mode());
        }

    }

    public String getSessionId() {
        return sessionId;
    }

    TestRestTemplate restTemplate = new TestRestTemplate();
    public void sendEvent(FhirCastWorkflowEvent fhirCastWorkflowEvent) throws FhirCastException {

        if ( this.verified ) {
            updateContext( fhirCastWorkflowEvent );
            // send events
            logger.info( "sendEvent " + fhirCastWorkflowEvent );
            for ( FhirCastSessionSubscribe fhirCastSessionSubscribe : this.fhirCastSubscriptions.values() ) {
                if ( fhirCastSessionSubscribe.getHub_events().contains( fhirCastWorkflowEvent.getEvent().getHub_event().toString() ) ) {

//                    if ( fhirCastWorkflowEvent.getId() != sessionId ) {
//                        throw new FhirCastException( "session id mismatch" );
//                    }

                    logger.info( "Sending event to " + fhirCastSessionSubscribe.getHub_callback() );
                    HttpHeaders httpHeaders = new HttpHeaders();
                    ObjectMapper objectMapper = new ObjectMapper();
                    String content = null;
                    try {
                        content = objectMapper.writeValueAsString( fhirCastWorkflowEvent );
                    } catch ( JsonProcessingException e ) {
                        throw new FhirCastException( "parsing Event failed" );
                    }

                    httpHeaders.add( "X-Hub-Signature", calculateHMAC( fhirCastSessionSubscribe.getHub_secret(), content ) );

                    HttpEntity<String> entity = new HttpEntity<>( content, httpHeaders );
                    ResponseEntity<String> response = restTemplate.exchange(
                        fhirCastSessionSubscribe.getHub_callback(),
                        HttpMethod.POST, entity, String.class
                    );
                    logger.info( "Sending event to " + fhirCastSessionSubscribe.getHub_callback() + " " + response.getStatusCode() );
                }
            }
        }
    }


    private void updateContext(FhirCastWorkflowEvent fhirCastWorkflowEvent) {
        fhirCastWorkflowEvent.getEvent().getContext().stream()
            .forEach( fhirCastContext -> this.context.put( fhirCastContext.getKey(), fhirCastContext.getResource() ) );
        //TODO check whether this is valid.....
    }


    public static String calculateHMAC(String secret, String content ) throws FhirCastException {
        Mac mac = null;
        try {
            mac = Mac.getInstance("HmacSHA256");
            String magicKey = "hellothere";
            mac.init(new SecretKeySpec(magicKey.getBytes(), "HmacSHA256"));

            byte[] hash = mac.doFinal(secret.getBytes());
            return DatatypeConverter.printHexBinary(hash);
        } catch ( Exception e ) {
            throw new FhirCastException( "Error generating HMAC" );
        }
    }


    public Map<String, String> getContext() {
        return context;
    }

    public void setCorrect(boolean success) {
        this.verified = success ;
    }
}
