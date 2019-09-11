package org.github.philipsonfhir.fhirproxy.clinicalreasoning.examples.bmi.definition;

import org.hl7.fhir.dstu3.model.*;

public class BmiObsActivityDefinition {
    static final String ID ="createBmiObsAd";

    ActivityDefinition createBmiAd = (ActivityDefinition) new ActivityDefinition()
        .setName( "Creates BMI Observation" )
        .setTitle( "Creates BMI Observation" )
        .setDescription( "Body Mass Index (BMI) is a person's weight in kilograms divided by the square of height" +
            " in meters. A high BMI can be an indicator of high body fatness. BMI can be used to screen for" +
            " weight categories that may lead to health problems but it is not diagnostic of the body" +
            " fatness or health of an individual.")
        .addLibrary( new Reference( ).setReference( "Library/"+BmiCqlLibrary.getId() ) )
        .setKind( ActivityDefinition.ActivityDefinitionKind.OBSERVATION)
        .setCode( new CodeableConcept().addCoding(OurCodeSystem.getBmiCoding()) )
        .addDynamicValue( new ActivityDefinition.ActivityDefinitionDynamicValueComponent()
            .setDescription( "Create basic Observation")
            .setPath( "value" )
            .setLanguage( "text/cql" )
            .setExpression( "bmi.BMIQuantity" )
        )
        .setId(ID);

    public ActivityDefinition build(){
        return createBmiAd;
    }
}
