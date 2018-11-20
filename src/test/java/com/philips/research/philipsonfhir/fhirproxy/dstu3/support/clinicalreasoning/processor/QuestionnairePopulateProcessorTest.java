package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.clinicalreasoning.processor;

import ca.uhn.fhir.context.FhirContext;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.exceptions.FHIRException;
import org.junit.Test;
import org.opencds.cqf.cql.data.fhir.BaseFhirDataProvider;
import org.opencds.cqf.cql.data.fhir.FhirDataProviderStu3;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class QuestionnairePopulateProcessorTest {
    public QuestionnairePopulateProcessorTest() throws UnsupportedEncodingException {
    }

    @Test
    public void testBasicQuestionnaire() throws FHIRException {
        Questionnaire questionnaire = new Questionnaire()
                .setTitle("Title")
                .setDescription("Description")
                .addItem(new Questionnaire.QuestionnaireItemComponent()
                        .setLinkId("1")
                        .setText("text1")
                )
                .addItem(new Questionnaire.QuestionnaireItemComponent()
                        .setLinkId("2")
                        .setText("text1")
                        .addItem( new Questionnaire.QuestionnaireItemComponent()
                                .setLinkId("2.1")
                        )
                );

        Parameters parameters = new Parameters().addParameter(new Parameters.ParametersParameterComponent()
                .setName("subject")
                .setValue(new Reference("PatientId"))
        );

        QuestionnairePopulateProcessor questionnairePopulateProcessor = new QuestionnairePopulateProcessor(
                null, questionnaire, parameters
        );

        QuestionnaireResponse questionnaireResponse = questionnairePopulateProcessor.getQuestionnaireResponse();

        checkQuestionnaire(questionnaire, questionnaireResponse);

    }

    @Test
    public void testBasicQuestionnaireInitialValues() throws FHIRException {
        Questionnaire questionnaire = new Questionnaire()
                .setTitle("Title")
                .setDescription("Description")
                .addItem(new Questionnaire.QuestionnaireItemComponent()
                        .setLinkId("1")
                        .setText("text1")
                        .setInitial( new StringType("initial"))
                )
                .addItem(new Questionnaire.QuestionnaireItemComponent()
                        .setLinkId("2")
                        .setText("text1")
                        .setInitial( new DecimalType(32432))
                        .addItem( new Questionnaire.QuestionnaireItemComponent()
                                .setLinkId("2.1")
                                .setInitial(new Quantity().setValue(2423))
                        )
                );

        Parameters parameters = new Parameters().addParameter(new Parameters.ParametersParameterComponent()
                .setName("subject")
                .setValue(new Reference("PatientId"))
        );

        QuestionnairePopulateProcessor questionnairePopulateProcessor = new QuestionnairePopulateProcessor(
                null, questionnaire, parameters
        );

        QuestionnaireResponse questionnaireResponse = questionnairePopulateProcessor.getQuestionnaireResponse();

        checkQuestionnaire(questionnaire, questionnaireResponse);
    }

//    @Test
//    public void testCqifQuestionnaireInitialValues() throws FHIRException {
//        Questionnaire questionnaire = (Questionnaire) new Questionnaire()
//                .setTitle("Title")
//                .setDescription("Description")
//                .addItem(new Questionnaire.QuestionnaireItemComponent()
//                        .setLinkId("1")
//                        .setText("text1")
//                        .setInitial( new StringType("initial"))
//                )
//                .addItem(new Questionnaire.QuestionnaireItemComponent()
//                        .setLinkId("2")
//                        .setText("text1")
//                        .addItem( new Questionnaire.QuestionnaireItemComponent()
//                                .setLinkId("2.1")
//                        )
//                ).setMeta(
//                        new Meta().addProfile("http://hl7.org/fhir/StructureDefinition/cqif-questionnaire")
//                ).setId("slakdsadas");
////        questionnaire.addExtension(
////          new Extension()
////            .setUrl("http://hl7.org/fhir/StructureDefinition/cqif-library")
////            .setValue(new Reference().setReference(ResourceType.Library+"/myLibrary"))
////        );
//        Questionnaire.QuestionnaireItemComponent cqifItem = (Questionnaire.QuestionnaireItemComponent) new Questionnaire.QuestionnaireItemComponent()
//                .setLinkId("3")
//                .setText("text3")
//                .addExtension(
//                        new Extension()
//                            .setUrl("http://hl7.org/fhir/StructureDefinition/cqif-initialValue")
//                            .setValue(new StringType("( AgeInYears() >= 18 and AgeInYears() <= 65 )"))
//                );
//        questionnaire.addItem(cqifItem);
//
//        Parameters parameters = new Parameters().addParameter(new Parameters.ParametersParameterComponent()
//                .setName("subject")
//                .setValue(new Reference("PatientId"))
//        );
//
//        BaseFhirDataProvider baseFhirDataProvider = new FhirDataProviderStu3().setEndpoint( "http://doesNotExist" );
//
//        QuestionnairePopulateProcessor questionnairePopulateProcessor = new QuestionnairePopulateProcessor(
//                baseFhirDataProvider, questionnaire, parameters
//        );
//
//        QuestionnaireResponse questionnaireResponse = questionnairePopulateProcessor.getQuestionnaireResponse();
//
//        checkQuestionnaire(questionnaire, questionnaireResponse);
//    }


    private void checkQuestionnaire(Questionnaire questionnaire, QuestionnaireResponse questionnaireResponse) {
        assertNotNull(questionnaireResponse);
        assertEquals( QuestionnaireResponse.QuestionnaireResponseStatus.INPROGRESS, questionnaireResponse.getStatus());
        assertEquals( questionnaire.getIdElement().getId(), questionnaireResponse.getQuestionnaire().getReference() );
        assertEquals( questionnaire.getItem().size(), questionnaireResponse.getItem().size() );

        for( Questionnaire.QuestionnaireItemComponent questionnaireItemComponent: questionnaire.getItem() ){
            List<QuestionnaireResponse.QuestionnaireResponseItemComponent> list =
                    questionnaireResponse.getItem().stream()
                            .filter(questionnaireResponseItem -> questionnaireResponseItem.getLinkId().equals(questionnaireItemComponent.getLinkId()))
                            .collect(Collectors.toList());
            assertEquals(1 , list.size() );
            QuestionnaireResponse.QuestionnaireResponseItemComponent questionnaireResponseItemComponent = list.get(0);
            if( questionnaireItemComponent.hasInitial()){
                assertTrue( questionnaireResponseItemComponent.hasAnswer() );
                assertEquals( questionnaireItemComponent.getInitial(), questionnaireResponseItemComponent.getAnswer().get(0).getValue() );
            }
            check( questionnaireItemComponent, questionnaireResponseItemComponent);
        }
        System.out.println(FhirContext.forDstu3().newJsonParser().setPrettyPrint(true).encodeResourceToString(questionnaireResponse));
    }

    private void check(Questionnaire.QuestionnaireItemComponent questionnaireItemComponent, QuestionnaireResponse.QuestionnaireResponseItemComponent questionnaireResponseItemComponent) {
        for( Questionnaire.QuestionnaireItemComponent subQuestionnaireItemComponent: questionnaireItemComponent.getItem() ){
            List<QuestionnaireResponse.QuestionnaireResponseItemComponent> list =
                    questionnaireResponseItemComponent.getItem().stream()
                            .filter(subQuestionnaireResponseItem ->
                                    subQuestionnaireResponseItem.getLinkId().equals(subQuestionnaireItemComponent.getLinkId())
                            )
                            .collect(Collectors.toList());
            assertEquals(1 , list.size() );
            if( questionnaireItemComponent.hasInitial()){
                assertTrue( questionnaireResponseItemComponent.hasAnswer() );
                assertEquals( questionnaireItemComponent.getInitial(), questionnaireResponseItemComponent.getAnswer().get(0).getValue() );
            }
            assertEquals( subQuestionnaireItemComponent.getLinkId(), list.get(0).getLinkId());
            check( subQuestionnaireItemComponent, list.get(0));
        }
    }

}