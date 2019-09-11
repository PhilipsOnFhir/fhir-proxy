package org.github.philipsonfhir.fhirproxy.clinicalreasoning.examples.bmi.definition;

import org.hl7.fhir.dstu3.model.ActivityDefinition;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Enumerations;

public class WeightActivityDefinition {
    static final String ID ="createWeightPrAd";


    ActivityDefinition createWeightPrAd =
        (ActivityDefinition) new ActivityDefinition()
            .setName("Creates Weight ProcedureRequest")
            .setTitle("Creates Weight ProcedureRequest")
            .setStatus(Enumerations.PublicationStatus.DRAFT)
            .setDescription(" Create a request for a weigth measurement")
            .setKind( ActivityDefinition.ActivityDefinitionKind.PROCEDUREREQUEST)
            .setCode( new CodeableConcept().addCoding(OurCodeSystem.getWeigthCoding()))
            .addDynamicValue( new ActivityDefinition.ActivityDefinitionDynamicValueComponent()
                .setDescription("Set required time to now")
                .setLanguage("text/fhirpath")
                .setExpression("now()")
                .setPath("occurrence")
            )
            .setId( ID );

    public ActivityDefinition build(){
        return createWeightPrAd;
    }

}
