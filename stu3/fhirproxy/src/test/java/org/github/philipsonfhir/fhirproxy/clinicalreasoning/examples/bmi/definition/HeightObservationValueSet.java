package org.github.philipsonfhir.fhirproxy.clinicalreasoning.examples.bmi.definition;

import org.hl7.fhir.dstu3.model.Enumerations;
import org.hl7.fhir.dstu3.model.ValueSet;

public class HeightObservationValueSet {
    private final ValueSet valueSet;

    public HeightObservationValueSet(){
        this.valueSet = (ValueSet) new ValueSet(  )
                .setDescription( "Test observation" )
                .setName( "Heigth Observation" )
                .setStatus( Enumerations.PublicationStatus.ACTIVE )
                .setCompose(  new ValueSet.ValueSetComposeComponent()
                        .addInclude( new ValueSet.ConceptSetComponent()
                                .setSystem( OurCodeSystem.getHeigthCoding().getSystem() )
                                .addConcept( new ValueSet.ConceptReferenceComponent(  )
                                        .setCode( OurCodeSystem.getHeigthCoding().getCode() )
                                        .setDisplay( OurCodeSystem.getHeigthCoding().getDisplay() )
                                )
                        )
                )
                .setUrl("http://philips.com/ValueSet/HeigthObservationValueSet")
                .setId("HeigthObservationValueSet")
        ;
    }

    public ValueSet build() {
        return valueSet;
    }
}
