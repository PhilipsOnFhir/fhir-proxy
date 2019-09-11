package org.github.philipsonfhir.fhirproxy.clinicalreasoning.examples.bmi.definition;

import org.hl7.fhir.dstu3.model.*;

import java.util.Date;

public class HeigthObservation {
    HeightObservationValueSet heightObservationValueSet = new HeightObservationValueSet();

    private Observation observation;


    public HeigthObservation(Patient patient, String id, double heigth) {
        observation = (Observation) new Observation()
            .setIssued( new Date() )
            .setSubject( new Reference().setReference( patient.getResourceType()+"/"+patient.getId() ) )
            .setCode( new CodeableConcept()
                .addCoding(OurCodeSystem.getHeigthCoding() )
            )
            .setId( id );

        observation.setValue( new Quantity()
            .setValue( heigth )
            .setCode("m")
            .setUnit("m")
            .setSystem("http://unitsofmeasure.org")
        );
    }

    public Observation build(){ return observation; };
}
