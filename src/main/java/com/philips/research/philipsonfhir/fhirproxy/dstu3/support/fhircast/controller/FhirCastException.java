package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.fhircast.controller;

public class FhirCastException extends Throwable {
    public FhirCastException(String unknownSessionId) {
        super( unknownSessionId );
    }

}
