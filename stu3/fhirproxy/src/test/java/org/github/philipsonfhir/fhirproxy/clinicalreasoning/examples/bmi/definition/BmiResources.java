package org.github.philipsonfhir.fhirproxy.clinicalreasoning.examples.bmi.definition;

import org.github.philipsonfhir.fhirproxy.clinicalreasoning.examples.bmi.CdsHooksTrigger;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.dstu3.model.codesystems.ActionType;
import org.hl7.fhir.dstu3.model.codesystems.PlanDefinitionType;

public class BmiResources {
    public static org.hl7.fhir.dstu3.model.PlanDefinition getPlanDefinition() {
        PlanDefinition bmiPlanDefinition = (PlanDefinition) new PlanDefinition()
                .setVersion("0.2.0")
                .setName( "BMI protocol")
                .setTitle( "BMI protocol")
                .setType( new CodeableConcept()
                        .addCoding( new Coding()
                                .setCode(PlanDefinitionType.ECARULE.toCode())
                                .setDisplay(PlanDefinitionType.ECARULE.getDisplay())
                                .setSystem(PlanDefinitionType.ECARULE.getSystem())
                        )
                )
                .setPublisher("Philips Research")
                .setDescription("Example Plan Definition that shows different interaction options for PlanDefinitions. " +
                        "The approach has been losely based on CMS069v7." )
                .addAction( new PlanDefinition.PlanDefinitionActionComponent()
                        .addTriggerDefinition( new TriggerDefinition()
                                .setType(TriggerDefinition.TriggerType.NAMEDEVENT)
                                .setEventName(CdsHooksTrigger.PATIENTVIEW.toString())
                        )
                        .addCondition( getPdCondition(
                                "Patient is older than 18, younger than 65," +
                                        " does not have registered BMI value within" +
                                        "the last 12 months, is not pregnant or receiving pallative care.",
                                "PatientElder18Younger65 and NoRecentBmiObservation and NotPregnant and NotReceivingPallativeCare"
                                )
                        )
                        .addAction( new PlanDefinition.PlanDefinitionActionComponent()
                                .addCondition(
                                        getPdCondition(
                                                "QuestionnaireResponse present and no BMI has been created",
                                                "QuestionnaireResponseWithinBmiObs"
                                        )
                                )
                                .setType( getSetTypeCoding(ActionType.CREATE))
                                .setDefinition( new Reference().setReference("ActivityDefinition/createBmiFromQuestionnaireResponse")) // TODO dynamix assignment
                        )
                        .addAction( new PlanDefinition.PlanDefinitionActionComponent()
                                .addCondition(
                                        getPdCondition(
                                                "Height and weight present but no BMI calculated afterwards",
                                                "BmiObservationRequired"
                                                )
                                )
                                .setType( getSetTypeCoding(ActionType.CREATE))
                                .setDefinition( new Reference().setReference("ActivityDefinition/createBmiFromHeightWeightObservations")) // TODO dynamix assignment
                        )
                        .addAction( new PlanDefinition.PlanDefinitionActionComponent()
                                .addCondition( getPdCondition(
                                        "No recent height and weight measurement present, measure height and weight",
                                        "NoRecentHeightWeight")
                                )
                                .setSelectionBehavior( PlanDefinition.ActionSelectionBehavior.EXACTLYONE )
                                .setRequiredBehavior( PlanDefinition.ActionRequiredBehavior.MUST)
                                .addAction( new PlanDefinition.PlanDefinitionActionComponent()
                                        .setDescription("Order Weight and Height measurement.")
                                        .addAction( new PlanDefinition.PlanDefinitionActionComponent()
                                                .setDefinition( new Reference().setReference("ActivityDefinition/createHeightProcedureRequest"))
                                        )
                                        .addAction( new PlanDefinition.PlanDefinitionActionComponent()
                                                .setDefinition( new Reference().setReference("ActivityDefinition/createWeightProcedureRequest"))
                                        )
                                )
                                .addAction( new PlanDefinition.PlanDefinitionActionComponent()
                                        .setDescription("Enter height and weight measurement values")
//                                        .setDefinition(new Reference().setReference("Questionnaire/WeightHeightQuestionnaire"))
                                )
                                .addAction( new PlanDefinition.PlanDefinitionActionComponent()
                                        .setDescription("Patient refuses to have his weight and heigth meaurement taken.")
                                        //.setDefinition(new Reference().setReference("Questionnaire/WeightHeightQuestionnaire"))
                                        // TODO what to do to log this? -- procedure request with "Do not perform".
                                )

                        )
                        .addAction( new PlanDefinition.PlanDefinitionActionComponent()
                                .addCondition( getPdCondition(
                                        "BMI has been measured in the last 12 Months and no follow-up action " +
                                                "has been selected.",
                                        "not(BmiObservationRequired) and not(bmiFollowUpPresent)"
                                ))
                                .addAction( new PlanDefinition.PlanDefinitionActionComponent()
                                        .addCondition( getPdCondition(
                                                "BmiLowerThan18.5"
                                        ))
                                        .addAction( new PlanDefinition.PlanDefinitionActionComponent()
                                                .setDescription("Low weight follow-up actions")
                                                .setSelectionBehavior( PlanDefinition.ActionSelectionBehavior.ONEORMORE)
                                                .addAction( new PlanDefinition.PlanDefinitionActionComponent()
                                                        .setDescription("Patient refuses treatment")
//                                                        .setDefinition()
                                                )
                                                .addAction( new PlanDefinition.PlanDefinitionActionComponent()
                                                        .setDescription("Refer to dietist")
                                                        //TODO continue from here
                                                )
                                        )
                                )
                        )
                )
                .setId( "BmiPlanDefinitionExample");

        return bmiPlanDefinition;
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
}
