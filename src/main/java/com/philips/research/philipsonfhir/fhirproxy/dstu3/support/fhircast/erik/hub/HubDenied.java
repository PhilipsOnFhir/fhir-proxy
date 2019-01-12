package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.fhircast.erik.hub;

public class HubDenied {

    private final String mode;
    private final String topic;
    private final String events;
    private final String reason;

    public HubDenied(String mode, String topic, String events, String reason) {
        this.mode = mode;
        this.topic = topic;
        this.events = events;
        this.reason = reason;
    }

    public String getMode() {
        return mode;
    }

    public String getTopic(){
        return topic;
    }

    public String getEvents() {
        return events;
    }

    public String getReason() {
        return reason;
    }
}
