package org.github.philipsonfhir.fhirproxy.clinicalreasoning.questionnaire;

import org.cqframework.cql.elm.execution.VersionedIdentifier;
import org.github.philipsonfhir.fhirproxy.clinicalreasoning.common.CqifQuestionnaire;
import org.github.philipsonfhir.fhirproxy.clinicalreasoning.common.CqlExecutionProvider;
import org.github.philipsonfhir.fhirproxy.clinicalreasoning.common.CqlLibrary;
import org.github.philipsonfhir.fhirproxy.clinicalreasoning.common.FhirValueSetter;
import org.github.philipsonfhir.fhirproxy.common.FhirProxyException;
import org.github.philipsonfhir.fhirproxy.common.util.ParametersUtil;
import org.github.philipsonfhir.fhirproxy.common.util.ReferenceUtil;
import org.hl7.fhir.dstu3.model.*;
import org.hl7.fhir.exceptions.FHIRException;
import org.opencds.cqf.cql.data.fhir.BaseFhirDataProvider;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

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
class QuestionnairePopulateProcessor {
    private Logger logger = Logger.getLogger( this.getClass().getName());
    private boolean cqifQuestionnaire;
    private CqlExecutionProvider cqlExecutionProvider = null;
    private QuestionnaireResponse questionnaireResponse;
    private static final String cqifProfileDefinition = "http://hl7.org/fhir/StructureDefinition/cqif-questionnaire";

    QuestionnairePopulateProcessor(BaseFhirDataProvider baseFhirDataProvider, String resourceId, Parameters parameters) throws FhirProxyException {
        this( baseFhirDataProvider, retrieveQuestionnaire( baseFhirDataProvider, resourceId, parameters ), parameters );
    }

    private static Questionnaire retrieveQuestionnaire(BaseFhirDataProvider baseFhirDataProvider, String resourceId, Parameters parameters) throws FhirProxyException {
        Questionnaire questionnaire = null;
        Parameters.ParametersParameterComponent q = ParametersUtil.getParameter(parameters, "questionnaire");
        Parameters.ParametersParameterComponent qref = ParametersUtil.getParameter(parameters, "questionnaireRef");

        if ( q!=null ){
            questionnaire = (Questionnaire) q.getResource();
        } else if (resourceId!=null ){
            questionnaire = baseFhirDataProvider.getFhirClient()
                    .read().resource(Questionnaire.class).withId( resourceId).execute();
        } else if ( qref!=null ){
            Reference reference = (Reference) qref.getValue();
            ReferenceUtil.ParsedReference parsedRef = ReferenceUtil.parseReference(reference);
            if ( parsedRef.hasResourceId() ) {
                questionnaire = baseFhirDataProvider.getFhirClient()
                        .read().resource(Questionnaire.class).withId(parsedRef.getResourceId()).execute();
            }
        }
        if ( questionnaire==null ){
            throw new FhirProxyException("Questionnaire could not be read");
        }
        return questionnaire;
    }

    QuestionnairePopulateProcessor(BaseFhirDataProvider baseFhirDataProvider, Questionnaire questionnaire, Parameters myParameters) throws FHIRException {
            // create questionnaire response
//        this.subject = queryParams.get("subject");
        logger.info("populate "+questionnaire.getId());
        questionnaireResponse = new QuestionnaireResponse();

        questionnaireResponse.setQuestionnaireTarget(questionnaire);
        questionnaireResponse.setQuestionnaire( new Reference(questionnaire));
        questionnaireResponse.setStatus( QuestionnaireResponse.QuestionnaireResponseStatus.INPROGRESS);
        Reference subject = (Reference) ParametersUtil.getParameter(myParameters, "subject").getValue();
        questionnaireResponse.setSubject(subject);
        questionnaireResponse.setAuthored( new Date() );

        cqifQuestionnaire = questionnaire.getMeta().getProfile().stream()
                .anyMatch(profile -> profile.equals(cqifProfileDefinition));

        if ( cqifQuestionnaire ){
            logger.info("cqif questionnaire detected");
//            CqlLibrary cqlLibrary = CqlLibrary.generateCqlLibrary( questionnaire, new ArrayList<>( ));
            List<String> libraries = CqlLibrary.getLibraries(questionnaire);

            List<Object> contextParameters = null;
            if ( libraries.size()==1 ){
                VersionedIdentifier vid = new VersionedIdentifier().withId("Library/"+libraries.get(0));
                cqlExecutionProvider = new CqlExecutionProvider(baseFhirDataProvider, questionnaire,  subject.getReference(), vid, contextParameters);
            } else {
                cqlExecutionProvider = new CqlExecutionProvider(baseFhirDataProvider, questionnaire,  subject.getReference(), contextParameters);
            }
        }

        for( Questionnaire.QuestionnaireItemComponent questionnaireItem : questionnaire.getItem() ) {
            questionnaireResponse.addItem( getQuestionnaireResponseItem( questionnaireItem ));
        }
    }



    private QuestionnaireResponse.QuestionnaireResponseItemComponent getQuestionnaireResponseItem(
            Questionnaire.QuestionnaireItemComponent questionnaireItem
    ) throws FHIRException {
        logger.info("processing item "+questionnaireItem.getLinkId());
        QuestionnaireResponse.QuestionnaireResponseItemComponent questionnaireResponseItem =
                new QuestionnaireResponse.QuestionnaireResponseItemComponent();
        questionnaireResponseItem.setLinkId(questionnaireItem.getLinkId());

        if (questionnaireItem.hasInitial()) {
            logger.info("processing item "+questionnaireItem.getLinkId()+" set initial");
            questionnaireResponseItem.addAnswer( new QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent()
                    .setValue( questionnaireItem.getInitial() )
            );
        }
        if ( cqifQuestionnaire ){
            boolean conditionsMet = true;
            for (String expression: CqifQuestionnaire.getConditionValueExpressions(questionnaireItem)){
                logger.info( "Processing condition  "+expression );
                Object result = cqlExecutionProvider.evaluateInContext(  expression );
                if ( !(result instanceof Boolean) ) {
                    continue;
                }

                if ( !(Boolean) result ) {
                    conditionsMet = false;
                }
            }
            if ( conditionsMet) {
                for (String expression: CqifQuestionnaire.getInitialValueExpressions(questionnaireItem)) {
                    Object result =  cqlExecutionProvider.evaluateInContext( expression );
                    if ( result!=null ) {
                        // ignore null versions
                        Type value = (Type) FhirValueSetter.getBaseValue(result);
                        //                    Type value = (Type) cqlExecutionProvider.evaluateInContext( expression );
                        logger.info("item " + questionnaireItem.getLinkId() + " initial " + value);
                        questionnaireResponseItem.getAnswer().clear();
                        questionnaireResponseItem.addAnswer(new QuestionnaireResponse.QuestionnaireResponseItemAnswerComponent()
                                .setValue(value)
                        );
                    }
                }
            }
        }
        for ( Questionnaire.QuestionnaireItemComponent subQuestionaireItem: questionnaireItem.getItem() ){
            questionnaireResponseItem.addItem( getQuestionnaireResponseItem( subQuestionaireItem ));
        }
        return questionnaireResponseItem;
    }

    QuestionnaireResponse getQuestionnaireResponse() {
        return questionnaireResponse;
    }
}
