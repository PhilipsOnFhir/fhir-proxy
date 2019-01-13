package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.fhircast.model;

public enum EventTypes {
    OPEN_PATIENT_CHART("open-patient-chart"),
    SWITCH_PATIENT_CHART("switch-patient-chart"),
    CLOSE_PATIENT_CHART("close-patient-chart");

    private final String event;

    EventTypes(String s) {
        this.event = s;
    }

    @Override
    public String toString() {
        return event;
    }
}
