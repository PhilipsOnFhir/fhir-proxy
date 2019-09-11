package org.github.philipsonfhir.fhirproxy.clinicalreasoning.examples.bmi;

public enum CdsHooksTrigger {
    PATIENTVIEW("patient-view"), ORDERREVIEW("order-review"), MEDICATIONPRESCRIBE("medication-prescribe");

    private final String stringValue;
    CdsHooksTrigger(final String s) { stringValue = s; }
    public String toString() { return stringValue; }

    public static  boolean isCdsHooksTrigger( String stringValue){

        boolean found = false;
        for( CdsHooksTrigger cdsHooksTrigger: CdsHooksTrigger.values() ){
            if ( cdsHooksTrigger.stringValue.equals(stringValue)){
                found=true;
            }
        }
        return found;
    }
}
