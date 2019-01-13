package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.fhircast.controller;

import com.philips.research.philipsonfhir.fhirproxy.dstu3.support.fhircast.model.FhirCastSessionSubscribe;
import com.sun.javafx.binding.StringFormatter;
import org.hl7.fhir.r4.model.MessageHeader;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.security.SecureRandom;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

public class VerificationProcess implements Runnable {

    private final FhirCastSession fhirCastSession;
    private final FhirCastSessionSubscribe fhirCastSessionSubscribe;
    private final String url;
    private String challenge;
    private boolean ready = false;
    private boolean success;
    private Logger logger = Logger.getLogger( this.getClass().getName() );

    public static VerificationProcess verify(FhirCastSession fhirCastSession, FhirCastSessionSubscribe fhirCastSessionSubscribe) {
        VerificationProcess verificationProcess = new VerificationProcess( fhirCastSession, fhirCastSessionSubscribe);
        new Thread( verificationProcess ).start();
        return verificationProcess;
    }

    VerificationProcess(FhirCastSession fhirCastSession, FhirCastSessionSubscribe fhirCastSessionSubscribe){
        this.fhirCastSession = fhirCastSession;
        this.fhirCastSessionSubscribe = fhirCastSessionSubscribe;
        this.url = fhirCastSessionSubscribe.getHub_callback();
    }

    @Override
    public void run() {
        RestTemplate restTemplate = new RestTemplate(  );
//        GET https://app.example.com/session/callback/v7tfwuk17a?hub.mode=subscribe&hub.topic=7jaa86kgdudewiaq0wtu&hub.events=patient-open-chart,patient-close-chart&hub.challenge=meu3we944ix80ox HTTP 1.1
        this.challenge = generateChallenge();
        String query = StringFormatter.format( "?hub.mode=%s&hub.topic=%s&hub.events=%s&hub.challenge=%s&hub.lease+seconds=%s",
            fhirCastSessionSubscribe.getHub_mode(),
            fhirCastSessionSubscribe.getHub_topic(),
            fhirCastSessionSubscribe.getHub_events(),
            challenge,
            "10000000"
            ).getValue();

//        Map<String,String> queryMap = new TreeMap<>();
//        queryMap.put( "hub.mode", fhirCastSessionSubscribe.getHub_mode() );
//        queryMap.put( "hub.topic", fhirCastSessionSubscribe.getHub_topic() );
//        queryMap.put( "&hub.events", fhirCastSessionSubscribe.getHub_events() );
//        queryMap.put( "hub.challenge", challenge );
//        queryMap.put( "hub.lease+seconds=%s", "1000000" );
//        ResponseEntity<String> response = restTemplate.getForEntity( url, String.class, queryMap );

         ResponseEntity<String> response = restTemplate.getForEntity( url+query, String.class  );
//        GET https://app.example.com/session/callback/v7tfwuk17a?hub.mode=subscribe&hub.topic=7jaa86kgdudewiaq0wtu&hub.events=patient-open-chart,patient-close-chart&hub.challenge=meu3we944ix80ox HTTP 1.1
        this.success = response.getStatusCode().value()>=200 && response.getStatusCode().value()<300 && response.getBody().equalsIgnoreCase( challenge );
        ready=true;
        this.fhirCastSession.setCorrect(success);
        logger.info( "verification of "+fhirCastSessionSubscribe.getHub_callback()+" returned "+success );
    }


    static String generateChallenge() {
        SecureRandom generator = new SecureRandom();
        return String.valueOf(generator.nextInt() & Integer.MAX_VALUE);
    }
}
