package org.github.philipsonfhir.fhirproxy.clinicalreasoning.examples.bmi.definition;

import org.hl7.fhir.dstu3.model.*;

import java.util.Date;

public class BmiObservation {
//    .setSystem( "http://loinc.org" ).setDisplay( "BMI observation" );
    private Observation observation;

    BmiObservation( Patient patient, String id, double bmi ) {
        new Observation()
            .setEffective( new DateType( new Date(  ) ) )
            .setSubject( new Reference().setReference( patient.getResourceType()+"/"+patient.getId() ) )
            .setCode( new CodeableConcept()
                .addCoding(OurCodeSystem.getBmiCoding())
                )
            .setId( id );

        observation.setValue( new Quantity()
            .setValue( bmi )
            .setCode("kg/m2")
            .setUnit("kg/m2")
            .setSystem("http://unitsofmeasure.org")
        );
    }

    Observation build(){ return observation; };
}
