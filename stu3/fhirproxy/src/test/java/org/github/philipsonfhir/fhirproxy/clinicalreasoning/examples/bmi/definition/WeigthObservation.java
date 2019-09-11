package org.github.philipsonfhir.fhirproxy.clinicalreasoning.examples.bmi.definition;

import org.hl7.fhir.dstu3.model.*;

import java.util.Date;

public class WeigthObservation {
    HeightObservationValueSet heightObservationValueSet = new HeightObservationValueSet();
    private Observation observation;

    public WeigthObservation(Patient patient, String id, double weigth) {
        observation = (Observation)new Observation()
//            .setEffective( DateTimeType.today()  )
            .setIssued( new Date() )
            .setSubject( new Reference().setReference( patient.getResourceType()+"/"+patient.getId() ) )
            .setCode( new CodeableConcept()
                .addCoding(OurCodeSystem.getWeigthCoding())
            )
            .setId( id );

        observation.setValue( new Quantity()
            .setValue( weigth )
            .setCode("kg")
            .setUnit("kg")
            .setSystem("http://unitsofmeasure.org")
        );
    }

    public Observation build(){ return observation; };
}
