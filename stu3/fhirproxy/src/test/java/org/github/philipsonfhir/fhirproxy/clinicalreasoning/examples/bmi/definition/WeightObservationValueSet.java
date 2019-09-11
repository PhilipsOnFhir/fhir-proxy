package org.github.philipsonfhir.fhirproxy.clinicalreasoning.examples.bmi.definition;

import org.hl7.fhir.dstu3.model.Enumerations;
import org.hl7.fhir.dstu3.model.ValueSet;

public class WeightObservationValueSet {
    private final ValueSet valueSet;

    public WeightObservationValueSet(){
        this.valueSet = (ValueSet) new ValueSet(  )
                .setName( "Weigth Observation" )
                .setDescription( "Observation code for Weigth observations" )
                .setStatus( Enumerations.PublicationStatus.ACTIVE )
                .setCompose(  new ValueSet.ValueSetComposeComponent()
                        .addInclude( new ValueSet.ConceptSetComponent()
                                .setSystem(OurCodeSystem.getWeigthCoding().getSystem() )
                                .addConcept( new ValueSet.ConceptReferenceComponent(  )
                                        .setCode( OurCodeSystem.getWeigthCoding().getCode() )
                                        .setDisplay( OurCodeSystem.getWeigthCoding().getDisplay() )
                                )
                        )
                )
                .setUrl("http://philips.com/valueset/WeigthObservationValueSet")
                .setId("WeigthObservationValueSet")
        ;
    }

    public ValueSet build() {
        return valueSet;
    }
}
