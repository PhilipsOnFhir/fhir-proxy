package org.github.philipsonfhir.fhirproxy.clinicalreasoning.examples.bmi.definition;

import org.hl7.fhir.dstu3.model.ValueSet;

public class BmiObservationValueSet {

    private final ValueSet valueSet;

    public BmiObservationValueSet(){
        this.valueSet = (ValueSet) new ValueSet(  )
                .setDescription( "Observation code for BMI observations" )
                .setName( "BMI Observation" )
                .setCompose(  new ValueSet.ValueSetComposeComponent()
                        .addInclude( new ValueSet.ConceptSetComponent()
                                .setSystem(OurCodeSystem.getBmiCoding().getSystem() )
                                .addConcept( new ValueSet.ConceptReferenceComponent(  )
                                        .setCode(OurCodeSystem.getBmiCoding().getCode() )
                                        .setDisplay( OurCodeSystem.getBmiCoding().getDisplay() )
                                )
                        )
                )
                .setUrl("http://philips.com/ValueSet/BmiObservationValueSet")
                .setId("BmiObservationValueSet")
        ;
    }

    public ValueSet build() {
        return valueSet;
    }
}
