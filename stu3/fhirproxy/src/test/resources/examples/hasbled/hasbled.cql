library hasbled version '1.0'

using FHIR version '3.0.0'

include FHIRHelpers version '3.0.0' called FHIRHelpers

context Patient

///////////////////////////////////////////////////////////////////////
define HasBledRiskAssessments:
   [RiskAssessment] R
//    where R.occurrence after Now() - 1 month
	sort by occurrence

define HasBledRiskAssessment:
   Last( HasBledRiskAssessments )

define HasBledRiskAssessmentExists:
   Exists( HasBledRiskAssessment )

define hasbledRiskAssessmentRisk:
    HasBledRiskAssessment.prediction.qualitativeRisk.coding.code

/////////////////////////////////////////////////////////////////////

define HasbledDescription:
    HasBledRiskAssessment.prediction.rationale.first()

define HasbledTitle:
	HasBledRiskAssessment.code.coding.display.first() + ' : ' +
    HasBledRiskAssessment.prediction.qualitativeRisk.coding.code.first()

define HasbledBackground:
	case
		when HasBledRiskAssessmentExists         then 'Previous riskassessment had '+HasbledTitle
		when HasBledQuestionnaireResponseExists  then 'Previous questionnaire '+ HasBledQuestionnaireResponse.id
		else '-'
	end

///////////////////////////////////////////////////////////////////////
define HasBledQuestionnaireResponses:
   [QuestionnaireResponse] QR
    //where QR.authored after Now() - 1 month
	where QR.questionnaire.reference.value = 'Questionnaire/HasBledCqifQuestionnaire'
	sort by authored

define HasBledQuestionnaireResponse:
   Last( HasBledQuestionnaireResponses )

define HasBledQuestionnaireResponseExists:
   Exists( HasBledQuestionnaireResponses )

///////////////////////////////////////////////////////////////////////

define function retrieveValue( s String ):
//    true
    HasBledQuestionnaireResponse.item.where( linkId.value=s).answer.first().value
//    case
//        when not( HasBledQuestionnaireResponseExists ) then false
//        when HasBledQuestionnaireResponse.item.where( linkId=s).answer.empty() then false
//        else HasBledQuestionnaireResponse.item.where( linkId=s).answer[0].value
//    end
    //	if ( HasBledQuestionnaireResponse.item.where( linkId=s).answer[0].value = null ) then
//		false
//	else
//		HasBledQuestionnaireResponse.item.where( linkId=s).answer[0].value.value


define function createAdvice( s String ):
	case
		when HasBledQuestionnaireResponseExists and retrieveValue( s ) then 'true is suggested based on entry in last questionnaire'
		when HasBledQuestionnaireResponseExists and not(retrieveValue( s )) then 'false is suggested based on entry in last questionnaire'
		else ''
	end

define function questionnaireResponseAdvice( s String ):
      if retrieveValue( s ) then
          'Last questionnaire had value true'
      else
          'Last questionnaire had value false'


define PreviousOldAgeValue:
    retrieveValue('oldAge')

define PreviousOldAgeAdvice:
	case
		when HasBledQuestionnaireResponseExists and PreviousOldAgeValue then 'Last questionnaire had value true'
		when HasBledQuestionnaireResponseExists and not(PreviousOldAgeValue) then 'Last questionnaire had value false'
		else ''
	end

define OldAgeValue:
	( AgeInYears() > 65 )

define PatientOldAgeAdvice:
	case
		when OldAgeValue then 'true is suggested based on patient record.'
		when not(OldAgeValue) then 'false is suggested based on patient record.'
		else ''
	end


define OldAgeAdvice:
	PatientOldAgeAdvice + PreviousOldAgeAdvice


define liverDiseaseValue: retrieveValue( 'liverDisease')

define renalDiseaseValue: retrieveValue( 'renalDisease')

