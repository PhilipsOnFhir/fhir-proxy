package com.philips.research.philipsonfhir.fhirproxy.dstu3.support.clinicalreasoning.processor;

import org.hl7.fhir.dstu3.model.*;
import org.opencds.cqf.cql.data.fhir.BaseFhirDataProvider;

/**
 In Parameters:
 * identifier	    0..1	uri A logical questionnaire identifier (i.e. ''Questionnaire.identifier''). The server must know the questionnaire or be able to retrieve it from other known repositories.
 * questionnaire	0..1	Questionnaire The Questionnaire is provided directly as part of the request. Servers may choose not to accept questionnaires in this fashion
 * questionnaireRef	0..1	Reference(Questionnaire)  The Questionnaire is provided as a resource reference. Servers may choose not to accept questionnaires in this fashion or may fail if they cannot resolve or access the referenced questionnaire.
 * subject	        1..1	Reference(Any) The resource that is to be the QuestionnaireResponse.subject. The QuestionnaireResponse instance will reference the provided subject. In addition, if the local parameter is set to true, server information about the specified subject will be used to populate the instance.
 * content	        0..*	Reference(Any) Resources containing information to be used to help populate the QuestionnaireResponse. These may be FHIR resources or may be binaries containing FHIR documents, CDA documents or other source materials. Servers may not support all possible source materials and may ignore materials they do not recognize. (They MAY provide warnings if ignoring submitted resources.)
 * local	        0..1	boolean If specified and set to 'true' (and the server is capable), the server should use what resources and other knowledge it has about the referenced subject when pre-populating answers to questions.
 Out Parameters:
 * questionnaire	1..1	QuestionnaireResponse The partially (or fully)-populated set of answers for the specified Questionnaire
 * issues	        0..1	OperationOutcome A list of hints and warnings about problems encountered while populating the questionnaire. These might be show to the user as an advisory note. Note: if the questionnaire cannot be populated at all, then the operation should fail, and an OperationOutcome is returned directly with the failure, rather than using this parameter
 */
public class QuestionnairePopulateProcessor {
    private Reference subject;
    private BaseFhirDataProvider fhirDataProvider;
    private Questionnaire questionnaire;
    private QuestionnaireResponse questionnaireResponse;
    private OperationOutcome operationOutcome;

    public QuestionnairePopulateProcessor(BaseFhirDataProvider baseFhirDataProvider, Questionnaire iBaseResource, MyParameters myParameters) {
            // create questionnaire response
//        this.subject = queryParams.get("subject");
        questionnaireResponse = new QuestionnaireResponse();
        operationOutcome      = new OperationOutcome();


        questionnaireResponse.setQuestionnaireTarget(questionnaire);
        questionnaireResponse.setStatus( QuestionnaireResponse.QuestionnaireResponseStatus.INPROGRESS);
        questionnaireResponse.setSubject((Reference) myParameters.getParameter("subject"));

        for( Questionnaire.QuestionnaireItemComponent questionnaireItem : questionnaire.getItem() ) {
            questionnaireResponse.addItem( getQuestionnaireResponseItem( questionnaireItem ));
        }
    }

    private QuestionnaireResponse.QuestionnaireResponseItemComponent getQuestionnaireResponseItem( Questionnaire.QuestionnaireItemComponent questionnaireItem ) {
        QuestionnaireResponse.QuestionnaireResponseItemComponent questionnaireResponseItem =
                new QuestionnaireResponse.QuestionnaireResponseItemComponent();
        questionnaireResponseItem.setLinkId(questionnaireItem.getLinkId());
        if (questionnaireItem.hasInitial()) {
            questionnaireResponseItem.addAnswer( new QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent()
                    .setValue( questionnaireItem.getInitial() )
            );
        }
        for ( Questionnaire.QuestionnaireItemComponent subQuestionaireItem: questionnaireItem.getItem() ){
            questionnaireResponse.addItem( getQuestionnaireResponseItem( questionnaireItem ));
        }
        return questionnaireResponseItem;
    }
}
