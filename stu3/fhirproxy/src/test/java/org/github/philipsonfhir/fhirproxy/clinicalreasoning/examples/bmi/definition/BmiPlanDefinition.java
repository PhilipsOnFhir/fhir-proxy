package org.github.philipsonfhir.fhirproxy.clinicalreasoning.examples.bmi.definition;

import org.github.philipsonfhir.fhirproxy.clinicalreasoning.examples.bmi.CdsHooksTrigger;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.dstu3.model.codesystems.ActionType;
import org.hl7.fhir.dstu3.model.codesystems.PlanDefinitionType;
import org.hl7.fhir.exceptions.FHIRException;

import java.io.IOException;

public class BmiPlanDefinition {
    private PlanDefinition planDefinition = null;
    Library library = new BmiCqlLibrary().build();

    public  BmiPlanDefinition() throws IOException, FHIRException {

        planDefinition = (PlanDefinition) new PlanDefinition()
            .setVersion("0.2.0")
            .setName( "BMI protocol")
            .setTitle( "BMI protocol")
            .setType( new CodeableConcept()
                .addCoding( new Coding()
                    .setCode( PlanDefinitionType.ECARULE.toCode())
                    .setDisplay(PlanDefinitionType.ECARULE.getDisplay())
                    .setSystem(PlanDefinitionType.ECARULE.getSystem())
                )
            )
            .setPublisher("Philips Research")
            .setDescription("Example Plan Definition that shows different interaction options for PlanDefinitions. " +
                "The approach has been losely based on CMS069v7." );

        planDefinition.addLibrary(  new Reference( "Library/"+library.getId() ) );
        planDefinition
            .addAction( new PlanDefinition.PlanDefinitionActionComponent()
                    .addTriggerDefinition( new TriggerDefinition()
                        .setType(TriggerDefinition.TriggerType.NAMEDEVENT)
                        .setEventName( CdsHooksTrigger.PATIENTVIEW.toString())
                    )
//                    .addCondition( getPdCondition(
//                        "Patient is older than 18, younger than 65," +
//                            " does not have registered BMI value within" +
//                            "the last 12 months, is not pregnant or receiving pallative care.",
//                        "bmi.NotPregnant and bmi.NotReceivingPallativeCare and bmi.PatientElder18Younger65"
//                        )
//                    )
                    .addCondition( getPdCondition(
                            "No recent height and weight measurement present, measure height and weight",
//                            "not(bmi.HeigthMeasured and bmi.WeigthMeasured)")
                            "not(BmiMeasured)")
                    )
                    .setTitle( "BMI value is required")
                    .setDescription("All patients should have a recent BMI value.")
                    .setSelectionBehavior( PlanDefinition.ActionSelectionBehavior.EXACTLYONE )
                    .setRequiredBehavior( PlanDefinition.ActionRequiredBehavior.MUST)
                    .addAction( new PlanDefinition.PlanDefinitionActionComponent()
                            .setDescription("Order Weight and Height measurement.")
                            .addAction( new PlanDefinition.PlanDefinitionActionComponent()
                                    .setType( new Coding()
                                            .setSystem( ActionType.CREATE.getSystem())
                                            .setCode( ActionType.CREATE.toCode())
                                            .setDisplay( ActionType.CREATE.getDisplay())
                                    )
                                    .setDefinition( new Reference().setReference("ActivityDefinition/"+HeightActivityDefinition.ID))
                            )
                            .addAction( new PlanDefinition.PlanDefinitionActionComponent()
                                    .setType( new Coding()
                                            .setSystem( ActionType.CREATE.getSystem())
                                            .setCode( ActionType.CREATE.toCode())
                                            .setDisplay( ActionType.CREATE.getDisplay())
                                    )
                                    .setDefinition( new Reference().setReference("ActivityDefinition/"+WeightActivityDefinition.ID))
                            )
                    )
                    .addAction((PlanDefinition.PlanDefinitionActionComponent) new PlanDefinition.PlanDefinitionActionComponent()
                            .setDescription("Enter Weight and Height measurement.")
//                            .setDefinition(new Reference().setReference("Questionnaire/"+BmiQuestionnaire.getId()))
//                            .setType( RequestGroupValueSet.LAUNCH )
//                            .addDynamicValue( new PlanDefinition.PlanDefinitionActionDynamicValueComponent()
//                                    .setLanguage( "text/fhirpath" )
//                                    .setPath( "%action.resource" )
//                                    .setExpression( "extension.value" )
//                            )
                    )
            )
            .addExtension( new Extension()
                    .setUrl("http://research.philips.com/connect/extension/QuestionnaireReference")
                    .setValue(new Reference().setReference(ResourceType.Questionnaire.name()+"/"+new BmiQuestionnaire().buildQuestionnaire().getId()))
            )
//
//
//
//
//                            .addAction( new PlanDefinition.PlanDefinitionActionComponent()
//                        .addCondition(
//                            getPdCondition(
//                                "Height and weight present but no BMI calculated afterwards",
//                                "bmi.HeigthMeasured and bmi.WeigthMeasured"
//                            )
//                        )
//                        .setType( getSetTypeCoding(ActionType.CREATE))
//                        .setDefinition( new Reference().setReference("ActivityDefinition/"+BmiObsActivityDefinition.ID)) // TODO dynamix assignment
//                    )
//                    .addAction( new PlanDefinition.PlanDefinitionActionComponent()
//                            .addCondition( getPdCondition(
//                                "No recent height and weight measurement present, measure height and weight",
//                                "not(bmi.HeigthMeasured and bmi.WeigthMeasured)")
//                            )
//                            .setSelectionBehavior( PlanDefinition.ActionSelectionBehavior.EXACTLYONE )
//                            .setRequiredBehavior( PlanDefinition.ActionRequiredBehavior.MUST)
//                            .addAction( new PlanDefinition.PlanDefinitionActionComponent()
//                                    .setDescription("Order Weight and Height measurement.")
//                                    .addAction( new PlanDefinition.PlanDefinitionActionComponent()
//                                    .setDefinition( new Reference().setReference("ActivityDefinition/"+HeightActivityDefinition.ID))
//                                    )
//                                    .addAction( new PlanDefinition.PlanDefinitionActionComponent()
//                                        .setDefinition( new Reference().setReference("ActivityDefinition/"+WeightActivityDefinition.ID))
//                                    )
//                            )
//                            .addAction( new PlanDefinition.PlanDefinitionActionComponent()
//                                    .setDescription("Enter height and weight measurement values")
////                                        .setDefinition(new Reference().setReference("Questionnaire/WeightHeightQuestionnaire"))
//                            )
//                            .addAction( new PlanDefinition.PlanDefinitionActionComponent()
//                                    .setDescription("Patient refuses to have his weight and heigth meaurement taken.")
//                                //.setDefinition(new Reference().setReference("Questionnaire/WeightHeightQuestionnaire"))
//                                // TODO what to do to log this? -- procedure request with "Do not perform".
//                            )
//
//                    )
//                    .addAction( new PlanDefinition.PlanDefinitionActionComponent()
//                            .addCondition( getPdCondition(
//                                "BMI has been measured in the last 12 Months and no follow-up action " +
//                                    "has been selected.",
//                                "not(BmiObservationRequired) and not(bmiFollowUpPresent)"
//                            ))
//                            .addAction( new PlanDefinition.PlanDefinitionActionComponent()
//                                    .addCondition( getPdCondition(
//                                        "BmiLowerThan18.5"
//                                    ))
//                                    .addAction( new PlanDefinition.PlanDefinitionActionComponent()
//                                            .setDescription("Low weight follow-up actions")
//                                            .setSelectionBehavior( PlanDefinition.ActionSelectionBehavior.ONEORMORE)
//                                            .addAction( new PlanDefinition.PlanDefinitionActionComponent()
//                                                    .setDescription("Patient refuses treatment")
////                                                        .setDefinition()
//                                            )
//                                            .addAction( new PlanDefinition.PlanDefinitionActionComponent()
//                                                    .setDescription("Refer to dietist")
//                                                //TODO continue from here
//                                            )
//                                    )
//                            )
//                    )
//            )
            .setId( "BmiPlanDefinitionExample");
    }

    private static PlanDefinition.PlanDefinitionActionConditionComponent getPdCondition(String description, String cqlExpression) {
        return new PlanDefinition.PlanDefinitionActionConditionComponent()
            .setKind( PlanDefinition.ActionConditionKind.APPLICABILITY )
            .setDescription(description)
            .setLanguage("text/cql")
            .setExpression(cqlExpression);
    }

    private static PlanDefinition.PlanDefinitionActionConditionComponent getPdCondition( String cqlExpression) {
        return getPdCondition("",cqlExpression);
    }
    private static Coding getSetTypeCoding(ActionType action) {
        Coding coding = new Coding();
        coding.setSystem( action.getSystem() );
        coding.setCode(action.toCode());
        coding.setDisplay(action.getDisplay());
        return coding;
    }

    public PlanDefinition build() {
        return planDefinition;
    }
}
