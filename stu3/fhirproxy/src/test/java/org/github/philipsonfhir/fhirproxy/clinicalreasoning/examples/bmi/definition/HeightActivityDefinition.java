package org.github.philipsonfhir.fhirproxy.clinicalreasoning.examples.bmi.definition;

import org.hl7.fhir.dstu3.model.ActivityDefinition;
import org.hl7.fhir.dstu3.model.CodeableConcept;
import org.hl7.fhir.dstu3.model.Enumerations;

public class HeightActivityDefinition {
    static final String ID ="createHeightPrAd";
    ActivityDefinition createHeightPrAd =
        (ActivityDefinition) new ActivityDefinition()
            .setName("Creates Height ProcedureRequest")
            .setTitle("Creates Height ProcedureRequest")
            .setStatus( Enumerations.PublicationStatus.DRAFT)
            .setDescription(" Create a request for a heigth measurement")
            .setKind( ActivityDefinition.ActivityDefinitionKind.PROCEDUREREQUEST)
            .setCode( new CodeableConcept().addCoding(OurCodeSystem.getHeigthCoding()))
            .addDynamicValue( new ActivityDefinition.ActivityDefinitionDynamicValueComponent()
                .setDescription("Set required time to now")
                .setLanguage("text/fhirpath")
                .setExpression("now()")
                .setPath("occurrence")
            )
            .setId( ID);

    public ActivityDefinition build(){
        return createHeightPrAd;
    }
}
