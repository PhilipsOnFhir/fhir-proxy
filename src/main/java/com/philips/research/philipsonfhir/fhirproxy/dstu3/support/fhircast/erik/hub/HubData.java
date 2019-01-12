package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.fhircast.erik.hub;

public class HubData {

    private final String callback;
    private final String mode;
    private final String topic;
    private final String events;
    private final String challenge;
    private boolean verified;

    public HubData(String callback, String mode, String topic, String events, String challenge, boolean verified) {
        this.callback = callback;
        this.mode = mode;
        this.topic = topic;
        this.events = events;
        this.challenge = challenge;
        this.verified = verified;
    }

    public String getMode(){
            return this.mode;
            }

    public String getCallback() {
            return this.callback;
            }

    public String getTopic() {
            return this.topic;
            }

    public String getEvents(){
            return this.events;
            }

    public String getChallenge(){
            return this.challenge;

    }

    public boolean getVerified(){
        return verified;
    }

    public void setVerified(boolean status) {
        this.verified = status;
    }
}
